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
     * Flutter {@linkplain https://github.com/GuanceCloud/datakit-flutter}
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


    public static ErrorType getValueFrom(String value) {
        ErrorType[] errorTypes = values();

        for (int i = 0; i < values().length; ++i) {
            ErrorType state = errorTypes[i];
            if (state.toString().toLowerCase().equals(value)) {
                return state;
            }
        }

        return NATIVE;
    }


    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
