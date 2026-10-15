package com.ft.sdk;

import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Test-only bridge; the production SDK does not expose or depend on this class. */
public final class LaunchAcceptanceProbe {
    private static final Pattern TYPE = Pattern.compile("app_launch_type=\"([^\"]+)\"");

    public static String type() {
        return FTAppStartCounter.get().getColdStartLaunchType();
    }

    public static void finishBackgroundForTest() {
        if (!"background".equals(type()) || FTActivityLifecycleCallbacks.isAppInForeground()) {
            throw new IllegalStateException("Explicit endpoint requires a background-only launch");
        }
        FTAppStartCounter.get().coldStart(System.nanoTime());
        FTAppStartCounter.get().coldStartUpload();
    }

    public static JSONObject snapshot() throws Exception {
        JSONObject report = new JSONObject();
        FTAppStartCounter counter = FTAppStartCounter.get();
        report.put("launch_type", type());
        report.put("foreground", FTActivityLifecycleCallbacks.isAppInForeground());
        report.put("first_draw_done", ((AtomicBoolean) field(counter, "firstDrawDone")).get());
        report.put("listener_registered", ((AtomicBoolean) field(counter, "firstDrawListenerRegistered")).get());
        report.put("application_ns", field(counter, "applicationOnCreateTimeLine"));
        report.put("first_activity_ns", field(counter, "firstActivityPreOnActivityTimeline"));
        report.put("sdk_version", BuildConfig.FT_SDK_VERSION);
        report.put("pre_application_duration_ns", field(counter, "applicationPreOnCreateDuration"));
        report.put("application_duration_ns", field(counter, "applicationOnCreateDuration"));
        report.put("first_frame_duration_ns", field(counter, "firsDrawnDuration"));
        JSONArray actions = new JSONArray();
        for (SyncData data : FTDBManager.get().queryDataByDescLimit(0)) {
            String line = data.getDataString();
            if (line == null || !line.contains("action_type=launch_cold")) continue;
            Matcher matcher = TYPE.matcher(line);
            JSONObject action = new JSONObject();
            action.put("launch_type", matcher.find() ? matcher.group(1) : "missing");
            action.put("line_protocol", line);
            actions.put(action);
        }
        report.put("cold_actions", actions);
        return report;
    }

    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }
}
