package com.ft.sdk.internal.metrics;

import java.util.HashSet;
import java.util.Set;

/** Initial Resource completion relative to the View's monotonic start, in nanoseconds. */
public final class NetworkSettledMetric {
    private static final long INITIAL_WINDOW_NS = 100_000_000L;
    private final long viewStart;
    private final Set<String> pending = new HashSet<>();
    private Long maxDuration;
    private Long frozenValue;
    private boolean stopped;

    public NetworkSettledMetric(long viewStart) {
        this.viewStart = viewStart;
    }

    public synchronized boolean resourceStarted(String id, long start) {
        // Match the reference identifier: the upper boundary is strict, with no lower bound.
        return !stopped && start - viewStart < INITIAL_WINDOW_NS && pending.add(id);
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
    }
}
