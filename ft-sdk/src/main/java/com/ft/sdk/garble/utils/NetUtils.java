package com.ft.sdk.garble.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.text.DecimalFormat;


/**
 * BY huangDianHua
 * DATE:2020-01-09 13:58
 * Description:
 */
public class NetUtils {
    public static int NETWORK_NONE = 0;
    public static int NETWORK_WIFI = 1;
    public static int NETWORK_2G = 2;
    public static int NETWORK_3G = 3;
    public static int NETWORK_4G = 4;
    public static int NETWORK_MOBILE = 5;

    private static NetUtils netUtils;
    private boolean isListenering;
    private TelephonyManager telephonyManager;
    private PhoneStatListener phoneStatListener;
    private NetUtils(){}
    public synchronized static NetUtils get(){
        if(netUtils == null){
            netUtils = new NetUtils();
        }
        return netUtils;
    }


    /**
     * 获得网络类型
     *
     * @param context
     * @return
     */
    public int getNetworkState(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) {
            return NETWORK_NONE;
        }
        NetworkInfo activeNetInfo = cm.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORK_NONE;
        }
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK_WIFI;
                }
            }
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_2G;
            // 3G网络
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_3G;
            // 4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_4G;
            default:
                return NETWORK_MOBILE;
        }
    }

    /**
     * 监听网络强度
     * @param context
     */
    public void listenerSignal(Context context){
        if(isListenering){
            return;
        }
        isListenering = true;
        if(telephonyManager == null){
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (phoneStatListener == null) {
                phoneStatListener = new PhoneStatListener();
            }
            telephonyManager.listen(phoneStatListener,PhoneStatListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }

    /**
     * 获得网络强度
     * @return
     */
    public int getSignalStrength(){
        if(phoneStatListener != null){
            return phoneStatListener.mSignalStrength;
        }
        return 0;
    }

    private class PhoneStatListener extends PhoneStateListener{
        private int mSignalStrength;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                mSignalStrength = signalStrength.getLevel();
                return;
            }
            mSignalStrength = signalStrength.getGsmSignalStrength();
        }
    }

    //private long rxtxTotal =0;
    private DecimalFormat showFloatFormat =new DecimalFormat("0.00");

    /**
     * 获得网络速度(阻塞方法)
     * @return
     */
    public String getNetSpeed() {
        long rxtxTotal = TrafficStats.getTotalRxBytes()
                + TrafficStats.getTotalTxBytes();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long tempSum = TrafficStats.getTotalRxBytes()
                + TrafficStats.getTotalTxBytes();
        long rxtxLast = tempSum -rxtxTotal;
        double totalSpeed= rxtxLast *1000 /2000d;
        //rxtxTotal = tempSum;
        return showSpeed(totalSpeed);//设置显示当前网速
    }

    private String showSpeed(double speed) {
        String speedString;
        if (speed >=1048576d) {
            speedString =showFloatFormat.format(speed /1048576d) +"MB/s";
        }else {
            speedString =showFloatFormat.format(speed /1024d) +"KB/s";
        }
        return speedString;

    }
}
