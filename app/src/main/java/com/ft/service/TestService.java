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
 * 测试 service ，okhttp网络请求追踪情况，{@link FTSDKConfig#isOnlySupportMainProcess()}
 */
public class TestService extends Service {
    /**
     * 收发使用 Action Filter
     */
    public static final String ACTION_MESSAGE = "com.ft.action.MESSAGE";
    private static final String TAG = "TestService";
    /**
     * SDK 安装状态
     */
    public static final String INSTALLED_STATE = "installed";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化操作
        boolean sdkInstalled = FTSdk.get() != null;

        Intent intent = new Intent(ACTION_MESSAGE);
        intent.putExtra(INSTALLED_STATE, sdkInstalled);
        //跨进程发送
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
        return null; // Service不需要绑定，返回null
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //结束进程后， ProcessConfigTest，采能进行下一次 Application.onCreate 重新调用
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}






