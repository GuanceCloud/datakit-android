package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.garble.bean.CollectType;

interface HistoricalAnrReporter {
    enum ReportResult {
        INSERTED,
        ALREADY_EXISTS,
        FAILED
    }

    ReportResult report(ProcessExitRecord exit, HistoricalRumContext context, String stack,
                        String eventDedupeKey);

    default void onCommitted(ReportResult result) {
    }

    default boolean onCommitted(ReportResult result,
                                String eventDedupeKey,
                                CollectType collectType) {
        onCommitted(result);
        return true;
    }
}
