package com.ft.sdk;

/**
 * 错误检测类型，在 Error 传输类型中 {@link FTRUMGlobalManager#addError} 在调用的时候自动添加
 *
 * @author Brandon
 */
public enum ErrorMonitorType {


    /**
     * 所有类型 {@link #BATTERY,#MEMORY,#CPU }
     */
    ALL(0xFFFFFFFF),
    /**
     * 当前电手机的电池量,{@link com.ft.sdk.garble.utils.Constants#KEY_BATTERY_USE}
     */
    BATTERY(1 << 1),
    /**
     * 手机当前内存,{@link com.ft.sdk.garble.utils.Constants#KEY_MEMORY_USE}
     */
    MEMORY(1 << 2),
    /**
     * 当前 CPU 负载,{@link com.ft.sdk.garble.utils.Constants#KEY_CPU_USE}
     */
    CPU(1 << 3),
    /**
     * 未设置状态,用于监控未设置判断 {@link  FTMonitorManager}
     */
    NO_SET(0);
    private final int value;


    ErrorMonitorType(int value) {

        this.value = value;
    }

    /**
     * 检测错误数值
     *
     * @return
     */
    public int getValue() {
        return value;
    }
}
