package com.ft.sdk.sessionreplay.internal;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;

public class NoOpRecordCallback implements RecordCallback {

    @Override
    public void onRecordForViewSent(EnrichedRecord record) {
        // No operation
    }
}
