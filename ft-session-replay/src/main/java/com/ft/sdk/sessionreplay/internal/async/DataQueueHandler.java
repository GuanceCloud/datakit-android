package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;

import java.util.List;

public interface DataQueueHandler {
    ResourceRecordedDataQueueItem addResourceItem(
        String identifier,
        byte[] resourceData
    );

    default ResourceRecordedDataQueueItem addResourceItem(
            String identifier,
            byte[] resourceData,
            RecordedQueuedItemContext recordedQueuedItemContext
    ) {
        return addResourceItem(identifier, resourceData);
    }

    TouchEventRecordedDataQueueItem addTouchEventItem(
        List<MobileRecord> pointerInteractions
    );

    SnapshotRecordedDataQueueItem addSnapshotItem(SystemInformation systemInformation);

    void tryToConsumeItems();

    void clearAndStopProcessingQueue();
}
