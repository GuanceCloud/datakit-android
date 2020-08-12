package com.ft.sdk.garble;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.utils.CameraUtils;
import com.ft.sdk.garble.utils.FpsUtils;
import com.ft.sdk.garble.utils.LocationUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.SensorUtils;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:26
 * Description:监控项配置
 */
public class FTMonitorConfig {
    private static FTMonitorConfig ftMonitorConfig;
    private int monitorType;
    private String geoKey;
    private boolean useGeoKey;

    public void setMonitorType(int monitorType) {
        this.monitorType = monitorType;
    }

    public void setGeoKey(String geoKey) {
        this.geoKey = geoKey;
    }

    public void setUseGeoKey(boolean useGeoKey) {
        this.useGeoKey = useGeoKey;
    }

    public int getMonitorType() {
        return monitorType;
    }

    private FTMonitorConfig() {
    }

    public static FTMonitorConfig get() {
        if (ftMonitorConfig == null) {
            ftMonitorConfig = new FTMonitorConfig();
        }
        return ftMonitorConfig;
    }

    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        monitorType = ftsdkConfig.getMonitorType();
        geoKey = ftsdkConfig.getGeoKey();
        useGeoKey = ftsdkConfig.isUseGeoKey();
        initParams();
    }

    public void initParams() {
        //注册传感器监听
        SensorUtils.get().register();
        if (isMonitorType(MonitorType.ALL)) {
            //开启网络监听
            NetUtils.get().listenerSignal(FTApplication.getApplication());
            //开始获取地理位置
            LocationUtils.get().setGeoKey(geoKey);
            LocationUtils.get().setUseGeoKey(useGeoKey);
            LocationUtils.get().startListener();
            //监听网络速度
            NetUtils.get().startMonitorNetRate();
            NetUtils.get().initSpeed();
            FpsUtils.get().start();
            CameraUtils.get().start();
        } else {
            if (isMonitorType(MonitorType.NETWORK)) {
                //开启网络监听
                NetUtils.get().listenerSignal(FTApplication.getApplication());
                //监听网络速度
                NetUtils.get().startMonitorNetRate();
                NetUtils.get().initSpeed();
            }
            if (isMonitorType(MonitorType.LOCATION)) {
                //获取地理位置
                LocationUtils.get().setGeoKey(geoKey);
                LocationUtils.get().setUseGeoKey(useGeoKey);
                LocationUtils.get().startListener();
            }
            if(isMonitorType(MonitorType.FPS)){
                FpsUtils.get().start();
            }
            if(isMonitorType(MonitorType.SENSOR_TORCH)){
                CameraUtils.get().start();
            }
        }
    }

    /**
     * 判断某种类型的监控是否开启
     *
     * @param monitorType
     * @return
     */
    public boolean isMonitorType(int monitorType) {
        //未开启监控项
        if (this.monitorType == 0) {
            return false;
        }
        //判断某一种监控项是否开启
        if ((this.monitorType | monitorType) == this.monitorType) {
            return true;
        }
        return false;
    }

    /**
     * 清楚当前监控配置项
     */
    public void release() {
        SensorUtils.get().release();
        FpsUtils.get().release();
        CameraUtils.get().release();
        ftMonitorConfig = null;
    }
}
