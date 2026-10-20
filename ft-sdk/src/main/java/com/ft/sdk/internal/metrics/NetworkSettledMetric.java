package com.ft.sdk.internal.metrics;

import com.ft.sdk.FTInitialResourceIdentifier;
import com.ft.sdk.FTNetworkSettledResourceContext;
import com.ft.sdk.FTTimeBasedInitialResourceIdentifier;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/** Initial Resource completion relative to the View's monotonic start, in nanoseconds. */
public final class NetworkSettledMetric {
    private final long viewStart;
    private final FTInitialResourceIdentifier identifier;
    private final AtomicBoolean callbackFailureReported = new AtomicBoolean();
    private final Set<String> pending = new HashSet<>();
    private final Set<String> evaluating = new HashSet<>();
    private Long maxDuration;
    private Long frozenValue;
    private boolean stopped;

    public NetworkSettledMetric(long viewStart) {
        this(viewStart, new FTTimeBasedInitialResourceIdentifier());
    }

    public NetworkSettledMetric(long viewStart, FTInitialResourceIdentifier identifier) {
        if (identifier == null) throw new NullPointerException("identifier");
        this.viewStart = viewStart;
        this.identifier = identifier;
    }

    public boolean resourceStarted(String id, long start) {
        synchronized (this) {
            if (stopped || pending.contains(id) || !evaluating.add(id)) return false;
        }
        // Only selected Resources enter pending. Never run application code under our lock.
        boolean selected = false;
        try {
            selected = identifier.validate(new FTNetworkSettledResourceContext(id, start, viewStart));
        } catch (Throwable failure) {
            synchronized (this) {
                evaluating.remove(id);
            }
            ViewTimingCallback.failed(failure, callbackFailureReported);
            return false;
        }
        synchronized (this) {
            evaluating.remove(id);
            return !stopped && selected && pending.add(id);
        }
    }

    public synchronized boolean resourceFinished(String id, Long end) {
        if (stopped || !pending.remove(id)) return false;
        if (end != null) maxDuration = Math.max(maxDuration == null ? 0 : maxDuration, end - viewStart);
        return true;
    }

    public synchronized Long resolve() {
        return stopped ? frozenValue : pending.isEmpty() ? maxDuration : null;
    }

    public synchronized void stop() {
        if (stopped) return;
        frozenValue = resolve();
        stopped = true;
        pending.clear();
        evaluating.clear();
    }
}
