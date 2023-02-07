package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

/**
 *
 * @author Brandon
 */
public enum ErrorType {
    /**
     *
     */
    NATIVE("native_crash"),
    /**
     *
     */
    JAVA("java_crash"),
    /**
     *  Flutter {@linkplain https://github.com/GuanceCloud/datakit-flutter}
     */
    FLUTTER("flutter_crash"),
    /**
     * 网络请求
     */
    NETWORK("network_error");


    private final String value;

    ErrorType(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
