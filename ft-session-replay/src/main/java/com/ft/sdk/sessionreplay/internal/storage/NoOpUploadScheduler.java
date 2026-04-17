package com.ft.sdk.sessionreplay.internal.storage;

public class NoOpUploadScheduler implements UploadScheduler {
    @Override
    public void startScheduling() {
        // No operation
    }

    @Override
    public void stopScheduling() {
        // No operation
    }
}
