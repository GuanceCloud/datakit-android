package com.ft.sdk;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

/** Validates a positive interaction-to-next-View interval below a configurable threshold. */
public final class FTTimeBasedInteractionIdentifier implements FTLastInteractionIdentifier {
    private final long thresholdNanos;

    /** Uses the default 3000 millisecond threshold. */
    public FTTimeBasedInteractionIdentifier() {
        this(3000);
    }

    /**
     * @param thresholdMilliseconds positive threshold in milliseconds; its upper boundary is excluded
     * @throws IllegalArgumentException if the threshold is not positive
     */
    public FTTimeBasedInteractionIdentifier(long thresholdMilliseconds) {
        if (thresholdMilliseconds <= 0) throw new IllegalArgumentException("thresholdMilliseconds must be positive");
        thresholdNanos = TimeUnit.MILLISECONDS.toNanos(thresholdMilliseconds);
    }

    @Override
    public boolean validate(@NonNull FTPreviousViewLastInteractionContext context) {
        long interval = context.getNextViewStartTimeNanos() - context.getInteractionTimeNanos();
        return interval > 0 && interval < thresholdNanos;
    }
}
