package com.ft.sdk;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppStateHistoryTest {
    static class TestClock implements AppStateHistory.Clock {
        long now = 1_000_000_000L;
        long mono = now;
        public long epochNanos() { return now; }
        public long monotonicNanos() { return mono; }
        void advance(long ns) { now += ns; mono += ns; }
    }

    @Test public void halfOpenBoundariesAndPointQueries() {
        TestClock clock = new TestClock();
        AppStateHistory history = new AppStateHistory(clock);
        long start = history.record(AppStateHistory.FOREGROUND);
        clock.advance(100);
        long boundary = history.record(AppStateHistory.BACKGROUND);
        clock.advance(100);
        assertEquals("foreground", history.between(start, boundary));
        assertEquals("background", history.at(boundary));
        assertEquals("background", history.during(boundary, 0));
        assertEquals("foreground_and_background", history.between(start, boundary + 1));
        assertEquals("background", history.between(boundary, clock.now));
    }

    @Test public void coveredUnknownDoesNotMakeAKnownStateMixed() {
        TestClock clock = new TestClock();
        AppStateHistory history = new AppStateHistory(clock);
        long start = clock.now;
        clock.advance(100);
        assertEquals("unknown", history.between(start, clock.now));
        history.record(AppStateHistory.FOREGROUND);
        clock.advance(100);
        assertEquals("foreground", history.between(start, clock.now));
        history.record(AppStateHistory.BACKGROUND);
        clock.advance(100);
        assertEquals("foreground_and_background", history.between(start, clock.now));
    }

    @Test public void evictionAndInvalidBoundariesReturnUnknown() {
        TestClock clock = new TestClock();
        AppStateHistory history = new AppStateHistory(clock);
        long start = history.record(AppStateHistory.FOREGROUND);
        for (int i = 0; i < 128; i++) {
            clock.advance(100);
            history.record(i % 2 == 0 ? AppStateHistory.BACKGROUND : AppStateHistory.FOREGROUND);
        }
        assertEquals("unknown", history.between(start, clock.now));
        assertEquals("foreground", history.at(clock.now));
        assertEquals("unknown", history.between(0, clock.now));
        assertEquals("unknown", history.between(clock.now, start));
        assertEquals("unknown", history.during(clock.now, -1));
        assertEquals("unknown", history.during(clock.now, Long.MAX_VALUE));
        assertEquals("unknown", history.at(clock.now + 1_000_000_000L));
    }

    @Test public void duplicateCallbacksDoNotEvictHistoryAndEqualTimeHasNoDuration() {
        TestClock clock = new TestClock();
        AppStateHistory history = new AppStateHistory(clock);
        long start = history.record(AppStateHistory.FOREGROUND);
        for (int i = 0; i < 256; i++) {
            clock.advance(100);
            history.record(AppStateHistory.FOREGROUND);
        }
        assertEquals("foreground", history.between(start, clock.now));
        history.record(AppStateHistory.BACKGROUND);
        history.record(AppStateHistory.FOREGROUND);
        clock.advance(100);
        assertEquals("foreground", history.between(start, clock.now));
    }

    @Test public void detectedClockJumpsInvalidateHistoryInEitherDirection() {
        for (long jump : new long[]{-1_000_000_000L, 1_000_000_000L}) {
            TestClock clock = new TestClock();
            AppStateHistory history = new AppStateHistory(clock);
            long start = history.record(AppStateHistory.FOREGROUND);
            clock.now += jump;
            assertEquals("unknown", history.at(start));
            clock.advance(2_000_000_000L);
            history.record(AppStateHistory.FOREGROUND);
            assertEquals("unknown", history.at(clock.now));
        }
    }
    @Test public void schedulingPausesInsideClockReadsDoNotInvalidateHistory() {
        TestClock clock = new TestClock() {
            @Override public long epochNanos() {
                long result = now;
                advance(100_000_000L);
                return result;
            }
        };
        AppStateHistory history = new AppStateHistory(clock);
        long start = history.record(AppStateHistory.FOREGROUND);
        assertEquals("foreground", history.between(start, clock.now));
    }

    @Test public void newHistoryDoesNotReuseEarlierProcessCoverage() {
        TestClock clock = new TestClock();
        AppStateHistory previous = new AppStateHistory(clock);
        long start = previous.record(AppStateHistory.FOREGROUND);
        clock.advance(100);
        AppStateHistory current = new AppStateHistory(clock);
        assertNotEquals(previous.processToken, current.processToken);
        assertEquals("unknown", current.between(start, clock.now));
    }

}
