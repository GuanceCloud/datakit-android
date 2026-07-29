package com.ft.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.Nullable;

/**
 * Starts a fresh SDK process after the worker ANR so ApplicationExitInfo recovery runs naturally.
 */
public class HistoricalAnrConsumerService extends Service {
    private static final long KEEP_ALIVE_MS = 10_000;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf(startId);
                Process.killProcess(Process.myPid());
            }
        }, KEEP_ALIVE_MS);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
