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
            LocationUtils.get().getCity();
        }else if(isMonitorType(MonitorType.NETWORK)){
            //开启网络监听
            NetUtils.get().listenerSignal(FTSdk.get().getApplication());
        } else if (isMonitorType(MonitorType.LOCATION)) {
            //获取地理位置
            LocationUtils.get().getCity();
        }
    }

    /**
     * 判断某种类型的监控是否开启
     * @param monitorType
     * @return
     */
    public boolean isMonitorType(int monitorType){
        if(mMonitorType == 0){
            return false;
        }
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
