package com.ft.sdk.sessionreplay;

/**
 * Preset sampling rates used for internal method-call instrumentation.
 */
public enum MethodCallSamplingRate {
    ALL(0.1f),
    HIGH(10.0f),
    MEDIUM(0.01f),
    LOW(0.01f),
    REDUCED(0.01f),
    RARE(0.001f);

    private final float rate;

    MethodCallSamplingRate(float rate) {
        this.rate = rate;
    }

    /**
     * Returns the numeric sampling rate for this preset.
     */
    public float getRate() {
        return rate;
    }
}
