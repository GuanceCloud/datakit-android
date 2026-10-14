package com.ft.sdk;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.utils.LineProtocolData;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/** Checks the actual persisted upload payload, using a real foreground Activity. */
@RunWith(AndroidJUnit4.class)
public class FTMonitorFieldsDeviceTest {
    private static final String[] METRIC_FIELDS = {
            "cpu_tick_count", "cpu_tick_count_per_second", "memory_avg", "memory_max",
            "fps_avg", "fps_mini", "battery_current_avg", "battery_current_max"
    };

    @Test
    public void viewMetricFieldsSurviveViewCompletion() throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getTargetContext();
        Bundle arguments = InstrumentationRegistry.getArguments();
        String arm = arguments.getString("monitorArm", "B");
        String mode = arguments.getString("monitorFields", "ALL");
        Assert.assertTrue("supported metrics mode", "ALL".equals(mode)
                || "CPU_MEMORY".equals(mode) || "NONE".equals(mode));
        FTRUMConfig config = new FTRUMConfig().setRumAppId("metrics-fields-regression")
                .setEnableTraceUserView(true);
        if ("ALL".equals(mode)) {
            config.setDeviceMetricsMonitorTypes(DetectFrequency.DEFAULT, DeviceMetricsMonitorType.ALL);
        } else if ("CPU_MEMORY".equals(mode)) {
            config.setDeviceMetricsMonitorTypes(DetectFrequency.DEFAULT,
                    DeviceMetricsMonitorType.CPU, DeviceMetricsMonitorType.MEMORY);
        } else {
            config.setDeviceMetricsMonitorTypes();
        }
        Activity activity = null;
        ValueAnimator animation = ValueAnimator.ofFloat(0.9f, 1f);
        try {
            instrumentation.runOnMainSync(() -> {
                FTAutoTrack.startApp((Application) context.getApplicationContext());
                FTSdk.install(FTSDKConfig.builder("http://127.0.0.1:1")
                        .setOnlySupportMainProcess(false).setAutoSync(false).setDebug(false));
                FTSdk.initRUMWithConfig(config);
                Assert.assertTrue(FTRUMConfigManager.get().isRumEnable());
            });
            activity = instrumentation.startActivitySync(new Intent(context,
                    MonitorLifecycleTestActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            instrumentation.waitForIdleSync();
            Assert.assertTrue("real foreground", FTActivityLifecycleCallbacks.isAppInForeground());
            Activity host = activity;
            instrumentation.runOnMainSync(() -> {
                animation.setDuration(500);
                animation.setRepeatCount(ValueAnimator.INFINITE);
                animation.addUpdateListener(value -> host.getWindow().getDecorView()
                        .setAlpha((Float) value.getAnimatedValue()));
                animation.start();
                FTRUMGlobalManager.get().stopView();
            });
            JSONArray pages = new JSONArray();
            String previousId = null;
            for (int page = 0; page < 2; page++) {
                String name = "fields-regression-" + page;
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("fields_regression_page", page);
                instrumentation.runOnMainSync(() -> FTRUMGlobalManager.get().startView(name, properties));
                LineProtocolData active = awaitView(name, true);
                String id = active.getTagAsString(Constants.KEY_RUM_VIEW_ID);
                Assert.assertNotNull(id);
                Assert.assertFalse("each page has its own ID", id.equals(previousId));
                assertFields(active, mode, false);
                SystemClock.sleep(3500);
                instrumentation.runOnMainSync(() -> FTRUMGlobalManager.get().stopView());
                LineProtocolData completed = awaitView(name, false);
                Assert.assertEquals("final fields belong to the same page", id,
                        completed.getTagAsString(Constants.KEY_RUM_VIEW_ID));
                Assert.assertEquals(page, completed.getFieldAsInt("fields_regression_page").intValue());
                Assert.assertTrue(completed.getFieldAsLong(Constants.KEY_RUM_VIEW_TIME_SPENT) > 0);
                assertFields(completed, mode, true);
                pages.put(new JSONObject().put("view_name", name).put("view_id", id)
                        .put("active", metrics(active)).put("completed", metrics(completed)));
                previousId = id;
            }
            JSONObject result = new JSONObject().put("arm", arm).put("mode", mode)
                    .put("frequency_ms", 500).put("pages", pages);
            File output = new File(context.getFilesDir(), "monitor-fields-" + arm + "-" + mode + ".json");
            try (FileOutputStream stream = new FileOutputStream(output)) {
                stream.write(result.toString(2).getBytes(StandardCharsets.UTF_8));
            }
            Bundle status = new Bundle();
            status.putString("stream", "\nMETRICS_FIELDS " + result + "\n");
            instrumentation.sendStatus(0, status);
        } finally {
            instrumentation.runOnMainSync(animation::cancel);
            if (activity != null) {
                Activity host = activity;
                instrumentation.runOnMainSync(host::finish);
                instrumentation.waitForIdleSync();
            }
            instrumentation.runOnMainSync(FTSdk::shutDown);
        }
    }

    private static LineProtocolData awaitView(String name, boolean active) {
        long deadline = SystemClock.elapsedRealtime() + 15000;
        while (SystemClock.elapsedRealtime() < deadline) {
            for (SyncData record : FTDBManager.get().queryDataByDataByTypeLimitDesc(50, DataType.RUM_APP)) {
                LineProtocolData data = new LineProtocolData(record.getDataString());
                if ("view".equals(data.getMeasurement())
                        && name.equals(data.getTagAsString("view_name"))
                        && Boolean.valueOf(active).equals(data.getFieldAsBoolean("is_active"))) {
                    return data;
                }
            }
            SystemClock.sleep(100);
        }
        throw new AssertionError("No upload payload for " + name + ", active=" + active);
    }

    private static void assertFields(LineProtocolData data, String mode, boolean completed) {
        for (int index = 0; index < METRIC_FIELDS.length; index++) {
            String key = METRIC_FIELDS[index];
            boolean enabled = "ALL".equals(mode) || ("CPU_MEMORY".equals(mode) && index < 4);
            Object value = data.getField(key);
            if (!enabled) {
                Assert.assertNull("disabled field must stay absent: " + key, value);
            } else if (completed || index >= 2) {
                Assert.assertTrue("numeric upload field: " + key, value instanceof Number);
                double number = ((Number) value).doubleValue();
                Assert.assertFalse("finite field: " + key, Double.isNaN(number) || Double.isInfinite(number));
            }
        }
        if (!"NONE".equals(mode)) {
            Assert.assertTrue("actual RSS bytes", number(data, "memory_avg") > 0);
            Assert.assertTrue(number(data, "memory_max") >= number(data, "memory_avg"));
            if (completed) {
                Assert.assertTrue(number(data, "cpu_tick_count") > 0);
                Assert.assertTrue(number(data, "cpu_tick_count_per_second") > 0);
                if ("ALL".equals(mode)) {
                    Assert.assertTrue("real rendered frames", number(data, "fps_avg") > 0);
                    Assert.assertTrue(number(data, "fps_mini") > 0);
                }
            }
        }
    }

    private static double number(LineProtocolData data, String key) {
        Object value = data.getField(key);
        Assert.assertTrue("numeric field: " + key, value instanceof Number);
        return ((Number) value).doubleValue();
    }

    private static JSONObject metrics(LineProtocolData data) throws Exception {
        JSONObject fields = new JSONObject();
        for (String key : METRIC_FIELDS) {
            Object value = data.getField(key);
            if (value != null) fields.put(key, value);
        }
        return fields;
    }
}
