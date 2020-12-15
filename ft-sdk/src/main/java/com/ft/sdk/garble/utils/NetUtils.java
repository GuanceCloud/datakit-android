package com.ft.sdk.garble.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.AsyncCallback;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.LinkedList;


/**
 * BY huangDianHua
 * DATE:2020-01-09 13:58
 * Description:
 */
public class NetUtils {
    public final static int NETWORK_NONE = 0;
    public final static int NETWORK_WIFI = 1;
    public final static int NETWORK_2G = 2;
    public final static int NETWORK_3G = 3;
    public final static int NETWORK_4G = 4;
    public final static int NETWORK_5G = 5;
    public final static int NETWORK_UNKNOWN = NETWORK_5G + 1;

    private static final String PUBLIC_IP_API = "http://ip-api.com/json";

    private static NetUtils netUtils;
    private boolean isListening;
    private TelephonyManager telephonyManager;
    private PhoneStatListener phoneStatListener;

    private long netUpRate = 0;
    private long netDownRate = 0;
    //上一次收到的总的字节数
    private long lastRx = 0;
    //上一次发送的总的字节数
    private long lastTx = 0;
    private boolean isRunNetMonitor = false;

    private NetStatusBean lastNetStatus;

    private NetUtils() {
    }

    private final Object lock = new Object();

    /**
     * 获取上一次请求之后，网络状态的一些数据
     *
     * @return
     */
    @Nullable
    public NetStatusBean getLastMonitorStatus() {
        synchronized (lock) {//取保多个 OKHttp 监听的时候，线程安全
            return lastNetStatus;
        }

    }

    public void setLastMonitorStatus(NetStatusBean netStatusBean) {
        synchronized (lock) {
            lastNetStatus = netStatusBean;
        }
    }


    public synchronized static NetUtils get() {
        if (netUtils == null) {
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
    @SuppressLint("MissingPermission")
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
        if (telephonyManager == null) {
            return NETWORK_NONE;
        }
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
            case TelephonyManager.NETWORK_TYPE_NR:
                return NETWORK_5G;
            default:
                return NETWORK_UNKNOWN;
        }
    }


    /**
     * 获取网络类型名称
     *
     * @return
     */
    public String getNetWorkStateName() {

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
            default:
                return "unknown";
        }


    }

    /**
     * 监听网络强度
     *
     * @param context
     */
    public void listenerSignal(Context context) {
        if (isListening) {
            return;
        }
        isListening = true;
        if (telephonyManager == null) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (phoneStatListener == null) {
                phoneStatListener = new PhoneStatListener();
            }
            telephonyManager.listen(phoneStatListener, PhoneStatListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }

    /**
     * 获得网络强度
     *
     * @return
     */
    public int getSignalStrength(Context context) {
        int state = getNetworkState(context);
        if (state == NETWORK_WIFI) {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            int wifi = wifiInfo.getRssi();
            if (wifi > -50 && wifi < 0) {
                return 3;
            } else if (wifi > -80 && wifi < -50) {
                return 2;
            } else if (wifi > -100 && wifi < -80) {
                return 1;
            } else {
                return 0;
            }
        } else if (state != NETWORK_NONE) {
            if (phoneStatListener != null) {
                return phoneStatListener.mSignalStrength;
            }
        }
        return 0;
    }

    private class PhoneStatListener extends PhoneStateListener {
        private int mSignalStrength;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mSignalStrength = signalStrength.getLevel();
                return;
            }
            mSignalStrength = signalStrength.getGsmSignalStrength();
        }
    }

    /**
     * 得到网络上行速度
     *
     * @return
     */
    public long getNetUpRate() {
        return netUpRate;
    }

    /**
     * 得到网络下行速度
     *
     * @return
     */
    public long getNetDownRate() {
        return netDownRate;
    }

    /**
     * 开始监听网络速率
     */
    public void startMonitorNetRate() {
        synchronized (this) {
            try {
                if (isRunNetMonitor) {
                    return;
                }
                new Thread(() -> {
                    try {
                        while (true) {
                            isRunNetMonitor = true;
                            try {
                                getNetSpeed();
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                    } finally {
                        isRunNetMonitor = false;
                    }
                }, "网速监听").start();
            } catch (Exception e) {
                isRunNetMonitor = false;
            }
        }
    }

    private DecimalFormat showFloatFormat = new DecimalFormat("0.00");

    /**
     * 应用启动时获取一次网速
     */
    public void initSpeed() {
        lastRx = TrafficStats.getTotalRxBytes();
        lastTx = TrafficStats.getTotalTxBytes();
        getNetSpeed();
    }

    /**
     * 获得网络速度（外部获取网速应该直接调用{@link NetUtils}）
     * 单位 字节
     *
     * @return
     */
    public void getNetSpeed() {
        long tempRx = TrafficStats.getTotalRxBytes();
        long tempTx = TrafficStats.getTotalTxBytes();
        long rxLast = tempRx - lastRx;
        long txLast = tempTx - lastTx;

        lastRx = tempRx;
        lastTx = tempTx;
        netDownRate = rxLast;
        netUpRate = txLast;
    }

    /**
     * 判断设备 是否使用代理上网
     */
    public String isWifiProxy(Context context) {
        final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        if (!TextUtils.isEmpty(proxyAddress) && (proxyPort != -1)) {
            return proxyAddress + ":" + proxyPort;
        } else {
            return Constants.UNKNOWN;
        }
    }

    /**
     * 判断网络是否在漫游
     *
     * @return
     */
    public boolean getRoamState() {
        ConnectivityManager manager = getConnectivityManager();
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return info.isRoaming();
        }
        return false;
    }

    /**
     * 获取 DNS 地址
     *
     * @param context
     * @return
     */
    public String[] getDnsFromConnectionManager(Context context) {
        LinkedList<String> dnsServers = new LinkedList<>();
        ConnectivityManager connectivityManager = getConnectivityManager();
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                for (Network network : connectivityManager.getAllNetworks()) {
                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                    if (networkInfo != null && networkInfo.getType() == activeNetworkInfo.getType()) {
                        LinkProperties lp = connectivityManager.getLinkProperties(network);
                        for (InetAddress address : lp.getDnsServers()) {
                            dnsServers.add(address.getHostAddress());
                        }
                    }
                }
            }
        }
        return dnsServers.isEmpty() ? new String[0] : dnsServers.toArray(new String[dnsServers.size()]);
    }

    /**
     * 获取 WI-FI 的ssID
     *
     * @return
     */
    public String getSSId() {
        String ssId = Constants.UNKNOWN;
        WifiManager manager = (WifiManager) FTApplication.getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                ssId = wifiInfo.getSSID();
                if (ssId.length() > 2 && ssId.charAt(0) == '"' && ssId.charAt(ssId.length() - 1) == '"') {
                    return ssId.substring(1, ssId.length() - 1);
                } else if (ssId.contains("<unknown ssid>")) {
                    ssId = Constants.UNKNOWN;
                }
            }
        }
        return ssId;
    }

    /**
     * 获得 WI-FI 的IP地址
     *
     * @return
     */
    public String getWifiIp() {
        String ip = Constants.UNKNOWN;
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
     * 获取当前设备的IP
     *
     * @return
     */
    public String getMobileIpAddress() {
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
                    e.printStackTrace();
                }
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return getWifiIp();
            } else {

            }
        }
        return null;
    }

    /**
     * 获取外网 IP 地址
     *
     * @param callback
     */
    public void getPublicIp(AsyncCallback callback) {
        ThreadPoolUtils.get().execute(() -> {
            ResponseData result = HttpBuilder.Builder()
                    .setUrl(PUBLIC_IP_API)
                    .setMethod(RequestMethod.GET)
                    .executeSync(ResponseData.class);
            if (callback != null) {
                callback.onResponse(result.getHttpCode(), result.getData());
            }
        });
    }


    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) FTApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
