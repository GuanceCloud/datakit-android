package com.ft.sdk;

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
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewTrackingType;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.test.utils.LineProtocolData;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Opt-in device/Studio acceptance. Credentials come from the test application's private files,
 * never from source or instrumentation arguments. Run with -e rumAcceptance true -e runId ID.
 * Timeout cases backdate only the SDK's private session timers; the device clock is untouched.
 */
@RunWith(AndroidJUnit4.class)
public class RUMIntervalFieldsAcceptanceTest {
    private Instrumentation instrumentation;
    private Context context;
    private Activity activity;
    private String run;
    private final Map<String, String[]> expected = new LinkedHashMap<>();
    private final JSONArray evidence = new JSONArray();

    @Test(timeout = 150000)
    public void uploadIntervalAndSessionScenarios() throws Exception {
        Bundle args = InstrumentationRegistry.getArguments();
        Assume.assumeTrue("Explicit opt-in required for real telemetry upload",
                "true".equals(args.getString("rumAcceptance")));
        instrumentation = InstrumentationRegistry.getInstrumentation();
        context = instrumentation.getTargetContext();
        run = args.getString("runId");
        Assert.assertNotNull("Unique runId required", run);
        File configFile = new File(context.getFilesDir(), "rum-acceptance-config.json");
        JSONObject config;
        try (FileInputStream input = new FileInputStream(configFile)) {
            byte[] bytes = new byte[(int) configFile.length()];
            Assert.assertEquals(bytes.length, input.read(bytes));
            config = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
        }
        String url = config.getString("url");
        String token = config.getString("token");
        String appId = config.getString("appId");
        AtomicInteger uploadCode = new AtomicInteger();
        // Observe status only. Never forward SDK debug logs, URLs or credentials to logcat.
        LogUtils.registerInnerLogHandler((level, tag, message) -> {
            if (message.contains("Sync Success-") || message.contains("Sync Fail")) {
                Matcher status = Pattern.compile("code:(\\d+)").matcher(message);
                if (status.find()) uploadCode.set(Integer.parseInt(status.group(1)));
            }
        });
        try {
            instrumentation.runOnMainSync(() -> {
                FTAutoTrack.startApp((Application) context.getApplicationContext());
                FTSdk.install(FTSDKConfig.builder(url, token).setOnlySupportMainProcess(false)
                        .setAutoSync(false).setDebug(true).setEnableDataFilter(false)
                        .setServiceName("rum_interval_acceptance"));
                FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(appId)
                        .setSessionSamplingRate(100).setEnableTraceUserView(true)
                        .setEnableTrackAppUIBlock(true, 300)
                        .addGlobalContext("acceptance_run", run));
            });
            foreground();
            FTRUMGlobalManager.get().stopView();
            String fg = start("foreground", ViewTrackingType.MANUAL);
            resource("foreground", false, false);
            longTask("foreground", timedStart(), false);
            // Exercise the real Looper detector, as well as explicit interval reporting below.
            instrumentation.runOnMainSync(() -> SystemClock.sleep(700));
            instrumentation.waitForIdleSync();
            expect("long_task", "looper_foreground", "foreground", "sdk_init");
            stop(fg);
            expect("view", "foreground", "foreground", "sdk_init");

            String mixed = start("mixed", ViewTrackingType.MANUAL);
            long crossStart = timedStart();
            resource("mixed", true, false);
            Assert.assertFalse(FTActivityLifecycleCallbacks.isAppInForeground());
            longTask("mixed", crossStart, false);
            stop(mixed);
            expect("view", "mixed", "foreground_and_background", "sdk_init");

            String bg = start("background", ViewTrackingType.MANUAL);
            resource("background", false, false);
            longTask("background", timedStart(), false);
            stop(bg);
            expect("view", "background", "background", "sdk_init");

            foreground();
            FTRUMGlobalManager.get().stopView();
            // A bridge-owned automatic View must close at the real onStopped boundary.
            String auto = start("automatic_background", ViewTrackingType.AUTOMATIC);
            SystemClock.sleep(120);
            background();
            expect("view", "automatic_background", "foreground", "sdk_init");
            foreground();
            FTRUMGlobalManager.get().stopView();
            String resumed = start("automatic_resumed", ViewTrackingType.AUTOMATIC);
            Assert.assertNotEquals(auto, resumed);
            FTRUMGlobalManager.get().stopView(auto, null);
            SystemClock.sleep(120);
            Assert.assertFalse("stale stop cannot close the new View", active().isClose());
            stop(resumed);
            expect("view", "automatic_resumed", "foreground", "sdk_init");

            // A positive, recent load origin predating this process's history must be unknown.
            FTRUMGlobalManager.get().onCreateView(name("unknown"), 1_000_000L,
                    Utils.getCurrentNanoTime() - 60_000_000_000L);
            String unknown = start("unknown", ViewTrackingType.MANUAL);
            resource("unknown", false, true);
            longTask("historical_unknown", timedStart(), true);
            stop(unknown);
            expect("view", "unknown", "unknown", "sdk_init");

            FTRUMInnerManager rum = FTRUMInnerManager.get();
            String initialSession = rum.getSessionId();
            String pending = run + "-pending";
            FTRUMGlobalManager.get().startResource(pending, properties("late_resource"));
            NetStatusBean pendingTiming = timing(false);
            setTimer("lastUserActiveTime", System.nanoTime() - FTRUMInnerManager.MAX_RESTING_TIME);
            sessionView("inactivity_timeout");
            Assert.assertNotEquals(initialSession, rum.getSessionId());
            finishResource(pending, pendingTiming, "late_resource", "http://127.0.0.1/pending");
            expect("resource", "late_resource", "foreground", "sdk_init");
            setTimer("lastSessionTime", System.nanoTime() - FTRUMInnerManager.SESSION_EXPIRE_TIME);
            sessionView("max_duration");
            Assert.assertTrue(rum.hotUpdate(0f, null));
            Assert.assertTrue(rum.hotUpdate(1f, null));
            sessionView("sampling_changed");
            String samplingSession = rum.getSessionId();
            Assert.assertFalse("same sampling value must not renew", rum.hotUpdate(1f, null));
            Assert.assertEquals(samplingSession, rum.getSessionId());
            rum.forceRefreshSessionId();
            sessionView("unknown_reason");

            awaitPayloads();
            JSONObject result = new JSONObject().put("run_id", run).put("cases", evidence)
                    .put("session_timeouts_accelerated", true)
                    .put("historical_longtask_simulated", true);
            write("rum-acceptance-result.json", result.toString(2));
            // Flush only after every local assertion passes. The SDK owns HTTP transport/retries.
            FTSdk.flushSyncData();
            long deadline = SystemClock.elapsedRealtime() + 60_000;
            while (SystemClock.elapsedRealtime() < deadline && !records().isEmpty()) {
                SystemClock.sleep(500);
            }
            Assert.assertTrue("SDK upload queue drained", records().isEmpty());
            result.put("upload_http_status", uploadCode.get());
            write("rum-acceptance-result.json", result.toString(2));
            Assert.assertEquals("SDK upload HTTP response", 200, uploadCode.get());
            result.put("sdk_upload_queue_drained", true);
            write("rum-acceptance-result.json", result.toString(2));
            Bundle status = new Bundle();
            status.putString("stream", "\nRUM_ACCEPTANCE " + run + " cases=" + evidence.length()
                    + " queue_drained=true\n");
            instrumentation.sendStatus(0, status);
        } finally {
            if (activity != null) instrumentation.runOnMainSync(activity::finish);
            instrumentation.runOnMainSync(FTSdk::shutDown);
            LogUtils.registerInnerLogHandler(null);
        }
    }

    private String name(String scenario) { return run + "_" + scenario; }

    private HashMap<String, Object> properties(String scenario) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("acceptance_case", scenario);
        return values;
    }

    private String start(String scenario, ViewTrackingType type) {
        return FTRUMGlobalManager.get().startView(name(scenario), properties(scenario), type);
    }

    private void stop(String id) {
        SystemClock.sleep(50);
        FTRUMGlobalManager.get().stopView(id, null);
    }

    private void foreground() {
        Intent intent = new Intent(context, MonitorLifecycleTestActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (activity == null) {
            activity = instrumentation.startActivitySync(intent);
        } else {
            // startActivitySync waits for a new Activity instance; reordering an existing one
            // only resumes it, so observe the real lifecycle instead of waiting for onCreate.
            instrumentation.runOnMainSync(() -> context.startActivity(intent));
        }
        instrumentation.waitForIdleSync();
        awaitForeground(true);
    }

    private void background() {
        instrumentation.runOnMainSync(() -> Assert.assertTrue(activity.moveTaskToBack(true)));
        instrumentation.waitForIdleSync();
        awaitForeground(false);
        SystemClock.sleep(150);
    }

    private void awaitForeground(boolean value) {
        long deadline = SystemClock.elapsedRealtime() + 10_000;
        while (SystemClock.elapsedRealtime() < deadline
                && FTActivityLifecycleCallbacks.isAppInForeground() != value) SystemClock.sleep(50);
        Assert.assertEquals("real Activity lifecycle", value, FTActivityLifecycleCallbacks.isAppInForeground());
    }

    private long timedStart() {
        long start = Utils.getCurrentNanoTime();
        SystemClock.sleep(120);
        return start;
    }

    private void longTask(String scenario, long start, boolean historical) {
        FTRUMInnerManager.get().addLongTask("Acceptance interval: " + scenario,
                Utils.getCurrentNanoTime() - start, start, historical, properties(scenario));
        expect("long_task", scenario, historical ? "unknown" : state(scenario), "sdk_init");
    }

    private NetStatusBean timing(boolean unknown) {
        NetStatusBean timing = new NetStatusBean();
        timing.callStartTime = System.nanoTime();
        timing.callStartEpoch = unknown ? 1 : Utils.getCurrentNanoTime();
        return timing;
    }

    /** Real loopback HTTP, with lifecycle transition while the response is pending. */
    private void resource(String scenario, boolean crossBackground, boolean unknown) throws Exception {
        String id = name("resource_" + scenario);
        FTRUMGlobalManager.get().startResource(id, properties(scenario));
        NetStatusBean timing = timing(unknown);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            server.setSoTimeout(15000);
            Thread responder = new Thread(() -> {
                try (Socket socket = server.accept()) {
                    socket.setSoTimeout(15000);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true) {
                        String line = reader.readLine();
                        if (line == null || line.isEmpty()) break;
                    }
                    SystemClock.sleep(150);
                    if (crossBackground) background();
                    socket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Length: 2\r\n"
                            + "Content-Type: text/plain\r\nConnection: close\r\n\r\nOK")
                            .getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().flush();
                } catch (Throwable error) { failure.set(error); }
            }, "rum-acceptance-http");
            responder.start();
            String url = "http://127.0.0.1:" + server.getLocalPort() + "/" + scenario;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            try {
                Assert.assertEquals(200, connection.getResponseCode());
                while (connection.getInputStream().read() != -1) { /* consume response */ }
            } finally { connection.disconnect(); }
            responder.join(15000);
            Assert.assertFalse("HTTP responder terminated", responder.isAlive());
            if (failure.get() != null) throw new AssertionError(failure.get());
            finishResource(id, timing, scenario, url);
        }
        expect("resource", scenario, state(scenario), "sdk_init");
    }

    private void finishResource(String id, NetStatusBean timing, String scenario, String url) {
        timing.bodyEndTime = System.nanoTime();
        ResourceParams params = new ResourceParams();
        params.url = url;
        params.resourceMethod = "GET";
        params.resourceStatus = 200;
        params.responseContentType = "text/plain";
        params.property = properties(scenario);
        FTRUMGlobalManager.get().stopResource(id);
        FTRUMGlobalManager.get().addResource(id, params, timing);
    }

    private String state(String scenario) {
        return "mixed".equals(scenario) ? "foreground_and_background" : scenario;
    }

    private void sessionView(String scenario) {
        String reason = "unknown_reason".equals(scenario) ? "unknown" : scenario;
        String id = start(scenario, ViewTrackingType.MANUAL);
        FTRUMGlobalManager.get().addAction(name(scenario), "acceptance", properties(scenario));
        stop(id);
        expect("view", scenario, "foreground", reason);
        expect("action", scenario, null, reason);
    }

    private void setTimer(String name, long value) throws Exception {
        Field field = FTRUMInnerManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setLong(FTRUMInnerManager.get(), value);
    }

    private com.ft.sdk.garble.bean.ActiveViewBean active() throws Exception {
        Field field = FTRUMInnerManager.class.getDeclaredField("activeView");
        field.setAccessible(true);
        return (com.ft.sdk.garble.bean.ActiveViewBean) field.get(FTRUMInnerManager.get());
    }

    private void expect(String type, String scenario, String state, String reason) {
        expected.put(type + ":" + scenario, new String[]{state, reason});
    }

    private java.util.List<SyncData> records() {
        return FTDBManager.get().queryDataByDataByTypeLimitDesc(1000, DataType.RUM_APP);
    }

    private void awaitPayloads() throws Exception {
        long deadline = SystemClock.elapsedRealtime() + 20_000;
        Map<String, LineProtocolData> found = new HashMap<>();
        StringBuilder lines = new StringBuilder();
        while (SystemClock.elapsedRealtime() < deadline) {
            found.clear();
            lines.setLength(0);
            for (SyncData record : records()) {
                LineProtocolData data = new LineProtocolData(record.getDataString());
                if (!run.equals(data.getTagAsString("acceptance_run"))) continue;
                lines.append(record.getDataString()).append('\n');
                if ("view".equals(data.getMeasurement())
                        && !Boolean.FALSE.equals(data.getFieldAsBoolean("is_active"))) continue;
                String key = data.getMeasurement() + ":" + data.getFieldAsString("acceptance_case");
                if ("long_task".equals(data.getMeasurement())
                        && data.getField("acceptance_case") == null
                        && name("foreground").equals(data.getTagAsString("view_name"))
                        && data.getField("duration") instanceof Number
                        && ((Number) data.getField("duration")).longValue() >= 500_000_000L) {
                    key = "long_task:looper_foreground";
                }
                if (!found.containsKey(key)) found.put(key, data);
            }
            if (found.keySet().containsAll(expected.keySet())) break;
            SystemClock.sleep(200);
        }
        write("rum-acceptance-payloads.txt", lines.toString());
        for (Map.Entry<String, String[]> entry : expected.entrySet()) {
            LineProtocolData data = found.get(entry.getKey());
            Assert.assertNotNull("payload missing: " + entry.getKey() + " found=" + found.keySet(), data);
            Assert.assertEquals(entry.getKey() + " app_state", entry.getValue()[0],
                    data.getFieldAsString("app_state"));
            Assert.assertEquals(entry.getKey() + " session_start_reason", entry.getValue()[1],
                    data.getFieldAsString("session_start_reason"));
            evidence.put(new JSONObject().put("case", entry.getKey())
                    .put("app_state", data.getField("app_state"))
                    .put("session_start_reason", data.getField("session_start_reason"))
                    .put("session_id", data.getTag("session_id"))
                    .put("view_id", data.getTag("view_id"))
                    .put("timestamp", data.getTimestamp()));
        }
    }

    private void write(String name, String contents) throws Exception {
        try (FileOutputStream output = new FileOutputStream(new File(context.getFilesDir(), name))) {
            output.write(contents.getBytes(StandardCharsets.UTF_8));
        }
    }
}
