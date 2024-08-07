package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;

public interface RecordWriter {
    /**
     * Writes the record to disk.
     * @param record to write
     */
    void write(EnrichedRecord record);
}
