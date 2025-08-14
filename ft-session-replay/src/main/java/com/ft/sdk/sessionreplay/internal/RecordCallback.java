package com.ft.sdk.sessionreplay.internal;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;

public interface RecordCallback {

    /**
     * Notifies when a view session replay record was sent.
     *
     * @param record the EnrichedRecord that was sent
     */
    void onRecordForViewSent(EnrichedRecord record);
}
