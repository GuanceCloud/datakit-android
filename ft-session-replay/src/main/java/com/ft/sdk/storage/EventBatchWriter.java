package com.ft.sdk.storage;

import com.ft.sdk.sessionreplay.internal.storage.EventType;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;

public interface EventBatchWriter {

    /**
     * @return the metadata of the current writeable batch
     */
    byte[] currentMetadata();

    /**
     * Writes the content of the event to the current available batch.
     *
     * @param event         the event to write (content + metadata)
     * @param batchMetadata the optional updated batch metadata
     * @param eventType     additional information about the event data
     * @return true if event was written, false otherwise.
     */
    boolean write(RawBatchEvent event, byte[] batchMetadata, EventType eventType);
}
