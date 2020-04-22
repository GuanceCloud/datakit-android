package com.ft.sdk;

import android.os.SystemClock;

import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.SyncDataManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;

import java.net.HttpURLConnection;

/**
 * create: by huangDianHua
 * time: 2020/4/17 10:16:20
 * description: 监控入口
 */
public class FTMonitor {
    //轮训周期，默认10秒
    private int period = 10;
    //监控类型
    private int monitorType = 0;
    private boolean useGeoKey;
    private String geoKey;
    private String measurement;
    private static FTMonitor instance;

    private FTMonitor() {
    }

    public static FTMonitor get() {
        if (instance == null) {
            instance = new FTMonitor();
        }
        return instance;
    }

    public void setTcpStartTime() {
        NetUtils.get().tcpStartTime = System.currentTimeMillis();
    }

    public void setTcpEndTime() {
        NetUtils.get().tcpEndTime = System.currentTimeMillis();
    }

    public void setDnsStartTime() {
        NetUtils.get().dnsStartTime = System.currentTimeMillis();
    }

    public void setDnsEndTime() {
        NetUtils.get().dnsEndTime = System.currentTimeMillis();
    }

    public void setResponseStartTime() {
        NetUtils.get().responseStartTime = System.currentTimeMillis();
    }

    public void setResponseEndTime() {
        NetUtils.get().responseEndTime = System.currentTimeMillis();
    }

    public void setStartTime() {
        NetUtils.get().startTime = System.currentTimeMillis();
    }

    public void setEndTime() {
        NetUtils.get().endTime = System.currentTimeMillis();
    }

    public void setRequestCount() {
        NetUtils.get().requestCount += 1;
    }

    public void setRequestErrCount() {
        NetUtils.get().requestErrCount += 1;
    }

    public FTMonitor setPeriod(int period) {
        this.period = period;
        return this;
    }

    public FTMonitor setMonitorType(int monitorType) {
        FTMonitorConfig.get().setMonitorType(monitorType);
        this.monitorType = monitorType;
        return this;
    }

    public FTMonitor setUseGeoKey(boolean useGeoKey) {
        FTMonitorConfig.get().setUseGeoKey(useGeoKey);
        this.useGeoKey = useGeoKey;
        return this;
    }

    public FTMonitor setGeoKey(String geoKey) {
        FTMonitorConfig.get().setGeoKey(geoKey);
        this.geoKey = geoKey;
        return this;
    }

    public FTMonitor setMeasurement(String measurement) {
        this.measurement = measurement;
        return this;
    }

    public void start() {
        FTMonitorConfig.get().initParams();
        FTMonitorManager.install(period, measurement);
    }

    public void release() {
        FTMonitorConfig.get().release();
        FTMonitorManager ftMonitorManager = FTMonitorManager.get();
        if (ftMonitorManager != null) {
            ftMonitorManager.release();
        }
        instance = null;
    }
}

class FTMonitorManager {
    //轮训周期
    private int period = 0;
    private String measurement;
    private static FTMonitorManager instance;
    private MonitorThread mThread;

    private FTMonitorManager() {
    }

    public static FTMonitorManager install(int period, String measurement) {
        if (instance == null) {
            instance = new FTMonitorManager();
        }
        instance.period = period;
        instance.measurement = measurement;
        instance.stopMonitor();
        instance.startMonitor();
        return instance;
    }

    public static FTMonitorManager get() {
        return instance;
    }

    /**
     * 开启监控
     */
    public void startMonitor() {
        mThread = new MonitorThread("监控轮训", period, measurement);
        mThread.start();
        LogUtils.d("监控轮训线程启动...");
    }

    /**
     * 停止监控
     */
    private void stopMonitor() {
        if (mThread != null && mThread.isAlive()) {
            LogUtils.d("关闭监控轮训线程");
            mThread.interrupt();
            mThread = null;
        }
    }

    public void release() {
        stopMonitor();
        instance = null;
    }

    static class MonitorThread extends Thread {
        //轮训周期
        private int period;
        private String measurement;

        public MonitorThread(String name, int period, String measurement) {
            setName(name);
            this.period = period;
            this.measurement = measurement;
        }

        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    Thread.sleep(period * 1000);
                    try {
                        String body = SyncDataManager.getMonitorUploadData(measurement);
                        SyncDataManager.printUpdateData(body);
                        ResponseData result = HttpBuilder.Builder()
                                .setMethod(RequestMethod.POST)
                                .setBodyString(body).executeSync(ResponseData.class);
                        if (result.getHttpCode() != HttpURLConnection.HTTP_OK) {
                            LogUtils.d("监控轮训线程上传数据出错(message：" + result.getData() + ")");
                        }
                    } catch (Exception e) {
                        LogUtils.d("监控轮训线程执行错误(message：" + e.getMessage() + ")");
                    }
                }
            } catch (InterruptedException e) {
                LogUtils.d("监控轮训线程被关闭");
            }
        }
    }
}