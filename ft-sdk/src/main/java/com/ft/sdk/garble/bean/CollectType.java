package com.ft.sdk.garble.bean;

/**
 * Sampling rate hit
 */
public enum CollectType {
    /**
     * Sampling rate hit
     */
    COLLECT_BY_SAMPLE,
    /**
     * Error sampling rate hit
     */
    COLLECT_BY_ERROR_SAMPLE,
    /**
     * Not collected
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
