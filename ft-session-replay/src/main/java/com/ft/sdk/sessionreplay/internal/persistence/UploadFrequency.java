package com.ft.sdk.sessionreplay.internal.persistence;

public enum UploadFrequency {

    // Define enum constants with their associated values
    FREQUENT(500L),
    AVERAGE(2000L),
    RARE(5000L);

    private final long baseStepMs;

    // Enum constructor
    UploadFrequency(long baseStepMs) {
        this.baseStepMs = baseStepMs;
    }

    // Getter method for baseStepMs
    public long getBaseStepMs() {
        return baseStepMs;
    }
}
