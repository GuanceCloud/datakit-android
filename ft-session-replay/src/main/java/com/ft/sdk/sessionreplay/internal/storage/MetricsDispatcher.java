package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.sessionreplay.internal.persistence.BatchClosedMetadata;

import java.io.File;

public interface MetricsDispatcher {
    void sendBatchDeletedMetric(File batchFile, RemovalReason removalReason);

    void sendBatchClosedMetric(File batchFile, BatchClosedMetadata batchMetadata);
}
