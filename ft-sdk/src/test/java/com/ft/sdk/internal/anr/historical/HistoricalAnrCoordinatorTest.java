package com.ft.sdk.internal.anr.historical;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class HistoricalAnrCoordinatorTest {

    @Test
    public void claimOwnerReadsTraceReportsAndCommitsExactlyOnce() {
        AtomicInteger sourceQueries = new AtomicInteger();
        AtomicInteger traceOpens = new AtomicInteger();
        AtomicInteger reports = new AtomicInteger();
        RecordingLedger ledger = new RecordingLedger();
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app:worker",
                123,
                20_000,
                ProcessExitRecord.REASON_ANR,
                100,
                () -> {
                    traceOpens.incrementAndGet();
                    return new ByteArrayInputStream(
                            "main thread blocked".getBytes(StandardCharsets.UTF_8));
                });
        HistoricalRumContext oldView = new HistoricalRumContext(
                "com.example.app:worker",
                "old-run",
                1_000,
                "old-session",
                "old-view",
                "Checkout",
                10_000_000_000L,
                0,
                false);
        Executor direct = Runnable::run;

        HistoricalAnrCoordinator coordinator = new HistoricalAnrCoordinator(
                "com.example.app",
                new ProcessRunIdentity("com.example.app:worker", "current-run", 30_000),
                () -> {
                    sourceQueries.incrementAndGet();
                    return Collections.singletonList(exit);
                },
                processName -> Collections.singletonList(oldView),
                ledger,
                (record, context, stack, dedupeKey) -> {
                    reports.incrementAndGet();
                    assertEquals("old-session", context.getSessionId());
                    assertEquals("old-view", context.getViewId());
                    assertEquals("main thread blocked", stack);
                    return HistoricalAnrReporter.ReportResult.INSERTED;
                },
                direct,
                () -> 40_000);

        coordinator.consumePending();
        coordinator.consumePending();

        assertEquals(1, sourceQueries.get());
        assertEquals(1, traceOpens.get());
        assertEquals(1, reports.get());
        assertEquals(1, ledger.committed);
    }

    @Test
    public void transientContextReadFailureIsRetriedInsteadOfDropped() {
        RecordingLedger ledger = new RecordingLedger();
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app",
                123,
                20_000,
                ProcessExitRecord.REASON_ANR,
                100,
                () -> new ByteArrayInputStream(new byte[]{1}));
        HistoricalAnrCoordinator coordinator = new HistoricalAnrCoordinator(
                "com.example.app",
                new ProcessRunIdentity("com.example.app", "current-run", 30_000),
                () -> Collections.singletonList(exit),
                processName -> {
                    throw new IOException("temporary store failure");
                },
                ledger,
                (record, context, stack, dedupeKey) ->
                        HistoricalAnrReporter.ReportResult.INSERTED,
                Runnable::run,
                () -> 40_000);

        coordinator.consumePending();

        assertEquals(1, ledger.retried);
        assertEquals(0, ledger.dropped);
    }

    @Test
    public void storedClaimSnapshotAllowsTakeoverWhenViewStoreIsUnavailable() {
        AtomicInteger reports = new AtomicInteger();
        HistoricalRumContext snapshot = new HistoricalRumContext(
                "com.example.app", null, 0,
                "old-session", "old-view", "Home", 0, 0, true);
        RecordingLedger ledger = new RecordingLedger() {
            @Override
            public Claim tryClaim(String exitKey, String ownerRunId, long nowMs,
                                  long leaseUntilMs, ProcessExitRecord exit,
                                  HistoricalRumContext context, String eventDedupeKey) {
                return Claim.acquired(snapshot);
            }
        };
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app", 123, 20_000, ProcessExitRecord.REASON_ANR, 100,
                () -> new ByteArrayInputStream(
                        "blocked".getBytes(StandardCharsets.UTF_8)));
        HistoricalAnrCoordinator coordinator = new HistoricalAnrCoordinator(
                "com.example.app",
                new ProcessRunIdentity("com.example.app", "current-run", 30_000),
                () -> Collections.singletonList(exit),
                processName -> {
                    throw new IOException("view store unavailable");
                },
                ledger,
                (record, context, stack, dedupeKey) -> {
                    reports.incrementAndGet();
                    assertEquals("old-session", context.getSessionId());
                    assertEquals("old-view", context.getViewId());
                    return HistoricalAnrReporter.ReportResult.ALREADY_EXISTS;
                },
                Runnable::run,
                () -> 40_000);

        coordinator.consumePending();

        assertEquals(1, reports.get());
        assertEquals(1, ledger.committed);
        assertEquals(0, ledger.retried);
    }

    @Test
    public void shutdownCancelsAQueuedConsumptionBeforeItQueriesTheSystem() {
        AtomicInteger sourceQueries = new AtomicInteger();
        AtomicReference<Runnable> queued = new AtomicReference<>();
        HistoricalAnrCoordinator coordinator = new HistoricalAnrCoordinator(
                "com.example.app",
                new ProcessRunIdentity("com.example.app", "current-run", 30_000),
                () -> {
                    sourceQueries.incrementAndGet();
                    return Collections.emptyList();
                },
                processName -> Collections.emptyList(),
                new RecordingLedger(),
                (record, context, stack, dedupeKey) ->
                        HistoricalAnrReporter.ReportResult.INSERTED,
                queued::set,
                () -> 40_000);

        coordinator.consumePending();
        coordinator.shutdown();
        queued.get().run();

        assertEquals(0, sourceQueries.get());
    }

    @Test
    public void nullTraceIsDroppedAfterClaim() {
        RecordingLedger ledger = new RecordingLedger();
        HistoricalAnrCoordinator coordinator = coordinatorForTrace(
                new ProcessExitRecord(
                        "com.example.app", 123, 20_000,
                        ProcessExitRecord.REASON_ANR, 100, () -> null),
                ledger);

        coordinator.consumePending();

        assertEquals(1, ledger.dropped);
        assertEquals(0, ledger.retried);
    }

    @Test
    public void traceReadFailureLeavesTheClaimRetryable() {
        RecordingLedger ledger = new RecordingLedger();
        HistoricalAnrCoordinator coordinator = coordinatorForTrace(
                new ProcessExitRecord(
                        "com.example.app", 123, 20_000,
                        ProcessExitRecord.REASON_ANR, 100, () -> {
                    throw new IOException("trace unavailable");
                }),
                ledger);

        coordinator.consumePending();

        assertEquals(1, ledger.retried);
        assertEquals(0, ledger.dropped);
    }

    @Test
    public void terminalClaimsDoNotConsumeThePerRunProcessingBudget() {
        AtomicInteger reports = new AtomicInteger();
        List<ProcessExitRecord> exits = new ArrayList<>();
        for (int i = 0; i < 33; i++) {
            final int pid = i;
            exits.add(new ProcessExitRecord(
                    "com.example.app",
                    pid,
                    20_000 + i,
                    ProcessExitRecord.REASON_ANR,
                    100,
                    () -> new ByteArrayInputStream(
                            "blocked".getBytes(StandardCharsets.UTF_8))));
        }
        HistoricalRumContext oldView = new HistoricalRumContext(
                "com.example.app", "old-run", 1_000,
                "old-session", "old-view", "Home",
                10_000_000_000L, 0, false);
        RecordingLedger ledger = new RecordingLedger() {
            @Override
            public Claim tryClaim(String exitKey, String ownerRunId, long nowMs,
                                  long leaseUntilMs, ProcessExitRecord exit,
                                  HistoricalRumContext context, String eventDedupeKey) {
                return exit.getPid() < 32 ? Claim.skipped() : Claim.acquired(context);
            }
        };
        HistoricalAnrCoordinator coordinator = new HistoricalAnrCoordinator(
                "com.example.app",
                new ProcessRunIdentity("com.example.app", "current-run", 30_000),
                () -> exits,
                processName -> Collections.singletonList(oldView),
                ledger,
                (record, context, stack, dedupeKey) -> {
                    reports.incrementAndGet();
                    return HistoricalAnrReporter.ReportResult.INSERTED;
                },
                Runnable::run,
                () -> 40_000);

        coordinator.consumePending();

        assertEquals(1, reports.get());
        assertEquals(1, ledger.committed);
    }

    private HistoricalAnrCoordinator coordinatorForTrace(ProcessExitRecord exit,
                                                         RecordingLedger ledger) {
        HistoricalRumContext oldView = new HistoricalRumContext(
                "com.example.app", "old-run", 1_000,
                "old-session", "old-view", "Home",
                10_000_000_000L, 0, false);
        return new HistoricalAnrCoordinator(
                "com.example.app",
                new ProcessRunIdentity("com.example.app", "current-run", 30_000),
                () -> Collections.singletonList(exit),
                processName -> Collections.singletonList(oldView),
                ledger,
                (record, context, stack, dedupeKey) ->
                        HistoricalAnrReporter.ReportResult.INSERTED,
                Runnable::run,
                () -> 40_000);
    }

    private static class RecordingLedger implements HistoricalExitLedger {
        private boolean claimed;
        private int committed;
        private int retried;
        private int dropped;

        @Override
        public Claim tryClaim(String exitKey, String ownerRunId, long nowMs,
                              long leaseUntilMs, ProcessExitRecord exit,
                              HistoricalRumContext context, String eventDedupeKey) {
            if (claimed) {
                return Claim.skipped();
            }
            claimed = true;
            return Claim.acquired(context);
        }

        @Override
        public void markCommitted(String exitKey, String ownerRunId, long nowMs) {
            committed++;
        }

        @Override
        public void markRetry(String exitKey, String ownerRunId, long retryAtMs, long nowMs) {
            retried++;
        }

        @Override
        public void markDropped(String exitKey, String ownerRunId, String reason, long nowMs) {
            dropped++;
        }

        @Override
        public void cleanup(long terminalBeforeMs) {
        }
    }
}
