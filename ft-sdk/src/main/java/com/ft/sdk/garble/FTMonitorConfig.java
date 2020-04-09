package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.utils.LocationUtils;
import com.ft.sdk.garble.utils.NetUtils;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:26
 * Description:监控项配置
 */
public class FTMonitorConfig {
    private static FTMonitorConfig ftMonitorConfig;
    private int mMonitorType;
    private FTMonitorConfig(){ }
    public static FTMonitorConfig get(){
        if(ftMonitorConfig == null){
            ftMonitorConfig = new FTMonitorConfig();
        }
        return ftMonitorConfig;
    }
    public void initParams(FTSDKConfig ftsdkConfig){
        if(ftsdkConfig != null) {
            mMonitorType = ftsdkConfig.getMonitorType();
        }
        if(isMonitorType(MonitorType.ALL)){
            //开启网络监听
            NetUtils.get().listenerSignal(FTSdk.get().getApplication());
            //开始获取地理位置
            LocationUtils.get().setGeoKey(ftsdkConfig.getGeoKey());
            LocationUtils.get().setUseGeoKey(ftsdkConfig.isUseGeoKey());
            LocationUtils.get().startLocation(FTSdk.get().getApplication());
            //监听网络速度
            NetUtils.get().startMonitorNetRate();
            NetUtils.get().initSpeed();
        } else {
            if (isMonitorType(MonitorType.NETWORK)) {
                //开启网络监听
                NetUtils.get().listenerSignal(FTSdk.get().getApplication());
                //监听网络速度
                NetUtils.get().startMonitorNetRate();
                NetUtils.get().initSpeed();
            }
            if (isMonitorType(MonitorType.LOCATION)) {
                //获取地理位置
                LocationUtils.get().setGeoKey(ftsdkConfig.getGeoKey());
                LocationUtils.get().setUseGeoKey(ftsdkConfig.isUseGeoKey());
                LocationUtils.get().startLocation(FTSdk.get().getApplication());
            }
        }
    }

    /**
     * 判断某种类型的监控是否开启
     * @param monitorType
     * @return
     */
    public boolean isMonitorType(int monitorType){
        //未开启监控项
        if(mMonitorType == 0){
            return false;
        }
        //开启全部监控项
        if(mMonitorType == 1){
            return true;
        }

        //判断某一种监控项是否开启
        if((mMonitorType | monitorType) == mMonitorType){
            return true;
        }
        return false;
    }

    /**
     * 清楚当前监控配置项
     */
    public void clear(){
        ftMonitorConfig = null;
    }
}
