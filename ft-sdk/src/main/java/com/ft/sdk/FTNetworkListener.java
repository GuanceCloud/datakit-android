package com.ft.sdk;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;

/**
 * BY huangDianHua
 * DATE:2020-01-19 17:35
 * Description: 监听网络连接状态
 */
 class FTNetworkListener {
    private final static String TAG = "FTNetworkListener";
    private static FTNetworkListener instance;
    private Application application;
    private FTNetWorkCallback networkCallback;
    private FTNetworkReceiver networkReceiver;
    ConnectivityManager connectivityManager;

    private FTNetworkListener() {
    }

    public synchronized static FTNetworkListener get() {
        if (instance == null) {
            instance = new FTNetworkListener();
        }
        return instance;
    }

    public void monitor() {
        if (networkCallback == null) {
            networkCallback = new FTNetWorkCallback();
        }
        if (application == null) {
            application = FTApplication.getApplication();
        }
        if (networkReceiver == null) {
            networkReceiver = new FTNetworkReceiver();
        }
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        initMonitor();
    }

    /**
     * 初始化网络状态监听
     */
    private void initMonitor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //SDK 版本大于 26 时通过registerDefaultNetworkCallback 注册网络状态变化回调
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //SDK 版本大于 21 小于 26 时通过 registerNetworkCallback 注册网络状态变化回调
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            NetworkRequest request = builder.build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        } else {
            //SDK 版本小于 21 时，通过广播来获得网络状态变化
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            application.registerReceiver(networkReceiver, intentFilter);
        }
    }

    /**
     * 网络状态变化回调类
     */
    public class FTNetWorkCallback extends ConnectivityManager.NetworkCallback {

        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            judgeNetState();
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
        }
    }

    /**
     * 网络变化广播接受类
     */
    public class FTNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            judgeNetState();
        }
    }

    /**
     * 判断网络是否可用
     */
    private void judgeNetState() {
        //大于 0 有网
        if (NetUtils.get().getNetworkState(application) > 0) {
            LogUtils.d(TAG, "Net->" + "网络已连接");
            SyncTaskManager.get().executeSyncPoll();
        }


    }

    public void release() {
        if (connectivityManager == null) {
            instance = null;
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //SDK 版本大于 26 时通过registerDefaultNetworkCallback 注册网络状态变化回调
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //SDK 版本大于 21 小于 26 时通过 registerNetworkCallback 注册网络状态变化回调
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } else {
            //SDK 版本小于 21 时，通过广播来获得网络状态变化
            application.unregisterReceiver(networkReceiver);
        }
        instance = null;
    }
}
