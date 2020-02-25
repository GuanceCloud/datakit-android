package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.utils.LocationUtils;
import com.ft.sdk.garble.utils.NetUtils;

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
    private boolean openFlowChart;
    //图标类型，产品代号
    private String flowProduct;
    //上一次操作的时间
    public long lastOpTime;
    private FTFlowChartConfig(){ }
    public static FTFlowChartConfig get(){
        if(ftFlowChartConfig == null){
            ftFlowChartConfig = new FTFlowChartConfig();
        }
        return ftFlowChartConfig;
    }
    public void initParams(FTSDKConfig ftsdkConfig){
        openFlowChart = ftsdkConfig.isOpenFlowChart();
        flowProduct = ftsdkConfig.getFlowProduct();
        createNewFlowUUid();
    }

    public void createNewFlowUUid(){
        flowUUID = UUID.randomUUID().toString();
    }

    public String getFlowUUID(){
        return flowUUID;
    }

    public boolean isOpenFlowChart() {
        return openFlowChart;
    }

    public String getFlowProduct() {
        return flowProduct;
    }
}
