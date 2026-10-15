package com.ft.launchfixture;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.SystemClock;

import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.LaunchAcceptanceProbe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LaunchApplication extends Application {
    public static final JSONArray EVENTS = new JSONArray();

    @Override
    public void onCreate() {
        super.onCreate();
        FTSdk.install(FTSDKConfig.builder("http://127.0.0.1:9")
                .setAutoSync(false).setRemoteConfiguration(false).setEnableDataFilter(false));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId("launch-acceptance")
                .setSessionSamplingRate(100f).setEnableTraceUserAction(true)
                .setEnableTraceUserView(true));
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity a, Bundle b) { event("created"); }
            @Override public void onActivityStarted(Activity a) { event("foreground"); }
            @Override public void onActivityResumed(Activity a) { event("resumed"); }
            @Override public void onActivityPaused(Activity a) { event("paused"); }
            @Override public void onActivityStopped(Activity a) { event("background"); }
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) { }
            @Override public void onActivityDestroyed(Activity a) { event("destroyed"); }
        });
        event("application_created");
    }

    public static void event(String name) {
        try {
            JSONObject event = new JSONObject();
            event.put("event", name);
            event.put("uptime_ms", SystemClock.uptimeMillis());
            event.put("launch_type", LaunchAcceptanceProbe.type());
            EVENTS.put(event);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }
}
