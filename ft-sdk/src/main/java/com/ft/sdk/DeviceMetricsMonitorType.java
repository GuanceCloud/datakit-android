package com.ft.sdk;

import java.util.HashMap;

/**
 *  设备检测类型，在 View 传输类型中 {@link FTRUMGlobalManager#startView(String, HashMap)}
 *  输出页面的电池、内存、CPU、FPS 等信息，通过观测云查看器 View 进行查看
 *   @author Brandon
 *
 */

public enum DeviceMetricsMonitorType {


    /**
     * 所有类型 {@link #BATTERY,#MEMORY,#CPU,#CPU}
     */
    ALL(0xFFFFFFFF),
    BATTERY(1 << 1),
    MEMORY(1 << 2),
    CPU(1 << 3),
    FPS(1 << 4),
    NO_SET(0);
    private final int value;


    DeviceMetricsMonitorType(int value) {

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
