package com.ft.sdk;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.NetworkStateBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;

/**
 * BY huangDianHua
 * DATE:2020-01-19 17:35
 * Description: 监听网络连接状态
 */
class FTNetworkListener extends ConnectivityManager.NetworkCallback {
    private final static String TAG = Constants.LOG_TAG_PREFIX + "FTNetworkListener";
    private static FTNetworkListener instance;
    private Application application;
    private FTNetworkReceiver networkReceiver;
    ConnectivityManager connectivityManager;

    private final NetworkStateBean networkStateBean = new NetworkStateBean();

    private FTNetworkListener() {
    }

    public synchronized static FTNetworkListener get() {
        if (instance == null) {
            instance = new FTNetworkListener();
        }
        return instance;
    }

    /**
     * 配置初始化网络类型监控对象
     */
    void monitor() {
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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //SDK 版本大于 26 时通过registerDefaultNetworkCallback 注册网络状态变化回调
                connectivityManager.registerDefaultNetworkCallback(this);
                fetchNetworkStateBean(connectivityManager);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //SDK 版本大于 21 小于 26 时通过 registerNetworkCallback 注册网络状态变化回调
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                NetworkRequest request = builder.build();
                connectivityManager.registerNetworkCallback(request, this);
                fetchNetworkStateBean(connectivityManager);
            } else {
                //SDK 版本小于 21 时，通过广播来获得网络状态变化
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                application.registerReceiver(networkReceiver, intentFilter);
            }
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    private void fetchNetworkStateBean(ConnectivityManager connectivityManager) {
        networkStateBean.setNetworkAvailable(NetUtils.isNetworkAvailable(connectivityManager));
        networkStateBean.setNetworkType(NetUtils.getNetWorkStateName(connectivityManager));
    }

    public NetworkStateBean getNetworkStateBean() {
        return networkStateBean;
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        networkStateBean.setNetworkNotAvailable();
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        judgeNetState();
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        fetchNetworkStateBean(connectivityManager);
    }

    /**
     * 网络变化广播接受类
     */
    class FTNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            judgeNetState();
        }
    }

    /**
     * 判断网络是否可用
     */
    private void judgeNetState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (networkStateBean.isNetworkAvailable()) {
                LogUtils.d(TAG, "Net Connected");
                SyncTaskManager.get().executeSyncPoll();
            }
        } else {
            //大于 0 有网
            if (NetUtils.isNetworkAvailable(application)) {
                LogUtils.d(TAG, "Net Connected");
                SyncTaskManager.get().executeSyncPoll();
            }
        }

    }

    private void unregister() {
        if (connectivityManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //SDK 版本大于 26 时通过registerDefaultNetworkCallback 注册网络状态变化回调
            connectivityManager.unregisterNetworkCallback(this);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //SDK 版本大于 21 小于 26 时通过 registerNetworkCallback 注册网络状态变化回调
            connectivityManager.unregisterNetworkCallback(this);
        } else {
            //SDK 版本小于 21 时，通过广播来获得网络状态变化
            //no use
            application.unregisterReceiver(networkReceiver);
        }
    }

    /**
     * 释放对象，{@link FTSdk#shutDown()} 时使用
     */
    static void release() {
        if (instance != null) {
            instance.unregister();
            instance = null;
        }
    }
}
