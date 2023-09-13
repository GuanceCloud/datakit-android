package com.ft.sdk.garble.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ft.sdk.FTApplication;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


/**
 * BY huangDianHua
 * DATE:2020-01-09 13:58
 * Description: 网络信息相关 IP，网络类型等
 */
public class NetUtils {
    private final static String TAG = Constants.LOG_TAG_PREFIX + "NetUtils";
    public final static int NETWORK_NONE = -1;
    public final static int NETWORK_UNKNOWN = 0;
    public final static int NETWORK_WIFI = 1;
    public final static int NETWORK_2G = 2;
    public final static int NETWORK_3G = 3;
    public final static int NETWORK_4G = 4;
    public final static int NETWORK_5G = 5;

    /**
     * 检查网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }


    /**
     * 获得网络类型
     * <p>
     * {@link  Manifest.permission#READ_PHONE_STATE}
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static int getNetworkState(Context context) {
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
        if (telephonyManager == null) {
            return NETWORK_NONE;
        }
        int networkType = NETWORK_UNKNOWN;

        try {
            if (Utils.hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                networkType = telephonyManager.getNetworkType();
            } else {
                LogUtils.e(TAG, "没有获得到 READ_PHONE_STATE 权限无法获取运营商信息");
            }
        } catch (Exception ex) {
            LogUtils.e(TAG, Log.getStackTraceString(ex));
            return NETWORK_UNKNOWN;
        }

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case TelephonyManager.NETWORK_TYPE_GSM:
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
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return NETWORK_3G;
            // 4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return NETWORK_4G;
            case TelephonyManager.NETWORK_TYPE_NR:
                return NETWORK_5G;
            default:
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return NETWORK_UNKNOWN;
        }
    }

    /**
     * 获取网络类型名称
     * {@link  Manifest.permission#READ_PHONE_STATE}
     *
     * @return
     */
    public static String getNetWorkStateName() {

        int type = getNetworkState(FTApplication.getApplication());
        switch (type) {
            case NETWORK_NONE:
                return "unreachable";
            case NETWORK_2G:
                return "2G";
            case NETWORK_3G:
                return "3G";
            case NETWORK_4G:
                return "4G";
            case NETWORK_5G:
                return "5G";
            case NETWORK_WIFI:
                return "wifi";
            default:
                return "unknown";
        }


    }

    /**
     * 获得 WI-FI 的IP地址
     *
     * @return
     */
    public static String getWifiIp() {
        String ip = null;
        WifiManager manager = (WifiManager) FTApplication.getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                int ipAsInt = wifiInfo.getIpAddress();
                if (ipAsInt != 0) {
                    ip = (ipAsInt & 0xFF) + "." + ((ipAsInt >> 8) & 0xFF) + "." + ((ipAsInt >> 16) & 0xFF) + "." + ((ipAsInt >> 24) & 0xFF);
                }
            }
        }
        return ip;
    }

    /**
     * 获取当前设备的IP，处于公网会显示公网 IP，局域网中会显示局域网 IP
     *
     * @return 返回 8.8.8.8 格式的字符
     */
    public static String getMobileIpAddress() {
        NetworkInfo networkInfo = ((ConnectivityManager) FTApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumeration = intf.getInetAddresses(); enumeration.hasMoreElements(); ) {
                            InetAddress inetAddress = enumeration.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, Log.getStackTraceString(e));

                }
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return getWifiIp();
            }
        }
        return null;
    }
}
