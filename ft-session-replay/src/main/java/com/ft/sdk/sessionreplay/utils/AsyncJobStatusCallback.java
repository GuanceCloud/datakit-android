package com.ft.sdk.sessionreplay.utils;

public interface AsyncJobStatusCallback {

    /**
     * Notifies that an async job has started.
     */
    void jobStarted();

    /**
     * Notifies that an async job has finished.
     */
    void jobFinished();
}
