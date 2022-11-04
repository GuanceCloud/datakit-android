package com.ft.sdk;

/**
 * 监测周期，单位毫秒
 *
 * @author Brandon
 */
public enum DetectFrequency {

    /**
     *
     */
    DEFAULT(500),

    /**
     *
     */
    FREQUENT(100),

    /**
     *
     */
    RARE(1000);

    private final long value;

    /**
     *
     * @param periodTime 事件周期
     */
    DetectFrequency(long periodTime) {

        this.value = periodTime;
    }

    /**
     *
     * @return  获取监测周期，单位 毫秒
     */
    public long getValue() {
        return value;
    }
}

