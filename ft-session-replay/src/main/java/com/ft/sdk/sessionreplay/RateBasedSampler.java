package com.ft.sdk.sessionreplay;

import java.security.SecureRandom;
import java.util.Random;

public class RateBasedSampler implements Sampler {

    private final Supplier<Float> sampleRateProvider;

    public RateBasedSampler(float sampleRate) {
        this.sampleRateProvider = new Supplier<Float>() {
            @Override
            public Float get() {
                return sampleRate;
            }
        };
    }

    private final Random random = new SecureRandom();

    @Override
    public boolean sample() {
        float sampleRate = getSampleRate();
        return sampleRate != 0f && (sampleRate == 1f || random.nextFloat() * 100 <= sampleRate * 100);
    }

    @Override
    public Float getSampleRate() {
        float rawSampleRate = sampleRateProvider.get();
        if (rawSampleRate < 0f) {
            System.out.println("Sample rate value provided " + rawSampleRate + " is below 0, setting it to 0.");
            return 0f;
        } else if (rawSampleRate > 1f) {
            System.out.println("Sample rate value provided " + rawSampleRate + " is above 1, setting it to 1.");
            return 1f;
        } else {
            return rawSampleRate;
        }
    }

}
