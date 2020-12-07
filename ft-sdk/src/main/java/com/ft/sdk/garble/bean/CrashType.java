package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

public enum CrashType {
    NATIVE("native_crash"),
    JAVA("java_crash");


    private final String value;

    CrashType(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
