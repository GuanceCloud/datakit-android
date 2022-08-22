package com.ft.sdk;

/**
 *
 */
public enum ErrorMonitorType {


    ALL(0xFFFFFFFF),
    BATTERY(1 << 1),
    MEMORY(1 << 2),
    CPU(1 << 3),
    NO_SET(0);
    private final int value;


    ErrorMonitorType(int value) {

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
