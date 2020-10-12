package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;

import java.util.UUID;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:26
 * Description:流程图配置类
 */
public class FTFlowConfig {
    private static FTFlowConfig ftFlowChartConfig;
    //上一次操作的时间
    public volatile long lastOpTime;
    //流程图日志上报功能
    private boolean eventFlowLog;

    private FTFlowConfig() {
    }

    public static FTFlowConfig get() {
        if (ftFlowChartConfig == null) {
            ftFlowChartConfig = new FTFlowConfig();
        }
        return ftFlowChartConfig;
    }

    public void initParams(FTSDKConfig ftsdkConfig) {
        lastOpTime = System.currentTimeMillis();
        eventFlowLog = ftsdkConfig.isEventFlowLog();
    }


    public boolean isEventFlowLog() {
        return eventFlowLog;
    }

    public static void release() {
        ftFlowChartConfig = null;
    }
}
