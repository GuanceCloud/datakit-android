package com.ft.sdk;

/**
 * Number of data entries used for synchronization, smaller means less device resource usage, but relative network IO will increase
 * Conversely, the larger the device resource usage, the lower the network IO consumption
 */
public enum SyncPageSize {

    /**
     * 5 entries
     */
    MINI(5),
    /**
     * 10 entries
     */
    MEDIUM(10),
    /**
     * 50 entries
     */
    LARGE(50);


    private final int value;

    SyncPageSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
