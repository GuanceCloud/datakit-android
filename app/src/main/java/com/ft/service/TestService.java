package com.ft.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ft.BuildConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.utils.RequestUtils;

import okhttp3.Request;

/**
 * Test service, okhttp network request tracking situation, {@link FTSDKConfig#isOnlySupportMainProcess()}
 */
public class TestService extends Service {
    /**
     * Send and receive using Action Filter
     */
    public static final String ACTION_MESSAGE = "com.ft.action.MESSAGE";
    private static final String TAG = "TestService";
    /**
     * SDK installation status
     */
    public static final String INSTALLED_STATE = "installed";

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialization operation
        boolean sdkInstalled = FTSdk.get() != null;

        Intent intent = new Intent(ACTION_MESSAGE);
        intent.putExtra(INSTALLED_STATE, sdkInstalled);
        // Cross-process sending
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = RequestUtils.requestUrl(BuildConfig.TRACE_URL);
                LogUtils.d(TAG, "header=" + request.headers().toString());
            }
        }).start();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Service doesn't need binding, return null
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // After ending the process, ProcessConfigTest can perform the next Application.onCreate re-call
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}






