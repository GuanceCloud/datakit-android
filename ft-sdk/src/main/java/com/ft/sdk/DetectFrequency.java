package com.ft.sdk;

public enum DetectFrequency {
    DEFAULT(500),
    FREQUENT(100),
    RARE(1000);


    private final long value;

    DetectFrequency(long periodTime) {

        this.value = periodTime;
    }

    public long getValue() {
        return value;
    }
}

