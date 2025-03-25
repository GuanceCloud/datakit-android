package com.ft.sdk.sessionreplay;

import android.app.Activity;

public interface SessionReplayInternalCallback {
    /**
     * Retrieves the current activity, allowing clients to pass it when needed.
     * This is used by SessionReplayRecorder to register fragment lifecycle callbacks
     * that were missed because the client was initialized after the `Application.onCreate` phase.
     */
    Activity getCurrentActivity();
}