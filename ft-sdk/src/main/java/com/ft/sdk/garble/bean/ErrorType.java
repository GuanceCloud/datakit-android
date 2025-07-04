package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

/**
 * @author Brandon
 */
public enum ErrorType {
    /**
     * C++/C type crash
     */
    NATIVE("native_crash"),
    /**
     * Java type crash
     */
    JAVA("java_crash"),
    /**
     * {<a href="https://github.com/GuanceCloud/datakit-flutter">Flutter SDK </a>}
     */
    FLUTTER("flutter_crash"),

    /**
     * Crash issues caused by ANR
     */
    ANR_CRASH("anr_crash"),

    /**
     * Triggered ANR events
     */
    ANR_ERROR("anr_error"),
    /**
     * Network request errors
     */
    NETWORK("network_error");


    /**
     * Error type, final character content displayed in line protocol
     */
    private final String value;

    ErrorType(String value) {
        this.value = value;
    }


    /**
     * Convert from character to corresponding ErrorType
     *
     * @param value native_crash, java_crash, flutter_crash, network_error
     * @return {@link ErrorType} type
     */
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
