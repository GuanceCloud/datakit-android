package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;

import java.util.List;

public interface DataQueueHandler {
    ResourceRecordedDataQueueItem addResourceItem(
        String identifier,
        String applicationId,
        byte[] resourceData
    );

    TouchEventRecordedDataQueueItem addTouchEventItem(
        List<MobileRecord> pointerInteractions
    );

    SnapshotRecordedDataQueueItem addSnapshotItem(SystemInformation systemInformation);

    void tryToConsumeItems();

    void clearAndStopProcessingQueue();
}
