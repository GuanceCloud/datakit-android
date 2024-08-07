package com.ft.sdk.sessionreplay.internal.persistence;

import com.ft.sdk.sessionreplay.internal.storage.EventType;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.storage.EventBatchWriter;

public class NoOpEventBatchWriter implements EventBatchWriter {

    @Override
    public byte[] currentMetadata() {
        return null;
    }

    @Override
    public boolean write(RawBatchEvent event, byte[] batchMetadata, EventType eventType) {
        return true;
    }
}
