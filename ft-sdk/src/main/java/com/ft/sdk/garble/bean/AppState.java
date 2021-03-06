package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

public enum AppState {
    UNKNOWN,
    STARTUP,
    RUN;

    public static AppState getValueFrom(String value) {
        AppState[] states = AppState.values();
        for (int i = 0; i < AppState.values().length; i++) {
            AppState state = states[i];
            if (state.toString().toLowerCase().equals(value)) {
                return state;
            }
        }
        return UNKNOWN;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
