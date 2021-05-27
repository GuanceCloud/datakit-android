package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;


public enum ErrorSource {
    LOGGER ("logger"),
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
