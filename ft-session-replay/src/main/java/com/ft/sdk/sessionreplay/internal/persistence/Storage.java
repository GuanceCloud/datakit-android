package com.ft.sdk.sessionreplay.internal.persistence;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.internal.storage.RemovalReason;

public interface Storage {

    void writeCurrentBatch(SessionReplayContext datadogContext, boolean forceNewBatch, EventBatchWriterCallback callback);

    /**
     * Utility to read a batch, synchronously.
     */
    BatchData readNextBatch();

    /**
     * Utility to update the state of a batch, synchronously.
     * 
     * @param batchId       the id of the Batch to confirm
     * @param removalReason the reason why the batch is being removed
     * @param deleteBatch   if `true` the batch will be deleted, otherwise it will be marked as
     *                      not readable.
     */
    void confirmBatchRead(BatchId batchId, RemovalReason removalReason, boolean deleteBatch);

    /**
     * Removes all the files backed by this storage, synchronously.
     */
    void dropAll();
}
