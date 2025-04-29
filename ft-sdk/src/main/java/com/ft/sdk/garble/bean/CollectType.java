package com.ft.sdk.garble.bean;

public enum CollectType {
    /**
     * 采样率命中
     */
    COLLECT_BY_SAMPLE,
    /**
     * 错误采样率命中
     */
    COLLECT_BY_ERROR_SAMPLE,
    /**
     * 不采集
     */
    NOT_COLLECT;

    public String getValue() {
        return toString().toLowerCase();
    }

    public static CollectType fromValue(String value) {
        if (value == null) {
            return NOT_COLLECT;
        }
        for (CollectType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return NOT_COLLECT;

    }
}
