package com.ft.sdk;

/**
 * Error detection type, automatically added when calling {@link FTRUMInnerManager#addError} in Error transmission type
 *
 * @author Brandon
 */
public enum ErrorMonitorType {

    /**
     * All types {@link #BATTERY,#MEMORY,#CPU }
     */
    ALL(0xFFFFFFFF),
    /**
     * Current battery level of the phone, TV devices do not support {@link com.ft.sdk.garble.utils.Constants#KEY_BATTERY_USE}
     */
    BATTERY(1 << 1),
    /**
     * Current memory of the phone, {@link com.ft.sdk.garble.utils.Constants#KEY_MEMORY_USE}
     */
    MEMORY(1 << 2),
    /**
     * Current CPU load, {@link com.ft.sdk.garble.utils.Constants#KEY_CPU_USE}
     */
    CPU(1 << 3);

    private final int value;
    /**
     * Unset state, used for monitoring unset judgment {@link FTMonitorManager}
     */
    public static final int NO_SET = 0;

    ErrorMonitorType(int value) {
        this.value = value;
    }

    /**
     * Detect error value
     *
     * @return
     */
    public int getValue() {
        return value;
    }
}
