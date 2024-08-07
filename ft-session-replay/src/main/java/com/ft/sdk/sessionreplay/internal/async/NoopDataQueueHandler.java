package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;

import java.util.List;

public class NoopDataQueueHandler implements DataQueueHandler {
    @Override
    public ResourceRecordedDataQueueItem addResourceItem(
        String identifier,
        String applicationId,
        byte[] resourceData
    ) {
        return null; // Return null as a no-operation implementation
    }

    @Override
    public TouchEventRecordedDataQueueItem addTouchEventItem(
        List<MobileRecord> pointerInteractions
    ) {
        return null; // Return null as a no-operation implementation
    }

    @Override
    public SnapshotRecordedDataQueueItem addSnapshotItem(
        SystemInformation systemInformation
    ) {
        return null; // Return null as a no-operation implementation
    }

    @Override
    public void tryToConsumeItems() {
        // No-operation implementation
    }

    @Override
    public void clearAndStopProcessingQueue() {
        // No-operation implementation
    }
}
