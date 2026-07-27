package com.ft;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Debug-only receiver that cold-starts the app from a background component.
 */
public class BackgroundLaunchReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.ft.action.SIMULATE_BACKGROUND_BROADCAST_LAUNCH";
    public static final String ACTION_SCHEDULE_JOB =
            "com.ft.action.SCHEDULE_BACKGROUND_LAUNCH_JOB";
    private static final String TAG = "BackgroundLaunchReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ActivityManager.RunningAppProcessInfo processInfo =
                new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(processInfo);

        String action = intent == null ? null : intent.getAction();
        Log.d(TAG, "background receiver launch, action=" + action
                + ", importance=" + processInfo.importance);

        if (ACTION_SCHEDULE_JOB.equals(action)) {
            BackgroundLaunchJobService.schedule(context);
        }
    }
}
