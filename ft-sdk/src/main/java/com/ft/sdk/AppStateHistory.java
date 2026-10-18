package com.ft.sdk;

import java.util.ArrayDeque;
import java.util.UUID;

/** Process-local, bounded history of epoch-nanosecond lifecycle boundaries. */
final class AppStateHistory {
    static final String FOREGROUND = "foreground";
    static final String BACKGROUND = "background";
    static final String MIXED = "foreground_and_background";
    static final String UNKNOWN = "unknown";
    private static final int CAPACITY = 128;
    // Epoch clocks have millisecond precision; tolerate scheduling/rounding noise only.
    private static final long CLOCK_TOLERANCE = 10_000_000L;
    interface Clock {
        long epochNanos();
        long monotonicNanos();
    }
    private final Clock clock;
    private final ArrayDeque<Entry> entries = new ArrayDeque<>();
    final String processToken = UUID.randomUUID().toString();
    private long lastEpoch;
    private long lastMonotonic;
    private long lastMonotonicBefore;
    private boolean incomparable;

    AppStateHistory() {
        this(new Clock() {
            public long epochNanos() { return System.currentTimeMillis() * 1_000_000L; }
            public long monotonicNanos() { return android.os.SystemClock.elapsedRealtimeNanos(); }
        });
    }

    AppStateHistory(Clock clock) {
        this.clock = clock;
        lastMonotonicBefore = clock.monotonicNanos();
        lastEpoch = clock.epochNanos();
        lastMonotonic = clock.monotonicNanos();
        entries.add(new Entry(lastEpoch, UNKNOWN));
    }

    synchronized long record(String state) {
        long now = observeClock();
        Entry last = entries.peekLast();
        if (!last.state.equals(state)) {
            if (now < last.time) incomparable = true;
            if (now == last.time) entries.removeLast();
            entries.addLast(new Entry(now, state));
            if (entries.size() > CAPACITY) entries.removeFirst();
        }
        return now;
    }

    synchronized String at(long time) { return between(time, time); }

    synchronized String during(long start, long duration) {
        if (duration < 0 || start > Long.MAX_VALUE - duration) return UNKNOWN;
        return between(start, start + duration);
    }

    /** Intervals are [start, end); zero duration is a point query. */
    synchronized String between(long start, long end) {
        long now = observeClock();
        if (incomparable || start <= 0 || end < start || end > now + CLOCK_TOLERANCE
                || start < entries.peekFirst().time) return UNKNOWN;
        String state = UNKNOWN;
        boolean foreground = false;
        boolean background = false;
        for (Entry entry : entries) {
            if (entry.time <= start) {
                state = entry.state;
            } else if (entry.time < end) {
                foreground |= FOREGROUND.equals(state);
                background |= BACKGROUND.equals(state);
                state = entry.state;
            } else break;
        }
        foreground |= FOREGROUND.equals(state);
        background |= BACKGROUND.equals(state);
        return foreground && background ? MIXED : foreground ? FOREGROUND
                : background ? BACKGROUND : UNKNOWN;
    }

    private long observeClock() {
        long before = clock.monotonicNanos();
        long epoch = clock.epochNanos();
        long after = clock.monotonicNanos();
        // Bracket epoch reads so scheduling pauses do not look like wall-clock jumps.
        long elapsed = epoch - lastEpoch;
        if (before < lastMonotonic || after < before
                || elapsed < before - lastMonotonic - CLOCK_TOLERANCE
                || elapsed > after - lastMonotonicBefore + CLOCK_TOLERANCE) {
            incomparable = true;
        }
        lastEpoch = epoch;
        lastMonotonicBefore = before;
        lastMonotonic = after;
        return epoch;
    }

    private static final class Entry {
        final long time;
        final String state;
        Entry(long time, String state) { this.time = time; this.state = state; }
    }
}
