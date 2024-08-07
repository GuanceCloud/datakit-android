package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;

public abstract class RecordedDataQueueItem {
    private final RecordedQueuedItemContext recordedQueuedItemContext;
    private final Long creationTimeStampInNs = System.nanoTime();


    public RecordedDataQueueItem(RecordedQueuedItemContext recordedQueuedItemContext) {
        this.recordedQueuedItemContext = recordedQueuedItemContext;
    }

    public RecordedQueuedItemContext getRecordedQueuedItemContext() {
        return recordedQueuedItemContext;
    }

    public abstract boolean isValid();

    public abstract boolean isReady();

    public Long getCreationTimeStampInNs() {
        return creationTimeStampInNs;
    }
}
