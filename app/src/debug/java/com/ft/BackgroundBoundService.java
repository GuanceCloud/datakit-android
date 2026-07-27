package com.ft;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Debug-only bound service used to verify process-importance inheritance from a foreground client.
 */
public class BackgroundBoundService extends Service {
    private static final String TAG = "BackgroundBoundService";
    private final IBinder binder = new Binder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        ActivityManager.RunningAppProcessInfo processInfo =
                new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(processInfo);
        Log.d(TAG, "bound service launch, importance=" + processInfo.importance);
        return binder;
    }
}
