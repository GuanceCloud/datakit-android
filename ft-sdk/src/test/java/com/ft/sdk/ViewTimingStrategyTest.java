package com.ft.sdk;

import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.internal.metrics.InteractionToNextViewMetric;
import com.ft.sdk.internal.metrics.NetworkSettledMetric;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ViewTimingStrategyTest {
    private static final long MS = 1_000_000L;

    @Before public void disableAndroidLogging() {
        LogUtils.setDebug(false);
    }

    @Test public void configurationDefaultsAndNullContracts() {
        FTRUMConfig config = new FTRUMConfig();
        assertTrue(config.getInitialResourceIdentifier() instanceof FTTimeBasedInitialResourceIdentifier);
        assertTrue(config.getLastInteractionIdentifier() instanceof FTTimeBasedInteractionIdentifier);
        assertSame(config, config.setLastInteractionIdentifier(null));
        assertNull(config.getLastInteractionIdentifier());
        assertThrows(NullPointerException.class, () -> config.setInitialResourceIdentifier(null));
        for (long invalid : new long[]{0, -1, Long.MIN_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> new FTTimeBasedInitialResourceIdentifier(invalid));
            assertThrows(IllegalArgumentException.class, () -> new FTTimeBasedInteractionIdentifier(invalid));
        }
    }

    @Test public void timeStrategiesUseMillisecondsAndStrictBoundariesWithoutOverflow() {
        FTInitialResourceIdentifier initial = new FTTimeBasedInitialResourceIdentifier(500);
        assertTrue(initial.validate(new FTNetworkSettledResourceContext("r", 500 * MS - 1, 0)));
        assertFalse(initial.validate(new FTNetworkSettledResourceContext("r", 500 * MS, 0)));
        assertTrue(initial.validate(new FTNetworkSettledResourceContext("r", 9, 10)));
        FTLastInteractionIdentifier interaction = new FTTimeBasedInteractionIdentifier(5000);
        assertTrue(interaction.validate(new FTPreviousViewLastInteractionContext("click", 0, 5000 * MS - 1)));
        assertFalse(interaction.validate(new FTPreviousViewLastInteractionContext("click", 0, 5000 * MS)));
        assertFalse(interaction.validate(new FTPreviousViewLastInteractionContext("click", 1, 1)));
        assertFalse(interaction.validate(new FTPreviousViewLastInteractionContext("click", 2, 1)));
        assertTrue(new FTTimeBasedInitialResourceIdentifier(Long.MAX_VALUE)
                .validate(new FTNetworkSettledResourceContext("r", 1000 * MS, 0)));
        assertTrue(new FTTimeBasedInteractionIdentifier(Long.MAX_VALUE)
                .validate(new FTPreviousViewLastInteractionContext("click", 0, 10_000 * MS)));
    }

    @Test public void customNetworkRuleReplacesWindowAndUsesOriginalContext() {
        AtomicInteger calls = new AtomicInteger();
        NetworkSettledMetric metric = new NetworkSettledMetric(10 * MS, context -> {
            calls.incrementAndGet();
            assertEquals(10 * MS, context.getViewStartTimeNanos());
            assertEquals(600 * MS, context.getResourceStartTimeNanos());
            return "selected".equals(context.getResourceId());
        });
        assertFalse(metric.resourceStarted("excluded", 600 * MS));
        assertTrue(metric.resourceStarted("selected", 600 * MS));
        assertFalse(metric.resourceStarted("selected", 600 * MS));
        metric.resourceFinished("excluded", 1000 * MS);
        assertNull(metric.resolve());
        metric.resourceFinished("selected", 800 * MS);
        assertEquals(Long.valueOf(790 * MS), metric.resolve());
        metric.resolve(); metric.stop(); metric.resolve();
        assertFalse(metric.resourceStarted("after-stop", 600 * MS));
        assertEquals(2, calls.get());
    }

    @Test public void networkFailureReleasesPendingAndDoesNotBreakLaterRequests() {
        NetworkSettledMetric metric = new NetworkSettledMetric(0, context -> {
            if ("failed".equals(context.getResourceId())) throw new IllegalStateException("callback");
            return true;
        });
        assertFalse(metric.resourceStarted("failed", 0));
        assertTrue(metric.resourceStarted("good", 0));
        metric.resourceFinished("good", 40L);
        assertEquals(Long.valueOf(40), metric.resolve());
    }

    @Test public void networkCallbackRunsOutsideLockAndCannotRegisterAfterStop() {
        NetworkSettledMetric[] holder = new NetworkSettledMetric[1];
        boolean[] lockHeld = {true};
        holder[0] = new NetworkSettledMetric(0, context -> {
            lockHeld[0] = Thread.holdsLock(holder[0]);
            holder[0].stop();
            return true;
        });
        assertFalse(holder[0].resourceStarted("r", 0));
        assertFalse(lockHeld[0]);
        assertNull(holder[0].resolve());
    }

    @Test public void customInvReplacesWindowButCannotEnableOtherTypesOrNonPositiveIntervals() {
        AtomicInteger calls = new AtomicInteger();
        InteractionToNextViewMetric metric = new InteractionToNextViewMetric(context -> {
            calls.incrementAndGet();
            assertEquals("click", context.getActionType());
            assertEquals(0, context.getInteractionTimeNanos());
            assertEquals(4000 * MS, context.getNextViewStartTimeNanos());
            return true;
        });
        metric.viewCreated("one", -1);
        metric.actionStored("one", "click", 0);
        metric.viewCreated("two", 4000 * MS);
        assertEquals(Long.valueOf(4000 * MS), metric.resolve("two"));
        assertFalse(metric.actionStored("one", "custom", 1));
        assertFalse(metric.actionStored("one", "scroll", 1));
        assertEquals(1, calls.get());
        for (long timestamp : new long[]{4000 * MS, 4000 * MS + 1}) {
            InteractionToNextViewMetric nonPositive = new InteractionToNextViewMetric(context -> true);
            nonPositive.viewCreated("a", 0);
            nonPositive.viewCreated("b", 4000 * MS);
            nonPositive.actionStored("a", "tap", timestamp);
            assertNull(nonPositive.resolve("b"));
        }
    }

    @Test public void stoppingDuringSelectionPreservesPreviouslySettledValue() {
        NetworkSettledMetric[] holder = new NetworkSettledMetric[1];
        holder[0] = new NetworkSettledMetric(0, context -> {
            if ("later".equals(context.getResourceId())) holder[0].stop();
            return true;
        });
        assertTrue(holder[0].resourceStarted("first", 0));
        holder[0].resourceFinished("first", 100L);
        assertFalse(holder[0].resourceStarted("later", 1));
        assertEquals(Long.valueOf(100), holder[0].resolve());
    }

    @Test public void invCachesPerCandidateAndDoesNotSearchEarlierAcceptedCandidates() {
        AtomicInteger calls = new AtomicInteger();
        InteractionToNextViewMetric metric = new InteractionToNextViewMetric(context -> {
            calls.incrementAndGet();
            return "click".equals(context.getActionType());
        });
        metric.viewCreated("source", 0);
        metric.actionStored("source", "click", 100);
        assertEquals(0, calls.get());
        metric.viewCreated("target", 1000);
        for (int i = 0; i < 10; i++) assertEquals(Long.valueOf(900), metric.resolve("target"));
        metric.viewCreated("target", 2000); // duplicate notifications do not re-evaluate
        assertEquals(1, calls.get());
        metric.actionStored("source", "swipe", 200);
        assertNull(metric.resolve("target"));
        assertEquals(2, calls.get());
        metric.actionStored("source", "custom", 300);
        assertNull(metric.resolve("target"));
        assertEquals(2, calls.get());
    }

    @Test public void historyEvictionPreservesValueAndDoesNotReevaluateBusinessState() {
        AtomicInteger calls = new AtomicInteger();
        InteractionToNextViewMetric metric = new InteractionToNextViewMetric(context -> {
            calls.incrementAndGet();
            return true;
        });
        metric.viewCreated("one", 0);
        metric.actionStored("one", "click", 100);
        metric.viewCreated("two", 200);
        metric.viewCreated("three", 300);
        metric.viewCreated("four", 400);
        metric.viewCreated("five", 500);
        metric.actionStored("five", "click", 550);
        assertEquals(Long.valueOf(100), metric.resolve("two"));
        assertEquals(1, calls.get());
        assertFalse(metric.actionStored("one", "click", 150));
        assertEquals(Long.valueOf(100), metric.resolve("two"));
        metric.clear();
        assertNull(metric.resolve("two"));
    }

    @Test public void invFailureRejectsNewCandidateAndKeepsFutureEvaluationsWorking() {
        InteractionToNextViewMetric metric = new InteractionToNextViewMetric(context -> {
            if ("swipe".equals(context.getActionType())) throw new IllegalArgumentException("callback");
            return true;
        });
        metric.viewCreated("source", 0);
        metric.viewCreated("target", 1000);
        metric.actionStored("source", "click", 100);
        assertEquals(Long.valueOf(900), metric.resolve("target"));
        metric.actionStored("source", "swipe", 200);
        assertNull(metric.resolve("target"));
        metric.actionStored("source", "tap", 300);
        assertEquals(Long.valueOf(700), metric.resolve("target"));
    }

    @Test public void invCallbackRunsOutsideLockAndClearCannotBeUndoneByItsReturn() {
        InteractionToNextViewMetric[] holder = new InteractionToNextViewMetric[1];
        boolean[] lockHeld = {true};
        holder[0] = new InteractionToNextViewMetric(context -> {
            lockHeld[0] = Thread.holdsLock(holder[0]);
            holder[0].clear();
            return true;
        });
        holder[0].viewCreated("source", 0);
        holder[0].viewCreated("target", 1000);
        holder[0].actionStored("source", "click", 100);
        assertFalse(lockHeld[0]);
        assertNull(holder[0].resolve("target"));
    }

    @Test(timeout = 5000) public void olderConcurrentEvaluationCannotOverwriteNewerCandidate() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            InteractionToNextViewMetric metric = new InteractionToNextViewMetric(context -> {
                if (context.getInteractionTimeNanos() == 100) {
                    entered.countDown();
                    try {
                        if (!resume.await(2, TimeUnit.SECONDS)) throw new IllegalStateException("timeout");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }
                return true;
            });
            metric.viewCreated("source", 0);
            metric.viewCreated("target", 1000);
            Future<?> old = executor.submit(() -> metric.actionStored("source", "click", 100));
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            metric.actionStored("source", "click", 300);
            assertEquals(Long.valueOf(700), metric.resolve("target"));
            resume.countDown();
            old.get(2, TimeUnit.SECONDS);
            assertEquals(Long.valueOf(700), metric.resolve("target"));
        } finally {
            resume.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test public void nullInvStrategyDisablesHistoryAndCorrelation() {
        InteractionToNextViewMetric metric = new InteractionToNextViewMetric(null);
        metric.viewCreated("one", 0);
        assertFalse(metric.actionStored("one", "click", 100));
        metric.viewCreated("two", 200);
        assertNull(metric.resolve("two"));
    }
}
