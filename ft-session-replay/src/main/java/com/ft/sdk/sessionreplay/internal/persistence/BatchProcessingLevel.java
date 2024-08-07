package com.ft.sdk.sessionreplay.internal.persistence;

public enum BatchProcessingLevel {

    // Define enum constants with their associated values
    LOW(1),
    MEDIUM(10),
    HIGH(100);

    private final int maxBatchesPerUploadJob;

    // Enum constructor
    BatchProcessingLevel(int maxBatchesPerUploadJob) {
        this.maxBatchesPerUploadJob = maxBatchesPerUploadJob;
    }

    // Getter method for maxBatchesPerUploadJob
    public int getMaxBatchesPerUploadJob() {
        return maxBatchesPerUploadJob;
    }
}
