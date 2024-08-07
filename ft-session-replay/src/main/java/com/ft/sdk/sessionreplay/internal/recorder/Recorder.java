package com.ft.sdk.sessionreplay.internal.recorder;

public interface Recorder {

    void registerCallbacks();

    void unregisterCallbacks();

    void stopProcessingRecords();

    void resumeRecorders();

    void stopRecorders();
}
