package com.ft.sdk.sessionreplay.internal.recorder;

import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;

public interface Recorder {

    void registerCallbacks();

    void unregisterCallbacks();

    void stopProcessingRecords();

    void resumeRecorders();

    void stopRecorders();
    
    // 添加获取RecordedDataQueueHandler的方法 added by zzq
    RecordedDataQueueHandler getRecordedDataQueueHandler();
}
