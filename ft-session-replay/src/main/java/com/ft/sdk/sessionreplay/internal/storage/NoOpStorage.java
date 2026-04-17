package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.internal.persistence.BatchData;
import com.ft.sdk.sessionreplay.internal.persistence.BatchId;
import com.ft.sdk.sessionreplay.internal.persistence.EventBatchWriterCallback;
import com.ft.sdk.sessionreplay.internal.persistence.Storage;

public class NoOpStorage implements Storage {
    @Override
    public void writeCurrentBatch(SessionReplayContext datadogContext, boolean forceNewBatch, EventBatchWriterCallback callback) {

    }

    @Override
    public BatchData readNextBatch() {
        return null;
    }

    @Override
    public void confirmBatchRead(BatchId batchId, RemovalReason removalReason, boolean deleteBatch) {

    }

    @Override
    public void dropAll() {

    }
}
