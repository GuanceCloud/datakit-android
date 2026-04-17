package com.ft.sdk.sessionreplay.internal.recorder;

public class NoOpRecorder implements Recorder {

    @Override
    public void registerCallbacks() {
        // No-op
    }

    @Override
    public void unregisterCallbacks() {
        // No-op
    }

    @Override
    public void stopProcessingRecords() {
        // No-op
    }

    @Override
    public void resumeRecorders() {
        // No-op
    }

    @Override
    public void stopRecorders() {
        // No-op
    }
}
