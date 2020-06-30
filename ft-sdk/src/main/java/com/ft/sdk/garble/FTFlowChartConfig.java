package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;

import java.util.UUID;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:26
 * Description:流程图配置类
 */
public class FTFlowChartConfig {
    private static FTFlowChartConfig ftFlowChartConfig;
    //标记流程图的唯一id
    private String flowUUID;
    //是否打开流程图
    @Deprecated
    private boolean openFlowChart;
    //上一次操作的时间
    public volatile long lastOpTime;
    //流程图日志上报功能
    private boolean eventFlowLog;

    private FTFlowChartConfig() {
    }

    public static FTFlowChartConfig get() {
        if (ftFlowChartConfig == null) {
            ftFlowChartConfig = new FTFlowChartConfig();
        }
        return ftFlowChartConfig;
    }

    public void initParams(FTSDKConfig ftsdkConfig) {
        lastOpTime = System.currentTimeMillis();
        openFlowChart = ftsdkConfig.isOpenFlowChart();
        eventFlowLog = ftsdkConfig.isEventFlowLog();
        createNewFlowUUid();
    }

    public void createNewFlowUUid() {
        flowUUID = UUID.randomUUID().toString();
    }

    public String getFlowUUID() {
        return flowUUID;
    }

    @Deprecated
    public boolean isOpenFlowChart() {
        return openFlowChart;
    }

    public boolean isEventFlowLog() {
        return eventFlowLog;
    }

    public void release() {
        ftFlowChartConfig = null;
    }
}
