package com.ft.sdk;

import android.content.Intent;

import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.service.MonitorService;
import com.ft.sdk.garble.utils.NetUtils;

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
    private static FTMonitor instance;
    private Intent intent;

    private FTMonitor() {
        intent = new Intent(FTApplication.getApplication(), MonitorService.class);
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

    public void start() {
        FTMonitorConfig.get().initParams();
        if (intent == null) {
            intent = new Intent(FTApplication.getApplication(), MonitorService.class);
        }
        intent.putExtra("command", MonitorService.START_CMD);
        intent.putExtra("period", period);
        FTApplication.getApplication().startService(intent);
    }

    public void release() {
        if (intent != null) {
            intent.putExtra("command", MonitorService.STOP_CMD);
            FTApplication.getApplication().stopService(intent);
        }
        FTMonitorConfig.get().release();
        instance = null;
    }
}