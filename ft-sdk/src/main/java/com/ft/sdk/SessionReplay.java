package com.ft.sdk;


import android.content.Context;

public class SessionReplay {
    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param ftSessionReplayConfig Configuration to use for the feature.
     */
    static void enable(Object ftSessionReplayConfig, Context context) {
        SessionReplayBridge.enable(ftSessionReplayConfig, context);
    }
}
