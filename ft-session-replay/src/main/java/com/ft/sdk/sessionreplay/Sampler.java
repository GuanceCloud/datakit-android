package com.ft.sdk.sessionreplay;

public interface Sampler {

    boolean sample();

    Float getSampleRate();
}