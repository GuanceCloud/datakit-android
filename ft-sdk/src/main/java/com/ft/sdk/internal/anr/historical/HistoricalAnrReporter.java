package com.ft.sdk.internal.anr.historical;

interface HistoricalAnrReporter {
    enum ReportResult {
        INSERTED,
        ALREADY_EXISTS,
        FAILED
    }

    ReportResult report(ProcessExitRecord exit, HistoricalRumContext context, String stack,
                        String eventDedupeKey);
}
