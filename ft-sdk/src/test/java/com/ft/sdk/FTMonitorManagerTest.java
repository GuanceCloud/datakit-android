package com.ft.sdk;

import com.ft.sdk.garble.threadpool.MonitorRunnable;
import com.ft.sdk.garble.utils.LogUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class FTMonitorManagerTest {
    private FTMonitorManager manager;
    private ScheduledThreadPoolExecutor service;

    @Before
    public void setUp() throws Exception {
        LogUtils.setDebug(false);
        setStatic(FTApplication.class, "instance", RuntimeEnvironment.getApplication());
        FTMonitorManager.release();
        setStatic(FTActivityLifecycleCallbacks.class, "activityCount", 1);
        manager = FTMonitorManager.get();
        manager.initWithConfig(new FTRUMConfig().setDeviceMetricsMonitorTypes(
                DetectFrequency.RARE, DeviceMetricsMonitorType.CPU));
        service = (ScheduledThreadPoolExecutor) field(manager, "service");
    }

    @After
    public void tearDown() throws Exception {
        FTMonitorManager.release();
        service.shutdownNow();
        Assert.assertTrue(service.awaitTermination(2, TimeUnit.SECONDS));
        setStatic(FTActivityLifecycleCallbacks.class, "activityCount", 0);
    }

    @Test
    public void endedViewsDoNotAccumulateScheduledTasks() {
        for (int i = 0; i < 200; i++) {
            manager.addMonitor("view-" + i);
            manager.removeMonitor("view-" + i);
        }
        Assert.assertTrue(runners().isEmpty());
        Assert.assertTrue(service.getQueue().isEmpty());

        manager.addMonitor("current");
        Assert.assertEquals(1, runners().size());
        Assert.assertEquals(1, service.getQueue().size());
    }

    @Test
    public void removalCancelsQueuedTaskAndPreventsFurtherCollection() {
        manager.addMonitor("view");
        MonitorRunnable runner = runners().get("view");
        ScheduledFuture<?> task = (ScheduledFuture<?>) service.getQueue().peek();
        int samples = runner.getCpuBean().count;

        manager.removeMonitor("view");
        runner.run();
        manager.removeMonitor("view");

        Assert.assertTrue(task.isCancelled());
        Assert.assertTrue(service.getQueue().isEmpty());
        Assert.assertEquals(samples, runner.getCpuBean().count);
    }

    @Test
    public void registeringSameViewCancelsPreviousRunner() {
        manager.addMonitor("same");
        MonitorRunnable previous = runners().get("same");
        int samples = previous.getCpuBean().count;

        manager.addMonitor("same");
        MonitorRunnable current = runners().get("same");
        previous.run();

        Assert.assertNotSame(previous, current);
        Assert.assertEquals(samples, previous.getCpuBean().count);
        Assert.assertEquals(1, service.getQueue().size());
        Assert.assertTrue(current.getCpuBean().count > 0);
    }

    @Test
    public void removalStillCancelsWhenMetricsWereDisabled() {
        manager.addMonitor("view");
        manager.initWithConfig(new FTRUMConfig().setDeviceMetricsMonitorTypes());
        manager.removeMonitor("view");
        Assert.assertTrue(runners().isEmpty());
        Assert.assertTrue(service.getQueue().isEmpty());
    }

    @Test
    public void releaseCancelsAllRunnersAndCannotRestartOldManager() throws Exception {
        manager.addMonitor("one");
        manager.addMonitor("two");
        MonitorRunnable one = runners().get("one");
        MonitorRunnable two = runners().get("two");

        FTMonitorManager.release();
        manager.addMonitor("after-release");
        one.run();
        two.run();

        Assert.assertTrue(service.awaitTermination(2, TimeUnit.SECONDS));
        Assert.assertTrue(runners().isEmpty());
        Assert.assertTrue(service.getQueue().isEmpty());
        Assert.assertNull(staticField(FTMonitorManager.class, "ftMonitorConfig"));
    }

    @Test
    public void cancellationAfterDequeueStillPreventsCollection() throws Exception {
        CountDownLatch dequeued = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1) {
            @Override
            protected void beforeExecute(Thread thread, Runnable runnable) {
                dequeued.countDown();
                await(proceed);
            }
        };
        executor.setRemoveOnCancelPolicy(true);
        MonitorRunnable runner = new MonitorRunnable(DetectFrequency.FREQUENT, executor, manager);
        try {
            runner.run();
            int samples = runner.getCpuBean().count;
            Assert.assertTrue(dequeued.await(2, TimeUnit.SECONDS));
            runner.cancel();
            proceed.countDown();
            executor.shutdown();
            Assert.assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
            Assert.assertEquals(samples, runner.getCpuBean().count);
            Assert.assertTrue(executor.getQueue().isEmpty());
        } finally {
            proceed.countDown();
            executor.shutdownNow();
            Assert.assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test
    public void cancellationRacingWithSchedulingCancelsNewFuture() throws Exception {
        CountDownLatch scheduling = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1) {
            @Override
            public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
                scheduling.countDown();
                await(proceed);
                return super.schedule(runnable, delay, unit);
            }
        };
        executor.setRemoveOnCancelPolicy(true);
        MonitorRunnable runner = new MonitorRunnable(DetectFrequency.RARE, executor, manager);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread collecting = new Thread(() -> {
            try {
                runner.run();
            } catch (Throwable error) {
                failure.set(error);
            }
        }, "metrics-collect-test");
        Thread cancelling = new Thread(runner::cancel, "metrics-cancel-test");
        try {
            collecting.start();
            Assert.assertTrue(scheduling.await(2, TimeUnit.SECONDS));
            cancelling.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (!(Boolean) field(runner, "stopped") && System.nanoTime() < deadline) {
                Thread.yield();
            }
            Assert.assertEquals(true, field(runner, "stopped"));
            proceed.countDown();
            collecting.join(2000);
            cancelling.join(2000);
            Assert.assertFalse(collecting.isAlive());
            Assert.assertFalse(cancelling.isAlive());
            Assert.assertNull(failure.get());
            Assert.assertTrue(executor.getQueue().isEmpty());
            int samples = runner.getCpuBean().count;
            runner.run();
            Assert.assertEquals(samples, runner.getCpuBean().count);
        } finally {
            proceed.countDown();
            collecting.join(2000);
            cancelling.join(2000);
            executor.shutdownNow();
            Assert.assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test
    public void externalShutdownRacingWithSchedulingDoesNotThrow() throws Exception {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1) {
            @Override
            public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
                shutdown();
                return super.schedule(runnable, delay, unit);
            }
        };
        try {
            MonitorRunnable runner = new MonitorRunnable(DetectFrequency.RARE, executor, manager);
            runner.run();
            Assert.assertEquals(true, field(runner, "stopped"));
            Assert.assertTrue(executor.getQueue().isEmpty());
        } finally {
            executor.shutdownNow();
            Assert.assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test
    public void releaseWithDequeuedTaskDoesNotRecreateSingleton() throws Exception {
        CountDownLatch dequeued = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1) {
            @Override
            protected void beforeExecute(Thread thread, Runnable runnable) {
                dequeued.countDown();
                await(proceed);
            }
        };
        executor.setRemoveOnCancelPolicy(true);
        service.shutdownNow();
        Field executorField = FTMonitorManager.class.getDeclaredField("service");
        executorField.setAccessible(true);
        executorField.set(manager, executor);
        service = executor;
        try {
            manager.addMonitor("view");
            Assert.assertTrue(dequeued.await(2, TimeUnit.SECONDS));
            FTMonitorManager.release();
            proceed.countDown();
            Assert.assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
            Assert.assertNull(staticField(FTMonitorManager.class, "ftMonitorConfig"));
        } finally {
            proceed.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void activeRunnerContinuesSamplingUntilRemoved() throws Exception {
        manager.addMonitor("current");
        MonitorRunnable runner = runners().get("current");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (service.submit(() -> runner.getCpuBean().count).get(1, TimeUnit.SECONDS) < 2
                && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        Assert.assertTrue(runner.getCpuBean().count >= 2);
        manager.removeMonitor("current");
        Assert.assertTrue(service.getQueue().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<String, MonitorRunnable> runners() {
        return (Map<String, MonitorRunnable>) field(manager, "runnerMap");
    }

    private static Object field(Object object, String name) {
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }

    private static Object staticField(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static void setStatic(Class<?> type, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(3, TimeUnit.SECONDS)) throw new AssertionError("latch timeout");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        }
    }
}
