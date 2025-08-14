package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.internal.async.ResourceRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.async.SnapshotRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.async.TouchEventRecordedDataQueueItem;

public interface Processor {
    void processResources(ResourceRecordedDataQueueItem item);

    void processScreenSnapshots(SnapshotRecordedDataQueueItem item);

    void processTouchEventsRecords(TouchEventRecordedDataQueueItem item);
}