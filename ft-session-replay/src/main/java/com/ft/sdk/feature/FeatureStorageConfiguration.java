package com.ft.sdk.feature;

public class FeatureStorageConfiguration {

    private long maxItemSize;
    private int maxItemsPerBatch;
    private long maxBatchSize;
    private long oldBatchThreshold;

    public FeatureStorageConfiguration(long maxItemSize, int maxItemsPerBatch, long maxBatchSize, long oldBatchThreshold) {
        this.maxItemSize = maxItemSize;
        this.maxItemsPerBatch = maxItemsPerBatch;
        this.maxBatchSize = maxBatchSize;
        this.oldBatchThreshold = oldBatchThreshold;
    }

    public long getMaxItemSize() {
        return maxItemSize;
    }

    public int getMaxItemsPerBatch() {
        return maxItemsPerBatch;
    }

    public long getMaxBatchSize() {
        return maxBatchSize;
    }

    public long getOldBatchThreshold() {
        return oldBatchThreshold;
    }


    public static final FeatureStorageConfiguration DEFAULT = new FeatureStorageConfiguration(
            // 512 KB
            512L * 1024,
            500,
            // 4 MB
            4L * 1024 * 1024,
            // 18 hours
            18L * 60L * 60L * 1000L
    );

    public FeatureStorageConfiguration copyWith(long maxItemSize, int maxItemsPerBatch,
                                                int maxBatchSize, int oldBatchThreshold) {
        if (maxItemSize > -1) {
            this.maxItemSize = maxItemSize;
        }
        if (maxItemsPerBatch > -1) {
            this.maxItemsPerBatch = maxItemsPerBatch;
        }
        if (maxBatchSize > -1) {
            this.maxBatchSize = maxBatchSize;
        }
        if (oldBatchThreshold > -1) {
            this.oldBatchThreshold = oldBatchThreshold;
        }
        return this;
    }


}
