package com.ft.sdk.sessionreplay.internal.persistence;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BatchData {

    private final BatchId id;
    private final List<RawBatchEvent> data;
    private final byte[] metadata;

    public BatchData(BatchId id, List<RawBatchEvent> data, byte[] metadata) {
        this.id = id;
        this.data = data;
        this.metadata = metadata;
    }

    public BatchData(BatchId id, List<RawBatchEvent> data) {
        this(id, data, null);
    }

    public BatchId getId() {
        return id;
    }

    public List<RawBatchEvent> getData() {
        return data;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchData batchData = (BatchData) o;
        return id.equals(batchData.id) &&
                data.equals(batchData.data) &&
                Objects.equals(metadata, batchData.metadata) &&
                (metadata != null ? Arrays.equals(metadata, batchData.metadata) : batchData.metadata == null);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, data);
        result = 31 * result + Arrays.hashCode(metadata);
        return result;
    }
}
