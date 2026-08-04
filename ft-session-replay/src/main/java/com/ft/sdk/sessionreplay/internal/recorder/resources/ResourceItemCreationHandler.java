package com.ft.sdk.sessionreplay.internal.recorder.resources;

import com.ft.sdk.sessionreplay.internal.async.DataQueueHandler;
import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;

public class ResourceItemCreationHandler {
    private final DataQueueHandler recordedDataQueueHandler;

    public ResourceItemCreationHandler(DataQueueHandler recordedDataQueueHandler) {
        this.recordedDataQueueHandler = recordedDataQueueHandler;
    }

    public void queueItem(String resourceId, byte[] resourceData) {
        recordedDataQueueHandler.addResourceItem(resourceId, resourceData);
    }

    public void queueItem(String resourceId, byte[] resourceData,
                          RecordedQueuedItemContext recordedQueuedItemContext) {
        recordedDataQueueHandler.addResourceItem(
                resourceId,
                resourceData,
                recordedQueuedItemContext
        );
    }
}
