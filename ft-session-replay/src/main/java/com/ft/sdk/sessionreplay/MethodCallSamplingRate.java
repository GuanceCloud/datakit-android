package com.ft.sdk.sessionreplay;

public enum MethodCallSamplingRate {
    DEFAULT(0.1f),
    REDUCED(0.01f),
    RARE(0.001f);

    private final float rate;

    MethodCallSamplingRate(float rate) {
        this.rate = rate;
    }

    public float getRate() {
        return rate;
    }
}
