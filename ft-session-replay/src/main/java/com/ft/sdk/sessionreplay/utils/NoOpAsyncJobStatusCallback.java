package com.ft.sdk.sessionreplay.utils;

public class NoOpAsyncJobStatusCallback implements AsyncJobStatusCallback {
    @Override
    public void jobStarted() {
        // No-op
    }

    @Override
    public void jobFinished() {
        // No-op
    }
}
