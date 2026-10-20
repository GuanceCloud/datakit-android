package com.ft;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDataStoreManager;
import com.ft.test.utils.LineProtocolData;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.*;

/** Exercises the Sample's actual Activity/server and checks persisted SDK fields. */
@RunWith(AndroidJUnit4.class)
public class NetworkSettledSampleTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private NetworkSettledTimingActivity activity;

    @Test public void sampleProducesNetworkSettledAndNegativeControls() throws Exception {
        String store = InstrumentationRegistry.getArguments().getString("store", "sqlite");
        JSONArray evidence = new JSONArray();
        instrumentation.runOnMainSync(() -> {
            FTAutoTrack.startApp((Application) instrumentation.getTargetContext().getApplicationContext());
            FTSdk.install(FTSDKConfig.builder("http://127.0.0.1:9").setAutoSync(false)
                    .setRemoteConfiguration(false).setEnableDataFilter(false)
                    .setUseFileDataStore("file".equals(store)));
            FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId("network-settled-sample-test")
                    .setSessionSamplingRate(100).setEnableTraceUserView(true)
                    .setEnableTraceUserResource(true));
        });
        try {
            for (String scenario : new String[]{NetworkSettledTimingActivity.INITIAL,
                    NetworkSettledTimingActivity.LATE_ONLY, NetworkSettledTimingActivity.LEAVE_EARLY}) {
                activity = (NetworkSettledTimingActivity) instrumentation.startActivitySync(
                        new Intent(instrumentation.getTargetContext(), NetworkSettledTimingActivity.class)
                                .putExtra(NetworkSettledTimingActivity.EXTRA_SCENARIO, scenario)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                instrumentation.waitForIdleSync();
                final String viewId = FTRUMInnerManager.get().getViewId();
                assertNotNull(viewId);
                instrumentation.runOnMainSync(() -> assertTrue(
                        ((TextView) activity.findViewById(R.id.network_settled_status))
                                .getText().toString().contains(viewId)));
                if (NetworkSettledTimingActivity.INITIAL.equals(scenario)) {
                    await("fast request completes", () -> resource(viewId, "/fast") != null);
                    assertNull("slow request still pending", resource(viewId, "/slow"));
                    LineProtocolData pending = view(viewId);
                    assertNotNull(pending);
                    assertNull("pending slow initial request suppresses the field",
                            pending.getField("network_settled_time"));
                }
                int count = NetworkSettledTimingActivity.INITIAL.equals(scenario) ? 3
                        : NetworkSettledTimingActivity.LATE_ONLY.equals(scenario) ? 1 : 2;
                await("all Resources retain original View ownership",
                        () -> resources(viewId).size() == count);
                await("local server and request threads stop", () -> {
                    for (Thread thread : Thread.getAllStackTraces().keySet()) {
                        if (thread.isAlive() && thread.getName().startsWith("ft-network-settled-")) return false;
                    }
                    return true;
                });
                if (NetworkSettledTimingActivity.LEAVE_EARLY.equals(scenario)) {
                    assertTrue("scenario actually left before slow completion", activity.isFinishing());
                } else {
                    instrumentation.runOnMainSync(activity::finish);
                    instrumentation.waitForIdleSync();
                }
                await("View summary exists", () -> view(viewId) != null);
                SystemClock.sleep(300);
                LineProtocolData view = view(viewId);
                long viewStart = Long.parseLong(view.getTimestamp());
                JSONArray requests = new JSONArray();
                for (LineProtocolData resource : resources(viewId)) {
                    String url = resource.getTagAsString("resource_url");
                    long offset = Long.parseLong(resource.getTimestamp()) - viewStart;
                    if (url.endsWith("/late")) assertTrue("late request outside 100 ms", offset >= 100_000_000L);
                    else assertTrue("initial request within 100 ms: " + offset,
                            offset >= 0 && offset < 100_000_000L);
                    requests.put(new JSONObject().put("url", url).put("start_offset_ns", offset)
                            .put("duration", resource.getField("duration")));
                }
                Object actual = view.getField("network_settled_time");
                if (NetworkSettledTimingActivity.INITIAL.equals(scenario)) {
                    assertNotNull("Sample must generate network_settled_time", actual);
                    LineProtocolData slow = resource(viewId, "/slow");
                    long expected = Long.parseLong(slow.getTimestamp())
                            + ((Number) slow.getField("duration")).longValue() - viewStart;
                    assertTrue("metric matches slow initial request rather than late request",
                            Math.abs(((Number) actual).longValue() - expected) < 20_000_000L);
                    assertTrue(((Number) actual).longValue() >= 1_200_000_000L);
                } else {
                    assertNull("negative control must omit the field", actual);
                }
                evidence.put(new JSONObject().put("scenario", scenario).put("view_id", viewId)
                        .put("network_settled_time", actual == null ? JSONObject.NULL : actual)
                        .put("resources", requests).put("passed", true));
            }
        } finally {
            StringBuilder payload = new StringBuilder();
            for (SyncData data : records()) payload.append(data.getDataString()).append('\n');
            write(store + "-payloads.txt", payload.toString());
            write(store + "-results.json", evidence.toString(2));
            instrumentation.runOnMainSync(() -> {
                if (activity != null) activity.finish();
                FTSdk.shutDown();
            });
        }
    }

    private List<SyncData> records() {
        return FTDataStoreManager.get().queryDataByDataByTypeLimitDesc(500, DataType.RUM_APP);
    }

    private LineProtocolData view(String id) {
        for (SyncData data : records()) {
            LineProtocolData event = new LineProtocolData(data.getDataString().trim());
            if ("view".equals(event.getMeasurement()) && id.equals(event.getTagAsString("view_id"))) return event;
        }
        return null;
    }

    private List<LineProtocolData> resources(String id) {
        List<LineProtocolData> result = new ArrayList<>();
        for (SyncData data : records()) {
            LineProtocolData event = new LineProtocolData(data.getDataString().trim());
            if ("resource".equals(event.getMeasurement()) && id.equals(event.getTagAsString("view_id"))) result.add(event);
        }
        return result;
    }

    private LineProtocolData resource(String id, String path) {
        for (LineProtocolData event : resources(id)) {
            if (event.getTagAsString("resource_url").endsWith(path)) return event;
        }
        return null;
    }

    private void await(String message, BooleanSupplier condition) {
        long deadline = SystemClock.elapsedRealtime() + 8000;
        while (!condition.getAsBoolean() && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(25);
        assertTrue(message, condition.getAsBoolean());
    }

    private void write(String name, String value) throws Exception {
        try (FileOutputStream stream = new FileOutputStream(
                new File(instrumentation.getTargetContext().getFilesDir(), "network-settled-" + name))) {
            stream.write(value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
