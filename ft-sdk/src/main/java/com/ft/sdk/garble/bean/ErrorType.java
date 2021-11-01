package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

public enum ErrorType {
    NATIVE("native_crash"),
    JAVA("java_crash"),
    FLUTTER("flutter_crash"),
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
