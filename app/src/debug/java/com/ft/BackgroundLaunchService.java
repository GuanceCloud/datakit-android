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
 * Verification:
 * 1. Build and install the prodTest debug APK.
 * 2. Reset the app and logcat:
 *    adb shell pm clear com.ft
 *    adb logcat -c
 * 3. Start this service while no Activity is visible:
 *    adb shell am start-foreground-service \
 *      -n com.ft/.BackgroundLaunchService \
 *      -a com.ft.action.SIMULATE_BACKGROUND_LAUNCH
 * 4. Open the first Activity before this service stops:
 *    adb shell am start -n com.ft/.DebugMainActivity
 * 5. Check logcat. The service log should show foreground=false, and the RUM cold
 *    launch action should contain app_launch_type="background".
 *
 * Use start-foreground-service instead of startservice on Android 8.0+ because normal
 * background service starts are blocked by the platform.
 */
public class BackgroundLaunchService extends Service {
    public static final String ACTION = "com.ft.action.SIMULATE_BACKGROUND_LAUNCH";
    private static final String TAG = "BackgroundLaunchService";
    private static final String CHANNEL_ID = "background_launch_debug";
    private static final int NOTIFICATION_ID = 1001;
    private static final long STOP_DELAY_MS = 60000L;

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
        ActivityManager.RunningAppProcessInfo processInfo =
                new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(processInfo);
        Log.d(TAG, "simulate background launch, action="
                + (intent == null ? null : intent.getAction())
                + ", importance=" + processInfo.importance
                + ", foreground="
                + (processInfo.importance
                <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND));

        handler.removeCallbacks(stopRunnable);
        handler.postDelayed(stopRunnable, STOP_DELAY_MS);
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
