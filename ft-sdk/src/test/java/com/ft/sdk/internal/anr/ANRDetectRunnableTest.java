package com.ft.sdk.internal.anr;

import com.ft.sdk.ExtraLogCatSetting;
import com.ft.sdk.FTActivityManager;
import com.ft.sdk.garble.bean.AppState;

import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class ANRDetectRunnableTest {
    private Object originalActivityManager;

    @Before
    public void setUp() throws Exception {
        Field instance = FTActivityManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        originalActivityManager = instance.get(null);
        instance.set(null, null);
        FTActivityManager.get().getAppState();
    }

    @After
    public void tearDown() throws Exception {
        Field instance = FTActivityManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, originalActivityManager);
    }

    @Test
    public void capturesStartupForegroundAndBackgroundBeforeCollectingStacks() throws Exception {
        for (AppState expected : new AppState[]{AppState.STARTUP, AppState.RUN, AppState.BACKGROUND}) {
            setAppState(expected);
            BlockingQueue<AppState> reports = new LinkedBlockingQueue<>();
            Thread mainThread = new Thread("fake-main") {
                @Override
                public StackTraceElement[] getStackTrace() {
                    try {
                        setAppState(expected == AppState.BACKGROUND ? AppState.RUN : AppState.BACKGROUND);
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                    return new StackTraceElement[0];
                }
            };
            ANRDetectRunnable runnable = new ANRDetectRunnable(null,
                    new FakeMainThreadPoster(new LinkedBlockingQueue<>(), mainThread),
                    new ANRDetectRunnable.IssueReporter() {
                        @Override
                        public void report(String stack, long time, String thread) {
                            Assert.fail("State snapshot was not passed to the reporter");
                        }

                        @Override
                        public void report(String stack, long time, String thread, AppState state) {
                            reports.add(state);
                        }
                    }, 25L);
            Thread detector = new Thread(runnable, "anr-state-test");
            detector.start();
            try {
                Assert.assertEquals(expected, reports.poll(2, TimeUnit.SECONDS));
            } finally {
                runnable.shutdown();
                detector.join(1000L);
            }
            Assert.assertFalse(detector.isAlive());
        }
    }

    private static void setAppState(AppState state) throws Exception {
        Field field = FTActivityManager.class.getDeclaredField("appState");
        field.setAccessible(true);
        field.set(FTActivityManager.get(), state);
    }

    @Test
    public void reportsProbeStartStateAndTimeInsteadOfTimeoutStateAndTime() throws Exception {
        for (AppState state : new AppState[]{AppState.STARTUP, AppState.RUN, AppState.BACKGROUND}) {
            assertProbeSnapshot(state, false);
        }
    }

    @Test
    public void healthyProbeDoesNotLeaveItsSnapshotOnTheNextBlockedProbe() throws Exception {
        assertProbeSnapshot(AppState.BACKGROUND, true);
    }

    private void assertProbeSnapshot(AppState expected, boolean healthyProbeFirst) throws Exception {
        setAppState(healthyProbeFirst ? AppState.STARTUP : expected);
        AtomicInteger posts = new AtomicInteger();
        AtomicLong reportedTime = new AtomicLong();
        AtomicLong probeLowerBound = new AtomicLong(System.currentTimeMillis() * 1_000_000L);
        AtomicLong probeUpperBound = new AtomicLong();
        BlockingQueue<AppState> reports = new LinkedBlockingQueue<>();
        ANRDetectRunnable runnable = new ANRDetectRunnable(null,
                new ANRDetectRunnable.MainThreadPoster() {
                    @Override
                    public boolean post(Runnable callback) {
                        try {
                            if (posts.incrementAndGet() == 1 && healthyProbeFirst) {
                                setAppState(expected);
                                callback.run();
                                probeLowerBound.set(System.currentTimeMillis() * 1_000_000L);
                            } else {
                                probeUpperBound.set(System.currentTimeMillis() * 1_000_000L + 999_999L);
                                // Change state before the timeout, rather than during stack capture.
                                setAppState(expected == AppState.BACKGROUND ? AppState.RUN : AppState.BACKGROUND);
                            }
                            return true;
                        } catch (Exception e) {
                            throw new AssertionError(e);
                        }
                    }

                    @Override
                    public Thread getThread() {
                        return new Thread("fake-main");
                    }
                }, new ANRDetectRunnable.IssueReporter() {
                    @Override
                    public void report(String stack, long time, String thread) {
                        Assert.fail("State snapshot was not passed to the reporter");
                    }

                    @Override
                    public void report(String stack, long time, String thread, AppState state) {
                        reportedTime.set(time);
                        reports.add(state);
                    }
                }, 50L);
        Thread detector = new Thread(runnable, "anr-probe-snapshot-test");
        detector.start();
        try {
            Assert.assertEquals(expected, reports.poll(2, TimeUnit.SECONDS));
            Assert.assertTrue(reportedTime.get() >= probeLowerBound.get());
            Assert.assertTrue("Event time moved from probe start to timeout",
                    reportedTime.get() <= probeUpperBound.get());
            Assert.assertEquals(healthyProbeFirst ? 2 : 1, posts.get());
        } finally {
            runnable.shutdown();
            detector.join(1000L);
        }
        Assert.assertFalse(detector.isAlive());
    }

    @Test
    public void legacyPublicConstructorRemainsAvailable() throws Exception {
        Constructor<ANRDetectRunnable> constructor =
                ANRDetectRunnable.class.getConstructor(ExtraLogCatSetting.class);

        Assert.assertTrue(Modifier.isPublic(constructor.getModifiers()));
    }

    @Test
    public void healthyMainThreadMaintainsDetectInterval() throws Exception {
        CountDownLatch firstPost = new CountDownLatch(1);
        CountDownLatch secondPost = new CountDownLatch(1);
        AtomicInteger postCount = new AtomicInteger();
        Thread mainThread = new Thread("fake-main");
        ANRDetectRunnable runnable = new ANRDetectRunnable(
                null,
                new ANRDetectRunnable.MainThreadPoster() {
                    @Override
                    public boolean post(Runnable callback) {
                        if (postCount.incrementAndGet() == 1) {
                            firstPost.countDown();
                        } else {
                            secondPost.countDown();
                        }
                        callback.run();
                        return true;
                    }

                    @Override
                    public Thread getThread() {
                        return mainThread;
                    }
                },
                (stack, occurredAtNanoseconds, threadName) ->
                        Assert.fail("healthy main reported ANR"),
                1_000L);
        Thread detector = new Thread(runnable, "anr-detector-test");
        detector.start();
        try {
            Assert.assertTrue(firstPost.await(1, TimeUnit.SECONDS));
            Assert.assertFalse("healthy callback shortened the detect interval",
                    secondPost.await(100, TimeUnit.MILLISECONDS));
        } finally {
            runnable.shutdown();
            detector.join(1_000L);
        }

        Assert.assertFalse(detector.isAlive());
    }

    @Test
    public void reportsOncePerBlockAndResumesDetectionAfterRecovery() throws Exception {
        BlockingQueue<Runnable> mainCallbacks = new LinkedBlockingQueue<>();
        BlockingQueue<Integer> reports = new LinkedBlockingQueue<>();
        Thread mainThread = new Thread("fake-main");
        AtomicInteger reportCount = new AtomicInteger();
        ANRDetectRunnable runnable = new ANRDetectRunnable(
                null,
                new FakeMainThreadPoster(mainCallbacks, mainThread),
                (stack, occurredAtNanoseconds, threadName) -> {
                    Assert.assertTrue(occurredAtNanoseconds > 0);
                    Assert.assertEquals("fake-main", threadName);
                    reports.add(reportCount.incrementAndGet());
                },
                25L);
        Thread detector = new Thread(runnable, "anr-detector-test");
        detector.start();
        try {
            Runnable callback = mainCallbacks.poll(1, TimeUnit.SECONDS);
            Assert.assertNotNull(callback);
            Assert.assertEquals(Integer.valueOf(1), reports.poll(1, TimeUnit.SECONDS));

            Assert.assertNull(mainCallbacks.poll(100, TimeUnit.MILLISECONDS));
            Assert.assertNull(reports.poll(100, TimeUnit.MILLISECONDS));
            Assert.assertEquals(1, reportCount.get());
            Assert.assertTrue(detector.isAlive());

            callback.run();
            Assert.assertNotNull(mainCallbacks.poll(2, TimeUnit.SECONDS));
            Assert.assertEquals(Integer.valueOf(2), reports.poll(1, TimeUnit.SECONDS));
            Assert.assertEquals(2, reportCount.get());
        } finally {
            runnable.shutdown();
            detector.join(1_000L);
        }
        Assert.assertFalse(detector.isAlive());
    }

    @Test
    public void shutdownWakesDetectorBeforeTheThreshold() throws Exception {
        BlockingQueue<Runnable> mainCallbacks = new LinkedBlockingQueue<>();
        AtomicInteger reports = new AtomicInteger();
        ANRDetectRunnable runnable = new ANRDetectRunnable(
                null,
                new FakeMainThreadPoster(mainCallbacks, new Thread("fake-main")),
                (stack, occurredAtNanoseconds, threadName) -> reports.incrementAndGet(),
                5_000L);
        Thread detector = new Thread(runnable, "anr-detector-test");
        detector.start();
        Assert.assertNotNull(mainCallbacks.poll(1, TimeUnit.SECONDS));

        runnable.shutdown();
        detector.join(1000L);

        Assert.assertFalse(detector.isAlive());
        Assert.assertEquals(0, reports.get());
    }

    private static final class FakeMainThreadPoster implements ANRDetectRunnable.MainThreadPoster {
        private final BlockingQueue<Runnable> callbacks;
        private final Thread thread;

        private FakeMainThreadPoster(BlockingQueue<Runnable> callbacks, Thread thread) {
            this.callbacks = callbacks;
            this.thread = thread;
        }

        @Override
        public boolean post(Runnable runnable) {
            return callbacks.offer(runnable);
        }

        @Override
        public Thread getThread() {
            return thread;
        }
    }
}
