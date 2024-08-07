package com.ft.sdk.sessionreplay.internal.persistence;

public class DataUploadConfiguration {

    private final UploadFrequency frequency;
    private final int maxBatchesPerUploadJob;

    private final long minDelayMs;
    private final long maxDelayMs;
    private final long defaultDelayMs;

    private static final int MIN_DELAY_FACTOR = 1;
    private static final int MAX_DELAY_FACTOR = 10;
    private static final int DEFAULT_DELAY_FACTOR = 5;

    public DataUploadConfiguration(UploadFrequency frequency, int maxBatchesPerUploadJob) {
        this.frequency = frequency;
        this.maxBatchesPerUploadJob = maxBatchesPerUploadJob;
        this.minDelayMs = MIN_DELAY_FACTOR * frequency.getBaseStepMs();
        this.maxDelayMs = MAX_DELAY_FACTOR * frequency.getBaseStepMs();
        this.defaultDelayMs = DEFAULT_DELAY_FACTOR * frequency.getBaseStepMs();
    }

    public UploadFrequency getFrequency() {
        return frequency;
    }

    public int getMaxBatchesPerUploadJob() {
        return maxBatchesPerUploadJob;
    }

    public long getMinDelayMs() {
        return minDelayMs;
    }

    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    public long getDefaultDelayMs() {
        return defaultDelayMs;
    }
}
