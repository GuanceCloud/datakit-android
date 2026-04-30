package com.ft.sdk.sessionreplay.utils;

/**
 * Callback used by mappers to report asynchronous resource processing.
 */
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
