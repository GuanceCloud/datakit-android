package com.ft.sdk.garble.interfaces;

/**
 * author: huangDianHua
 * time: 2020/9/28 10:35:20
 * description:
 */
public interface UiPerfMonitorConfig {
    //配置的卡顿时间划分等级
    int TIME_WARNING_LEVEL_1 = 1000;
    int TIME_WARNING_LEVEL_2 = 1500;

    //监控状态
    int UI_PERF_MONITOR_STOP = 0x01;
    int UI_PERF_MONITOR_START = 0x02;

    //卡顿等级，可以根据不同的需求配置不同的卡顿等级
    int UI_PERF_LEVEL_1 = 0x01;
    int UI_PERF_LEVEL_2 = 0x02;
}
