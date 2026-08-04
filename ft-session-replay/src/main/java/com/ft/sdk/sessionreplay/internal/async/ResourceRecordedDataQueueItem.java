package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceRecordedDataQueueItem extends RecordedDataQueueItem {
    private final String identifier;
    private final byte[] resourceData;
    private final WriteCompletionCallback writeCompletionCallback;
    private final AtomicBoolean writeCompletionNotified = new AtomicBoolean(false);

    public ResourceRecordedDataQueueItem(
        RecordedQueuedItemContext recordedQueuedItemContext,
        String identifier,
        byte[] resourceData
    ) {
        this(recordedQueuedItemContext, identifier, resourceData, null);
    }

    public ResourceRecordedDataQueueItem(
        RecordedQueuedItemContext recordedQueuedItemContext,
        String identifier,
        byte[] resourceData,
        WriteCompletionCallback writeCompletionCallback
    ) {
        super(recordedQueuedItemContext);
        this.identifier = identifier;
        this.resourceData = resourceData;
        this.writeCompletionCallback = writeCompletionCallback;
    }

    @Override
    public boolean isValid() {
        return resourceData != null && resourceData.length > 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    public String getIdentifier() {
        return identifier;
    }


    public byte[] getResourceData() {
        return resourceData;
    }

    public void onWriteFinished(boolean success) {
        if (writeCompletionCallback != null
                && writeCompletionNotified.compareAndSet(false, true)) {
            writeCompletionCallback.onComplete(success);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ResourceRecordedDataQueueItem that = (ResourceRecordedDataQueueItem) o;
        return identifier.equals(that.identifier) &&
               Arrays.equals(resourceData, that.resourceData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), identifier);
        result = 31 * result + Arrays.hashCode(resourceData);
        return result;
    }

    public interface WriteCompletionCallback {
        void onComplete(boolean success);
    }
}
