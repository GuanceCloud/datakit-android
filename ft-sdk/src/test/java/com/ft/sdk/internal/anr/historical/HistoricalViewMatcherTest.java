package com.ft.sdk.internal.anr.historical;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HistoricalViewMatcherTest {
    private static final long TOLERANCE_MS = 5;
    private static final long MAX_DISTANCE_MS = 30_000;

    @Test
    public void latestOpenViewFromPreviousRunWinsWithinSameProcess() {
        ProcessExitRecord exit = exit("com.example.app:worker", 20_000);
        HistoricalRumContext older = context(
                "com.example.app:worker", "old-run", "older", 10_000, 0, false);
        HistoricalRumContext expected = context(
                "com.example.app:worker", "old-run", "expected", 15_000, 0, false);
        HistoricalRumContext currentRun = context(
                "com.example.app:worker", "current-run", "current", 19_000, 0, false);
        HistoricalRumContext otherProcess = context(
                "com.example.app", "main-run", "main", 19_500, 0, false);

        HistoricalRumContext match = HistoricalViewMatcher.match(
                Arrays.asList(older, expected, currentRun, otherProcess),
                exit,
                "current-run",
                TOLERANCE_MS,
                MAX_DISTANCE_MS);

        assertEquals("expected", match.getViewId());
    }

    @Test
    public void closedViewContainingExitWinsWhenNoOpenViewExists() {
        ProcessExitRecord exit = exit("com.example.app", 12_000);
        HistoricalRumContext containing = context(
                "com.example.app", "old-run", "containing", 10_000, 3_000, true);
        HistoricalRumContext endedEarlier = context(
                "com.example.app", "old-run", "earlier", 5_000, 1_000, true);

        HistoricalRumContext match = HistoricalViewMatcher.match(
                Arrays.asList(endedEarlier, containing),
                exit,
                "current-run",
                TOLERANCE_MS,
                MAX_DISTANCE_MS);

        assertEquals("containing", match.getViewId());
    }

    @Test
    public void nearestPreExitViewMustBeWithinMaximumDistance() {
        ProcessExitRecord exit = exit("com.example.app", 50_000);
        HistoricalRumContext near = context(
                "com.example.app", "old-run", "near", 30_000, 5_000, true);
        HistoricalRumContext far = context(
                "com.example.app", "older-run", "far", 1_000, 1_000, true);

        HistoricalRumContext match = HistoricalViewMatcher.match(
                Arrays.asList(far, near),
                exit,
                "current-run",
                TOLERANCE_MS,
                20_000);

        assertEquals("near", match.getViewId());
        assertNull(HistoricalViewMatcher.match(
                Collections.singletonList(far),
                exit,
                "current-run",
                TOLERANCE_MS,
                20_000));
    }

    @Test
    public void legacyViewWithoutProcessRunIdIsNeverMatched() {
        HistoricalRumContext legacy = context(
                "com.example.app", null, "legacy", 10_000, 0, false);

        assertNull(HistoricalViewMatcher.match(
                Collections.singletonList(legacy),
                exit("com.example.app", 12_000),
                "current-run",
                TOLERANCE_MS,
                MAX_DISTANCE_MS));
    }

    @Test
    public void equallyRankedCandidatesAreRejectedAsAmbiguous() {
        HistoricalRumContext first = context(
                "com.example.app", "run-one", "first", 10_000, 0, false);
        HistoricalRumContext second = context(
                "com.example.app", "run-two", "second", 10_000, 0, false);

        assertNull(HistoricalViewMatcher.match(
                Arrays.asList(first, second),
                exit("com.example.app", 12_000),
                "current-run",
                TOLERANCE_MS,
                MAX_DISTANCE_MS));
    }

    private static ProcessExitRecord exit(String processName, long timestampMs) {
        return new ProcessExitRecord(processName, 123, timestampMs, 6, 100, null);
    }

    private static HistoricalRumContext context(String processName, String processRunId,
                                                String viewId, long startMs,
                                                long timeSpentMs, boolean closed) {
        return new HistoricalRumContext(
                processName,
                processRunId,
                1_000,
                "session-" + viewId,
                viewId,
                "view-" + viewId,
                startMs * 1_000_000,
                timeSpentMs * 1_000_000,
                closed);
    }
}
