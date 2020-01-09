package com.ft.sdk.garble;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.utils.NetUtils;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:26
 * Description:
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
            NetUtils.get().listenerSignal(FTApplication.getApplication());
            NetUtils.get().getNetSpeed();
        }else if(isMonitorType(MonitorType.NETWORK)){
            NetUtils.get().listenerSignal(FTApplication.getApplication());
            NetUtils.get().getNetSpeed();
        }
    }

    public boolean isMonitorType(int monitorType){
        if(mMonitorType == 0){
            return false;
        }
        if((mMonitorType | monitorType) == mMonitorType){
            return true;
        }
        return false;
    }
}
