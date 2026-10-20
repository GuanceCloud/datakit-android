package com.ft.sdk.internal.metrics;

import org.junit.Test;
import static org.junit.Assert.*;

public class ViewTimingMetricTest {
    private static final long MS = 1_000_000L;

    @Test public void initialResourcesCompleteOutOfOrderAndExcludeTheBoundary() {
        NetworkSettledMetric metric = new NetworkSettledMetric(0);
        assertNull(metric.resolve());
        metric.resourceStarted("a", 20 * MS);
        metric.resourceStarted("b", 80 * MS);
        metric.resourceStarted("boundary", 100 * MS);
        metric.resourceStarted("late", 120 * MS);
        metric.resourceFinished("b", 400 * MS);
        assertNull(metric.resolve());
        metric.resourceFinished("a", 150 * MS);
        assertEquals(Long.valueOf(400 * MS), metric.resolve());
        metric.resourceFinished("boundary", 900 * MS);
        metric.resourceFinished("late", 800 * MS);
        metric.resourceFinished("a", 1000 * MS);
        assertEquals(Long.valueOf(400 * MS), metric.resolve());
    }

    @Test public void anotherInitialRequestMakesTheValuePendingAgainAndDiscardDoesNotAddTime() {
        NetworkSettledMetric metric = new NetworkSettledMetric(10 * MS);
        metric.resourceStarted("a", 20 * MS);
        metric.resourceFinished("a", 40 * MS);
        assertEquals(Long.valueOf(30 * MS), metric.resolve());
        metric.resourceStarted("b", 80 * MS);
        assertNull(metric.resolve());
        metric.resourceFinished("b", null);
        assertEquals(Long.valueOf(30 * MS), metric.resolve());
        NetworkSettledMetric discarded = new NetworkSettledMetric(0);
        discarded.resourceStarted("only", 0);
        discarded.resourceFinished("only", null);
        assertNull(discarded.resolve());
    }

    @Test public void stoppingFreezesBothResolvedAndUnresolvedValues() {
        for (boolean finish : new boolean[]{false, true}) {
            NetworkSettledMetric metric = new NetworkSettledMetric(0);
            metric.resourceStarted("a", 1);
            if (finish) metric.resourceFinished("a", 42L);
            metric.stop();
            metric.resourceFinished("a", 100L);
            metric.resourceStarted("new", 2);
            metric.resourceFinished("new", 200L);
            metric.stop();
            assertEquals(finish ? Long.valueOf(42) : null, metric.resolve());
        }
    }

    @Test public void referenceIdentifierHasNoLowerBoundAndValidZeroIsKept() {
        NetworkSettledMetric metric = new NetworkSettledMetric(10);
        metric.resourceStarted("negative", 9);
        metric.resourceFinished("negative", 10L);
        assertEquals(Long.valueOf(0), metric.resolve());
    }

    @Test public void interactionBelongsToNextViewAndCanArriveAfterViewCreation() {
        InteractionToNextViewMetric metric = new InteractionToNextViewMetric();
        metric.viewCreated("one", 0);
        metric.viewCreated("two", 1250 * MS);
        assertNull(metric.resolve("two"));
        metric.actionStored("one", "click", 1000 * MS);
        assertEquals(Long.valueOf(250 * MS), metric.resolve("two"));
        assertNull(metric.resolve("one"));
        metric.viewCreated("three", 1400 * MS);
        assertNull(metric.resolve("three"));
        metric.actionStored("two", "back", 1300 * MS);
        assertEquals(Long.valueOf(100 * MS), metric.resolve("three"));
    }

    @Test public void invHasStrictPositiveAndThreeSecondBounds() {
        for (long delta : new long[]{-1, 0, 1, 3000 * MS - 1, 3000 * MS, 3000 * MS + 1}) {
            InteractionToNextViewMetric metric = new InteractionToNextViewMetric();
            metric.viewCreated("one", 0);
            metric.viewCreated("two", 4000 * MS);
            metric.actionStored("one", "tap", 4000 * MS - delta);
            assertEquals(delta > 0 && delta < 3000 * MS ? Long.valueOf(delta) : null, metric.resolve("two"));
        }
    }

    @Test public void lastReceivedEligibleInteractionWinsAndOtherTypesDoNotOverwrite() {
        InteractionToNextViewMetric metric = new InteractionToNextViewMetric();
        metric.viewCreated("one", 0);
        metric.viewCreated("two", 1000);
        for (String type : new String[]{"tap", "swipe", "click", "back"}) {
            metric.actionStored("one", type, 900);
            assertEquals(Long.valueOf(100), metric.resolve("two"));
            metric.actionStored("one", type, 800);
            assertEquals(Long.valueOf(200), metric.resolve("two"));
        }
        for (String type : new String[]{"custom", "scroll", "launch_cold", null}) {
            metric.actionStored("one", type, 950);
            assertEquals(Long.valueOf(200), metric.resolve("two"));
        }
    }

    @Test public void historyIsBoundedAndClearPreventsCrossLifecycleCorrelation() {
        InteractionToNextViewMetric metric = new InteractionToNextViewMetric();
        for (int i = 0; i < 5; i++) {
            metric.viewCreated("v" + i, i * 1000L);
            metric.actionStored("v" + i, "click", i * 1000L + 100);
        }
        assertNull(metric.resolve("v0"));
        assertEquals(Long.valueOf(900), metric.resolve("v1"));
        assertEquals(Long.valueOf(900), metric.resolve("v4"));
        metric.clear();
        assertNull(metric.resolve("v4"));
    }
}
