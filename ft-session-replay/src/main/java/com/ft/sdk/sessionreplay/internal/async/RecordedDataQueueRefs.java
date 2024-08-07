package com.ft.sdk.sessionreplay.internal.async;

import android.os.Handler;
import android.os.Looper;

public class RecordedDataQueueRefs {
    private final RecordedDataQueueHandler recordedDataQueueHandler;
    private final Handler mainThreadHandler;

    // Constructor with default value for mainThreadHandler
    public RecordedDataQueueRefs(RecordedDataQueueHandler recordedDataQueueHandler) {
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    // Constructor with explicit initialization of mainThreadHandler
    public RecordedDataQueueRefs(RecordedDataQueueHandler recordedDataQueueHandler, Handler mainThreadHandler) {
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.mainThreadHandler = mainThreadHandler;
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

    // Method to try to consume item, posted on main thread handler
    public void tryToConsumeItem() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                recordedDataQueueHandler.tryToConsumeItems();
            }
        });
    }
}
