package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

/**
 * 日志来源类型
 *
 * @author Brandon
 */
public enum ErrorSource {
    /**
     * 原生日志类型一般指
     */
    LOGGER("logger"),
    /**
     * 网络类型数据源
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
