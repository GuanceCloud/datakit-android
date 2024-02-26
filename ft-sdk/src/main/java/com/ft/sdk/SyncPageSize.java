package com.ft.sdk;

/**
 * 同步使用数据条目数，越小代表设备资源占用较小，但是相对网络 IO 会提升
 * 相反，设备资源占用越大，但是网络 IO 消耗会降低
 */
public enum SyncPageSize {

    /**
     * 5 条
     */
    MINI(5),
    /**
     * 10 条
     */
    MEDIUM(10),
    /**
     * 50 条，
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
