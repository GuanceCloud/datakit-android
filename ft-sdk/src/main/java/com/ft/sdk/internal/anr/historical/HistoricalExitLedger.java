package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.garble.bean.CollectType;

import java.util.List;

interface HistoricalExitLedger {
    enum ClaimResult {
        ACQUIRED,
        COMMITTED,
        SKIPPED
    }

    final class Claim {
        private static final Claim SKIPPED = new Claim(ClaimResult.SKIPPED, null);

        private final ClaimResult result;
        private final HistoricalRumContext context;

        private Claim(ClaimResult result, HistoricalRumContext context) {
            this.result = result;
            this.context = context;
        }

        static Claim acquired(HistoricalRumContext context) {
            return new Claim(ClaimResult.ACQUIRED, context);
        }

        static Claim committed(HistoricalRumContext context) {
            return new Claim(ClaimResult.COMMITTED, context);
        }

        static Claim skipped() {
            return SKIPPED;
        }

        ClaimResult getResult() {
            return result;
        }

        boolean isAcquired() {
            return result == ClaimResult.ACQUIRED;
        }

        boolean isCommitted() {
            return result == ClaimResult.COMMITTED;
        }

        HistoricalRumContext getContext() {
            return context;
        }
    }

    final class PendingCommit {
        private final String exitKey;
        private final String eventDedupeKey;
        private final CollectType collectType;

        PendingCommit(String exitKey, String eventDedupeKey, CollectType collectType) {
            this.exitKey = exitKey;
            this.eventDedupeKey = eventDedupeKey;
            this.collectType = collectType == null
                    ? CollectType.NOT_COLLECT
                    : collectType;
        }

        String getExitKey() {
            return exitKey;
        }

        String getEventDedupeKey() {
            return eventDedupeKey;
        }

        CollectType getCollectType() {
            return collectType;
        }
    }

    Claim tryClaim(String exitKey, String ownerRunId, long nowMs, long leaseUntilMs,
                   ProcessExitRecord exit, HistoricalRumContext context,
                   String eventDedupeKey);

    boolean markCommitted(String exitKey, String ownerRunId, long nowMs);

    List<PendingCommit> loadPendingCommits();

    boolean markEventVisible(String exitKey, String eventDedupeKey, long nowMs);

    void markRetry(String exitKey, String ownerRunId, long retryAtMs, long nowMs);

    void markDropped(String exitKey, String ownerRunId, String reason, long nowMs);

    void cleanup(long terminalBeforeMs);
}
