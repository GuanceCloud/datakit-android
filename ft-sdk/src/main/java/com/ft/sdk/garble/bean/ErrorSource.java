package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

/**
 * Log source type
 *
 * @author Brandon
 */
public enum ErrorSource {
    /**
     * Native log type, generally refers to logcat
     */
    LOGGER("logger"),
    /**
     * Network type data source
     */
    NETWORK("network");


    private final String value;

    ErrorSource(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
