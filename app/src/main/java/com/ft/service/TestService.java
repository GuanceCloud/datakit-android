package com.ft.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ft.BuildConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.utils.RequestUtils;

import okhttp3.Request;

/**
 * 测试 service ，okhttp网络请求追踪情况，{@link FTSDKConfig#isOnlySupportMainProcess()}
 */
public class TestService extends Service {
    private static final String TAG = "TestService";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化操作
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
        return null; // Service不需要绑定，返回null
    }


}






