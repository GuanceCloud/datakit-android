package com.ft.sdk.sessionreplay.internal.async;

import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;

import java.util.Arrays;
import java.util.Objects;

public class ResourceRecordedDataQueueItem extends RecordedDataQueueItem {
    private final String identifier;
    private final String applicationId;
    private final byte[] resourceData;

    public ResourceRecordedDataQueueItem(
        RecordedQueuedItemContext recordedQueuedItemContext,
        String identifier,
        String applicationId,
        byte[] resourceData
    ) {
        super(recordedQueuedItemContext);
        this.identifier = identifier;
        this.applicationId = applicationId;
        this.resourceData = resourceData;
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

    public String getApplicationId() {
        return applicationId;
    }

    public byte[] getResourceData() {
        return resourceData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ResourceRecordedDataQueueItem that = (ResourceRecordedDataQueueItem) o;
        return identifier.equals(that.identifier) &&
               applicationId.equals(that.applicationId) &&
               Arrays.equals(resourceData, that.resourceData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), identifier, applicationId);
        result = 31 * result + Arrays.hashCode(resourceData);
        return result;
    }
}
