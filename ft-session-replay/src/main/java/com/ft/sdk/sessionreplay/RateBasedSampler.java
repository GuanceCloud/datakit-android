package com.ft.sdk.sessionreplay;

import java.security.SecureRandom;
import java.util.Random;

public class RateBasedSampler implements Sampler {

    private final Supplier<Float> sampleRateProvider;

    public RateBasedSampler(float sampleRate) {
        this.sampleRateProvider = () -> sampleRate;
    }

    public RateBasedSampler(double sampleRate) {
        this((float) sampleRate);
    }

    private final Random random = new SecureRandom();

    @Override
    public boolean sample() {
        float sampleRate = getSampleRate();
        return sampleRate == 0f ? false : sampleRate == 100f ? true : random.nextFloat() * 100 <= sampleRate;
    }

    @Override
    public Float getSampleRate() {
        float rawSampleRate = sampleRateProvider.get();
        if (rawSampleRate < 0f) {
            System.out.println("Sample rate value provided " + rawSampleRate + " is below 0, setting it to 0.");
            return 0f;
        } else if (rawSampleRate > 100f) {
            System.out.println("Sample rate value provided " + rawSampleRate + " is above 100, setting it to 100.");
            return 100f;
        } else {
            return rawSampleRate;
        }
    }

    public static final float SAMPLE_ALL_RATE = 100f;
}
