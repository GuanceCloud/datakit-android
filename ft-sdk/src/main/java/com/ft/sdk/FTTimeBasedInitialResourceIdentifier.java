package com.ft.sdk;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

/** Selects Resources starting strictly before a configurable interval from View start. */
public final class FTTimeBasedInitialResourceIdentifier implements FTInitialResourceIdentifier {
    private final long thresholdNanos;

    /** Uses the default 100 millisecond window. */
    public FTTimeBasedInitialResourceIdentifier() {
        this(100);
    }

    /**
     * @param thresholdMilliseconds positive window in milliseconds; its upper boundary is excluded
     * @throws IllegalArgumentException if the window is not positive
     */
    public FTTimeBasedInitialResourceIdentifier(long thresholdMilliseconds) {
        if (thresholdMilliseconds <= 0) throw new IllegalArgumentException("thresholdMilliseconds must be positive");
        thresholdNanos = TimeUnit.MILLISECONDS.toNanos(thresholdMilliseconds);
    }

    /** Preserves the default rule's lack of a lower bound for original Resource timestamps. */
    @Override
    public boolean validate(@NonNull FTNetworkSettledResourceContext context) {
        return context.getResourceStartTimeNanos() - context.getViewStartTimeNanos() < thresholdNanos;
    }
}
