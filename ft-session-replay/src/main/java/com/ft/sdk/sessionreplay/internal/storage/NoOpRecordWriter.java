package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;

public class NoOpRecordWriter implements RecordWriter {

    @Override
    public void write(EnrichedRecord record) {
        // no-op
    }
}
