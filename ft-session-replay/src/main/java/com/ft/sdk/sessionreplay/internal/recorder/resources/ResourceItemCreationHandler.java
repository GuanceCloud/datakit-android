package com.ft.sdk.sessionreplay.internal.recorder.resources;

import androidx.annotation.VisibleForTesting;

import com.ft.sdk.sessionreplay.internal.async.DataQueueHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResourceItemCreationHandler {
    private final DataQueueHandler recordedDataQueueHandler;
    private final String applicationId;

    // resource IDs previously sent in this session -
    // optimization to avoid sending the same resource multiple times
    // atm this set is unbounded but expected to use relatively little space (~80kb per 1k items)
    @VisibleForTesting
    final Set<String> resourceIdsSeen = Collections.synchronizedSet(new HashSet<>());

    public ResourceItemCreationHandler(DataQueueHandler recordedDataQueueHandler, String applicationId) {
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.applicationId = applicationId;
    }

    public void queueItem(String resourceId, byte[] resourceData) {
        if (!resourceIdsSeen.contains(resourceId)) {
            resourceIdsSeen.add(resourceId);
            recordedDataQueueHandler.addResourceItem(resourceId, applicationId, resourceData);
        }
    }
}
