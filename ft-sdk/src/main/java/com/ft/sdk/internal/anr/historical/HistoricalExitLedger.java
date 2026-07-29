package com.ft.sdk.internal.anr.historical;

interface HistoricalExitLedger {
    enum ClaimResult {
        ACQUIRED,
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

        static Claim skipped() {
            return SKIPPED;
        }

        ClaimResult getResult() {
            return result;
        }

        boolean isAcquired() {
            return result == ClaimResult.ACQUIRED;
        }

        HistoricalRumContext getContext() {
            return context;
        }
    }

    Claim tryClaim(String exitKey, String ownerRunId, long nowMs, long leaseUntilMs,
                   ProcessExitRecord exit, HistoricalRumContext context,
                   String eventDedupeKey);

    void markCommitted(String exitKey, String ownerRunId, long nowMs);

    void markRetry(String exitKey, String ownerRunId, long retryAtMs, long nowMs);

    void markDropped(String exitKey, String ownerRunId, String reason, long nowMs);

    void cleanup(long terminalBeforeMs);
}
