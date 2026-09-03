package com.ft.sdk.sessionreplay.internal.async;

/**
 * Marks the timestamp where the native recorder hands the visible surface to
 * an external recorder.
 */
public class ViewEndRecordedDataQueueItem extends RecordedDataQueueItem {

    private final long timestamp;

    public ViewEndRecordedDataQueueItem(long timestamp) {
        super(null);
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
