package com.ft.sdk.garble.manager;

import androidx.annotation.NonNull;

enum AppState {
    UNKNOWN,
    START,
    RUNNING;

    public static AppState getValueFrom(String value) {
        AppState[] states = AppState.values();
        for (int i = 0; i < AppState.values().length; i++) {
            AppState state = states[i];
            if (state.toString().equals(value)) {
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
