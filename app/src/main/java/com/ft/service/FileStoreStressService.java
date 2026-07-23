package com.ft.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ft.utils.FileStoreStressScenario;

/**
 * Runs the FileStore stress scenario in a non-main process.
 */
public class FileStoreStressService extends Service {
    private volatile boolean running;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, FileStoreStressService.class);
        intent.putExtra(FileStoreStressScenario.EXTRA_WORKERS,
                FileStoreStressScenario.DEFAULT_WORKERS);
        intent.putExtra(FileStoreStressScenario.EXTRA_EVENTS_PER_WORKER,
                FileStoreStressScenario.DEFAULT_EVENTS_PER_WORKER);
        intent.putExtra(FileStoreStressScenario.EXTRA_PAYLOAD_BYTES,
                FileStoreStressScenario.DEFAULT_PAYLOAD_BYTES);
        return intent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (running) {
            Log.w(FileStoreStressScenario.TAG, "remote stress is already running");
            return START_NOT_STICKY;
        }

        int workers = getIntExtra(intent, FileStoreStressScenario.EXTRA_WORKERS,
                FileStoreStressScenario.DEFAULT_WORKERS);
        int eventsPerWorker = getIntExtra(intent, FileStoreStressScenario.EXTRA_EVENTS_PER_WORKER,
                FileStoreStressScenario.DEFAULT_EVENTS_PER_WORKER);
        int payloadBytes = getIntExtra(intent, FileStoreStressScenario.EXTRA_PAYLOAD_BYTES,
                FileStoreStressScenario.DEFAULT_PAYLOAD_BYTES);

        running = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileStoreStressScenario.runBlocking(getApplicationContext(),
                            FileStoreStressScenario.ROLE_REMOTE, workers, eventsPerWorker,
                            payloadBytes);
                } finally {
                    running = false;
                    stopSelf(startId);
                }
            }
        }, "FTFileStoreStressService");
        thread.start();

        return START_NOT_STICKY;
    }

    private int getIntExtra(Intent intent, String name, int defaultValue) {
        if (intent == null) {
            return defaultValue;
        }
        return intent.getIntExtra(name, defaultValue);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
