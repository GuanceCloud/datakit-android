package com.ft.sdk;

/**
 *
 */

public enum DeviceMetricsMonitorType {


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
