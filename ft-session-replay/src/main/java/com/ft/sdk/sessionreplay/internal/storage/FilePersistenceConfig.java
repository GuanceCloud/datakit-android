package com.ft.sdk.sessionreplay.internal.storage;

public class FilePersistenceConfig {

    private final long recentDelayMs;
    private final long maxBatchSize;
    private final long maxItemSize;
    private final int maxItemsPerBatch;
    private final long oldFileThreshold;
    private final long maxDiskSpace;
    private final long cleanupFrequencyThreshold;

    public static final long MAX_BATCH_SIZE = 4L * 1024 * 1024; // 4 MB
    public static final int MAX_ITEMS_PER_BATCH = 500;
    public static final long MAX_ITEM_SIZE = 512L * 1024; // 512 KB
    public static final long OLD_FILE_THRESHOLD = 18L * 60L * 60L * 1000L; // 18 hours
    public static final long MAX_DISK_SPACE = 128 * MAX_BATCH_SIZE; // 512 MB
    public static final long MAX_DELAY_BETWEEN_MESSAGES_MS = 5000L;
    public static final long CLEANUP_FREQUENCY_THRESHOLD_MS = 5000L; // 5s

    public FilePersistenceConfig() {
        this(MAX_DELAY_BETWEEN_MESSAGES_MS, MAX_BATCH_SIZE, MAX_ITEM_SIZE,
             MAX_ITEMS_PER_BATCH, OLD_FILE_THRESHOLD, MAX_DISK_SPACE,
             CLEANUP_FREQUENCY_THRESHOLD_MS);
    }

    public FilePersistenceConfig(long recentDelayMs, long maxBatchSize, long maxItemSize,
                                 int maxItemsPerBatch, long oldFileThreshold, long maxDiskSpace,
                                 long cleanupFrequencyThreshold) {
        this.recentDelayMs = recentDelayMs;
        this.maxBatchSize = maxBatchSize;
        this.maxItemSize = maxItemSize;
        this.maxItemsPerBatch = maxItemsPerBatch;
        this.oldFileThreshold = oldFileThreshold;
        this.maxDiskSpace = maxDiskSpace;
        this.cleanupFrequencyThreshold = cleanupFrequencyThreshold;
    }

    public long getRecentDelayMs() {
        return recentDelayMs;
    }

    public long getMaxBatchSize() {
        return maxBatchSize;
    }

    public long getMaxItemSize() {
        return maxItemSize;
    }

    public int getMaxItemsPerBatch() {
        return maxItemsPerBatch;
    }

    public long getOldFileThreshold() {
        return oldFileThreshold;
    }

    public long getMaxDiskSpace() {
        return maxDiskSpace;
    }

    public long getCleanupFrequencyThreshold() {
        return cleanupFrequencyThreshold;
    }

    @Override
    public String toString() {
        return "FilePersistenceConfig{" +
                "recentDelayMs=" + recentDelayMs +
                ", maxBatchSize=" + maxBatchSize +
                ", maxItemSize=" + maxItemSize +
                ", maxItemsPerBatch=" + maxItemsPerBatch +
                ", oldFileThreshold=" + oldFileThreshold +
                ", maxDiskSpace=" + maxDiskSpace +
                ", cleanupFrequencyThreshold=" + cleanupFrequencyThreshold +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilePersistenceConfig that = (FilePersistenceConfig) o;

        if (recentDelayMs != that.recentDelayMs) return false;
        if (maxBatchSize != that.maxBatchSize) return false;
        if (maxItemSize != that.maxItemSize) return false;
        if (maxItemsPerBatch != that.maxItemsPerBatch) return false;
        if (oldFileThreshold != that.oldFileThreshold) return false;
        if (maxDiskSpace != that.maxDiskSpace) return false;
        return cleanupFrequencyThreshold == that.cleanupFrequencyThreshold;
    }

    @Override
    public int hashCode() {
        int result = (int) (recentDelayMs ^ (recentDelayMs >>> 32));
        result = 31 * result + (int) (maxBatchSize ^ (maxBatchSize >>> 32));
        result = 31 * result + (int) (maxItemSize ^ (maxItemSize >>> 32));
        result = 31 * result + maxItemsPerBatch;
        result = 31 * result + (int) (oldFileThreshold ^ (oldFileThreshold >>> 32));
        result = 31 * result + (int) (maxDiskSpace ^ (maxDiskSpace >>> 32));
        result = 31 * result + (int) (cleanupFrequencyThreshold ^ (cleanupFrequencyThreshold >>> 32));
        return result;
    }
}
