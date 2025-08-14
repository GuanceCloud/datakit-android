package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueRefs;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;

public class QueueStatusCallback implements AsyncJobStatusCallback {

    private final RecordedDataQueueRefs recordedDataQueueRefs;

    public QueueStatusCallback(RecordedDataQueueRefs recordedDataQueueRefs) {
        this.recordedDataQueueRefs = recordedDataQueueRefs;
    }

    @Override
    public void jobStarted() {
        recordedDataQueueRefs.incrementPendingJobs();
    }

    @Override
    public void jobFinished() {
        recordedDataQueueRefs.decrementPendingJobs();
        recordedDataQueueRefs.tryToConsumeItem();
    }
}