package com.ft.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSdk;

/**
 * Debug-only harness for validating fatal ANR recovery from a dedicated process.
 *
 * <p>{@link #ACTION_PREPARE} creates a persisted worker View. After the ready broadcast,
 * {@link #ACTION_FATAL_ANR} blocks the worker main thread so a manual API 30+ device run can
 * produce an {@code ApplicationExitInfo} ANR record without involving the app's main process.</p>
 */
public class HistoricalAnrWorkerService extends Service {
    public static final String ACTION_PREPARE =
            "com.ft.historical_anr.action.PREPARE";
    public static final String ACTION_FATAL_ANR =
            "com.ft.historical_anr.action.FATAL_ANR";
    public static final String ACTION_READY =
            "com.ft.historical_anr.action.READY";
    public static final String EXTRA_SDK_INSTALLED = "sdk_installed";
    public static final String VIEW_NAME = "HistoricalAnrWorker";

    public static Intent prepareIntent(Context context) {
        return new Intent(context, HistoricalAnrWorkerService.class)
                .setAction(ACTION_PREPARE);
    }

    public static Intent fatalAnrIntent(Context context) {
        return new Intent(context, HistoricalAnrWorkerService.class)
                .setAction(ACTION_FATAL_ANR);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_PREPARE.equals(action)) {
            prepareWorkerView();
        } else if (ACTION_FATAL_ANR.equals(action)) {
            prepareWorkerView();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    blockMainThread();
                }
            });
        }
        return START_NOT_STICKY;
    }

    private void prepareWorkerView() {
        boolean installed = FTSdk.get() != null;
        if (installed) {
            FTRUMGlobalManager.get().startView(VIEW_NAME);
        }
        Intent ready = new Intent(ACTION_READY)
                .setPackage(getPackageName())
                .putExtra(EXTRA_SDK_INSTALLED, installed);
        sendBroadcast(ready);
    }

    private void blockMainThread() {
        Object blocker = new Object();
        synchronized (blocker) {
            while (true) {
                try {
                    blocker.wait();
                } catch (InterruptedException ignored) {
                    // Keep the debug worker blocked until Android terminates the process.
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
