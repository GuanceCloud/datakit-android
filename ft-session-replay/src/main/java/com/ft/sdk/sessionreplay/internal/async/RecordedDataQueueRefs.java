package com.ft.sdk.sessionreplay.internal.async;

public class RecordedDataQueueRefs {
    private final RecordedDataQueueHandler recordedDataQueueHandler;

    // Constructor with default value for mainThreadHandler
    public RecordedDataQueueRefs(RecordedDataQueueHandler recordedDataQueueHandler) {
        this.recordedDataQueueHandler = recordedDataQueueHandler;
    }

    // Nullable field for recordedDataQueueItem
    private SnapshotRecordedDataQueueItem recordedDataQueueItem;

    // Setter method for recordedDataQueueItem
    public void setRecordedDataQueueItem(SnapshotRecordedDataQueueItem recordedDataQueueItem) {
        this.recordedDataQueueItem = recordedDataQueueItem;
    }

    // Method to increment pending jobs
    public void incrementPendingJobs() {
        if (recordedDataQueueItem != null) {
            recordedDataQueueItem.incrementPendingJobs();
        }
    }

    // Method to decrement pending jobs
    public void decrementPendingJobs() {
        if (recordedDataQueueItem != null) {
            recordedDataQueueItem.decrementPendingJobs();
        }
    }

    public void tryToConsumeItem() {
//        System.out.println("[FT-SDK] tryToConsumeItems from resource");
        recordedDataQueueHandler.tryToConsumeItems();
    }
}
