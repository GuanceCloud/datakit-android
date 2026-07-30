package com.ft;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Debug-only foreground service used to simulate a real non-Activity process launch.
 *
 * Build the prodTest debug and AndroidTest APKs, then run
 * {@code app/scripts/verify_app_launch_type.sh} to verify this service together with Activity,
 * receiver, bound-service, provider, and JobService launches.
 */
public class BackgroundLaunchService extends Service {
    public static final String ACTION = "com.ft.action.SIMULATE_BACKGROUND_LAUNCH";
    public static final String EXTRA_KEEP_ALIVE_MS = "keep_alive_ms";
    public static final long DEFAULT_KEEP_ALIVE_MS = 5 * 60 * 1000L;
    private static final String TAG = "BackgroundLaunchService";
    private static final String CHANNEL_ID = "background_launch_debug";
    private static final int NOTIFICATION_ID = 1001;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "stop background launch simulation service");
            stopSelf();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        startAsForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long keepAliveMs = intent == null
                ? DEFAULT_KEEP_ALIVE_MS
                : intent.getLongExtra(EXTRA_KEEP_ALIVE_MS, DEFAULT_KEEP_ALIVE_MS);
        if (keepAliveMs <= 0) {
            keepAliveMs = DEFAULT_KEEP_ALIVE_MS;
        }

        ActivityManager.RunningAppProcessInfo processInfo =
                new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(processInfo);
        Log.d(TAG, "simulate background launch, action="
                + (intent == null ? null : intent.getAction())
                + ", importance=" + processInfo.importance
                + ", foreground="
                + (processInfo.importance
                <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                + ", keepAliveMs=" + keepAliveMs);

        handler.removeCallbacks(stopRunnable);
        handler.postDelayed(stopRunnable, keepAliveMs);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(stopRunnable);
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startAsForegroundService() {
        createNotificationChannel();
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Background launch debug")
                    .setContentText("Simulating a non-Activity process launch")
                    .setOngoing(true)
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Background launch debug")
                    .setContentText("Simulating a non-Activity process launch")
                    .setOngoing(true)
                    .build();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Background launch debug",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
