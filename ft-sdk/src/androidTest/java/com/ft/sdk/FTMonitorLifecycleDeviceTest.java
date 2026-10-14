package com.ft.sdk;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.threadpool.MonitorRunnable;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Run the same APK test against both SDK implementations with monitorArm=A/B.
 * The baseline arm deliberately verifies the reported leak; the default B arm
 * verifies that ended views are collectible while the current view still samples.
 */
@RunWith(AndroidJUnit4.class)
public class FTMonitorLifecycleDeviceTest {
    @Test
    public void endedViewsStopCollecting() throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getTargetContext();
        Bundle arguments = InstrumentationRegistry.getArguments();
        String arm = arguments.getString("monitorArm", "B");
        int closedViews = Integer.parseInt(arguments.getString("monitorClosedViews", "100"));
        Assert.assertTrue("arm must be A or B", "A".equals(arm) || "B".equals(arm));
        Assert.assertTrue("bounded page count", closedViews > 0 && closedViews <= 1000);
        FTRUMConfig config = new FTRUMConfig()
                .setRumAppId("metrics-lifecycle-ab")
                .setEnableTraceUserView(true)
                .setDeviceMetricsMonitorTypes(DetectFrequency.DEFAULT,
                        DeviceMetricsMonitorType.CPU, DeviceMetricsMonitorType.MEMORY);
        Activity activity = null;
        ScheduledThreadPoolExecutor service = null;
        try {
            instrumentation.runOnMainSync(() -> {
                // Library test APKs do not have the Plugin-instrumented
                // Application.onCreate hook used by customer applications.
                FTAutoTrack.startApp((Application) context.getApplicationContext());
                FTSdk.install(FTSDKConfig.builder("http://127.0.0.1:1")
                        .setOnlySupportMainProcess(false).setAutoSync(false).setDebug(false));
                FTSdk.initRUMWithConfig(config);
                Assert.assertNotNull("SDK must install", FTSdk.get());
                Assert.assertTrue("RUM must initialize", FTRUMConfigManager.get().isRumEnable());
            });
            activity = instrumentation.startActivitySync(new Intent(context,
                    MonitorLifecycleTestActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            instrumentation.waitForIdleSync();
            long foregroundDeadline = SystemClock.elapsedRealtime() + 5000;
            while (!FTActivityLifecycleCallbacks.isAppInForeground()
                    && SystemClock.elapsedRealtime() < foregroundDeadline) {
                SystemClock.sleep(20);
            }
            Assert.assertTrue("real Activity must keep the SDK in foreground",
                    FTActivityLifecycleCallbacks.isAppInForeground());
            // Remove the automatic host View's monitor so both arms start with
            // exactly closedViews ended Views and one final active View.
            instrumentation.runOnMainSync(() -> {
                FTRUMGlobalManager.get().stopView();
                FTMonitorManager.release();
                FTMonitorManager.get().initWithConfig(config);
            });
            FTMonitorManager manager = FTMonitorManager.get();
            service = (ScheduledThreadPoolExecutor) readField(manager, "service");
            List<WeakReference<MonitorRunnable>> ended = new ArrayList<>();
            for (int i = 0; i < closedViews; i++) {
                final String name = "metrics-ab-" + i;
                instrumentation.runOnMainSync(() -> {
                    FTRUMGlobalManager.get().startView(name);
                    Map<String, MonitorRunnable> runners = runners(manager);
                    Assert.assertEquals(1, runners.size());
                    ended.add(new WeakReference<>(runners.values().iterator().next()));
                    FTRUMGlobalManager.get().stopView();
                });
            }
            instrumentation.runOnMainSync(() -> FTRUMGlobalManager.get().startView("metrics-ab-active"));
            CountDownLatch drained = new CountDownLatch(1);
            EventConsumerThreadPool.get().execute(drained::countDown);
            Assert.assertTrue("RUM writes must settle before measurement", drained.await(30, TimeUnit.SECONDS));
            SystemClock.sleep(2000);
            System.gc();
            System.runFinalization();
            System.gc();
            SystemClock.sleep(300);
            int retained = 0;
            for (WeakReference<MonitorRunnable> reference : ended) {
                if (reference.get() != null) retained++;
            }
            MonitorRunnable active = runners(manager).values().iterator().next();
            long cpuSamplesBefore = active.getCpuBean().count;
            long memorySamplesBefore = active.getMemoryBean().count;
            long cpuBefore = service.submit(Debug::threadCpuTimeNanos).get(10, TimeUnit.SECONDS);
            long completedBefore = service.getCompletedTaskCount();
            long allocatedBefore = allocatedBytes();
            long gcBefore = gcCount();
            long start = SystemClock.elapsedRealtimeNanos();
            SystemClock.sleep(6000);
            long cpuAfter = service.submit(Debug::threadCpuTimeNanos).get(10, TimeUnit.SECONDS);
            long elapsed = SystemClock.elapsedRealtimeNanos() - start;
            long completed = service.getCompletedTaskCount() - completedBefore - 1;
            long cpuSamples = active.getCpuBean().count - cpuSamplesBefore;
            long memorySamples = active.getMemoryBean().count - memorySamplesBefore;
            JSONObject result = new JSONObject();
            result.put("arm", arm);
            result.put("android_release", Build.VERSION.RELEASE);
            result.put("api", Build.VERSION.SDK_INT);
            result.put("device", Build.MODEL);
            result.put("frequency_ms", 500);
            result.put("metrics", "CPU,MEMORY");
            result.put("ended_views", closedViews);
            result.put("active_map_entries", runners(manager).size());
            result.put("retained_ended_runners_after_gc", retained);
            result.put("pending_tasks", service.getQueue().size());
            result.put("measurement_seconds", elapsed / 1e9);
            result.put("completed_sampling_rounds", completed);
            result.put("sampling_rounds_per_second", completed / (elapsed / 1e9));
            result.put("worker_cpu_percent_one_core", 100.0 * (cpuAfter - cpuBefore) / elapsed);
            result.put("process_managed_allocated_bytes_per_second",
                    (allocatedBytes() - allocatedBefore) / (elapsed / 1e9));
            result.put("process_gc_count_delta", gcCount() - gcBefore);
            result.put("active_cpu_samples", cpuSamples);
            result.put("active_memory_samples", memorySamples);
            result.put("active_cpu_max_ticks", active.getCpuBean().maxValue);
            result.put("active_memory_max_bytes", active.getMemoryBean().maxValue);
            File output = new File(context.getFilesDir(), "monitor-ab-" + arm + "-" + closedViews + ".json");
            try (FileOutputStream stream = new FileOutputStream(output)) {
                stream.write(result.toString(2).getBytes(StandardCharsets.UTF_8));
            }
            Bundle status = new Bundle();
            status.putString("stream", "\nMETRICS_AB " + result.toString() + "\n");
            instrumentation.sendStatus(0, status);
            Assert.assertEquals(1, runners(manager).size());
            Assert.assertTrue("CPU must read actual process ticks", active.getCpuBean().maxValue > 0);
            Assert.assertTrue("memory must read actual process RSS", active.getMemoryBean().maxValue > 0);
            if ("B".equals(arm)) {
                Assert.assertTrue("current CPU sampling must continue", cpuSamples >= 8);
                Assert.assertTrue("current memory sampling must continue", memorySamples >= 8);
                Assert.assertEquals("ended tasks must be collectible", 0, retained);
                Assert.assertTrue("only the active task can remain", service.getQueue().size() <= 1);
                Assert.assertTrue("sampling count must stay bounded", completed <= 20);
            } else {
                Assert.assertTrue("baseline must remain foreground and sample", cpuSamples > 0 && memorySamples > 0);
                Assert.assertEquals("baseline must reproduce task retention", closedViews, retained);
                Assert.assertTrue("baseline must reproduce repeated collection", completed > 20);
            }
        } finally {
            if (activity != null) {
                Activity host = activity;
                instrumentation.runOnMainSync(host::finish);
                instrumentation.waitForIdleSync();
            }
            instrumentation.runOnMainSync(FTSdk::shutDown);
            if (service != null) {
                service.shutdownNow();
                Assert.assertTrue("test metrics worker must terminate", service.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    private static long allocatedBytes() {
        return Long.parseLong(Debug.getRuntimeStat("art.gc.bytes-allocated"));
    }

    private static long gcCount() {
        return Long.parseLong(Debug.getRuntimeStat("art.gc.gc-count"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, MonitorRunnable> runners(FTMonitorManager manager) {
        return (Map<String, MonitorRunnable>) readField(manager, "runnerMap");
    }

    private static Object readField(Object object, String name) {
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }
}
