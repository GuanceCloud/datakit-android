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
 * Description: Listen for network connection status
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
     * Configure and initialize network type monitoring object
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
     * Initialize network status monitoring
     */
    private void initMonitor() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //For SDK version greater than 26, register network status change callback via registerDefaultNetworkCallback
                connectivityManager.registerDefaultNetworkCallback(this);
                fetchNetworkStateBean(connectivityManager);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //For SDK version greater than 21 and less than 26, register network status change callback via registerNetworkCallback
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                NetworkRequest request = builder.build();
                connectivityManager.registerNetworkCallback(request, this);
                fetchNetworkStateBean(connectivityManager);
            } else {
                //For SDK version less than 21, obtain network status changes via broadcast
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
        LogUtils.d(TAG, "Net Connected");
        SyncTaskManager.get().executeSyncPoll();
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        fetchNetworkStateBean(connectivityManager);
    }

    /**
     * Network change broadcast receiver class
     */
    class FTNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetUtils.isNetworkAvailable(application)) {
                LogUtils.d(TAG, "Net Connected");
                SyncTaskManager.get().executeSyncPoll();
            }
        }
    }

    /**
     * Determine whether the network is available
     */
    private void judgeNetState() {

    }

    private void unregister() {
        if (connectivityManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //For SDK version greater than 26, register network status change callback via registerDefaultNetworkCallback
            connectivityManager.unregisterNetworkCallback(this);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //For SDK version greater than 21 and less than 26, register network status change callback via registerNetworkCallback
            connectivityManager.unregisterNetworkCallback(this);
        } else {
            //For SDK version less than 21, obtain network status changes via broadcast
            //no use
            application.unregisterReceiver(networkReceiver);
        }
    }

    /**
     * Release object, used when {@link FTSdk#shutDown()}
     */
    static void release() {
        if (instance != null) {
            instance.unregister();
            instance = null;
        }
    }
}
