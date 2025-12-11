package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

/**
 * Application running state, used to distinguish the running state of the App by checking the application state flag
 */
public enum AppState {
    /**
     * Unknown state, marked as Unknown when not available
     */
    UNKNOWN,
    /**
     * Application startup
     */
    STARTUP,
    /**
     * Running
     */
    RUN,
    /**
     * Background
     */
    BACKGROUND;

    /**
     * Convert from string to corresponding AppState
     *
     * @param value Corresponding string of @{@link AppState}
     * @return Returns the current running state
     */
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

    /**
     * Used for line protocol parameter passing, all lowercase in line protocol
     *
     * @return Lowercase string: unknown, startup, run
     */
    @NonNull
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
