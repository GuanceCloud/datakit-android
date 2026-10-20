package com.ft.sdk;

import androidx.annotation.NonNull;

/**
 * Immutable Resource registration facts. Times use the process-local {@link System#nanoTime()}
 * clock, not Unix time. URL, headers, status and custom properties are not included.
 */
public final class FTNetworkSettledResourceContext {
    private final String resourceId;
    private final long resourceStartTimeNanos;
    private final long viewStartTimeNanos;

    /** Creates a context with the original Resource and owning View timing origins. */
    public FTNetworkSettledResourceContext(@NonNull String resourceId, long resourceStartTimeNanos,
                                          long viewStartTimeNanos) {
        if (resourceId == null) throw new NullPointerException("resourceId");
        this.resourceId = resourceId;
        this.resourceStartTimeNanos = resourceStartTimeNanos;
        this.viewStartTimeNanos = viewStartTimeNanos;
    }

    /** Returns the Resource identifier used by this SDK registration. */
    @NonNull
    public String getResourceId() {
        return resourceId;
    }

    /** Returns the SDK's original Resource timing origin in monotonic nanoseconds. */
    public long getResourceStartTimeNanos() {
        return resourceStartTimeNanos;
    }

    /** Returns the owning View's start in monotonic nanoseconds. */
    public long getViewStartTimeNanos() {
        return viewStartTimeNanos;
    }
}
