package com.ft.launchtest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Activity in the separate AndroidTest APK that starts target-app background components while
 * this caller remains in the foreground.
 */
public class BackgroundComponentCallerActivity extends Activity {
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_BIND_SERVICE = "bind_service";
    public static final String MODE_PROVIDER = "provider";
    public static final String MODE_BROADCAST = "broadcast";
    public static final String MODE_START_SERVICE = "start_service";
    private static final String TAG = "BackgroundCaller";

    private Cursor providerCursor;
    private boolean serviceBound;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "bound to " + name);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "disconnected from " + name);
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        Log.d(TAG, "start mode=" + mode);
        if (MODE_BIND_SERVICE.equals(mode)) {
            Intent serviceIntent = new Intent()
                    .setComponent(new ComponentName("com.ft", "com.ft.BackgroundBoundService"));
            serviceBound = bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else if (MODE_PROVIDER.equals(mode)) {
            providerCursor = getContentResolver().query(
                    Uri.parse("content://com.ft.background-launch/status"),
                    null,
                    null,
                    null,
                    null);
            Log.d(TAG, "provider cursor=" + providerCursor);
        } else if (MODE_BROADCAST.equals(mode)) {
            Intent broadcastIntent =
                    new Intent("com.ft.action.SIMULATE_BACKGROUND_BROADCAST_LAUNCH")
                            .setComponent(new ComponentName(
                                    "com.ft",
                                    "com.ft.BackgroundLaunchReceiver"));
            sendBroadcast(broadcastIntent);
        } else if (MODE_START_SERVICE.equals(mode)) {
            Intent serviceIntent =
                    new Intent("com.ft.action.SIMULATE_BACKGROUND_LAUNCH")
                            .setComponent(new ComponentName(
                                    "com.ft",
                                    "com.ft.BackgroundLaunchService"))
                            .putExtra("keep_alive_ms", 120000L);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            Log.e(TAG, "unknown mode=" + mode);
        }
    }

    @Override
    protected void onDestroy() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        if (providerCursor != null) {
            providerCursor.close();
            providerCursor = null;
        }
        super.onDestroy();
    }
}
