package com.ft.sdk.internal.anr.historical;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.ft.sdk.garble.db.FTContentProvider;
import com.ft.sdk.garble.db.FTSQL;

final class ProviderHistoricalExitLedger implements HistoricalExitLedger {
    private final ContentResolver resolver;

    ProviderHistoricalExitLedger(Context context) {
        resolver = context.getContentResolver();
    }

    @Override
    public Claim tryClaim(String exitKey, String ownerRunId, long nowMs,
                          long leaseUntilMs, ProcessExitRecord exit,
                          HistoricalRumContext context, String eventDedupeKey) {
        Bundle extras = new Bundle();
        extras.putString("exit_key", exitKey);
        extras.putString("owner_run_id", ownerRunId);
        extras.putLong("now_ms", nowMs);
        extras.putLong("lease_until_ms", leaseUntilMs);
        extras.putString("process_name", exit.getProcessName());
        extras.putInt("pid", exit.getPid());
        extras.putLong("exit_time_ms", exit.getTimestampMs());
        extras.putInt("reason", exit.getReason());
        extras.putString("event_dedupe_key", eventDedupeKey);
        if (context != null) {
            extras.putString("session_id", context.getSessionId());
            extras.putString("view_id", context.getViewId());
            extras.putString("view_name", context.getViewName());
            extras.putString("collect_type", context.getCollectType().getValue());
        }
        Bundle result = resolver.call(
                FTContentProvider.getUriViewData(),
                FTContentProvider.METHOD_HISTORICAL_EXIT_CLAIM,
                null,
                extras);
        if (result == null || !result.getBoolean("acquired")) {
            return Claim.skipped();
        }
        HistoricalRumContext snapshot = HistoricalRumContext.fromSnapshot(
                exit.getProcessName(),
                result.getString("session_id"),
                result.getString("view_id"),
                result.getString("view_name"),
                result.getString("collect_type"));
        return Claim.acquired(snapshot);
    }

    @Override
    public void markCommitted(String exitKey, String ownerRunId, long nowMs) {
        update(exitKey, ownerRunId, FTSQL.HISTORICAL_EXIT_STATE_COMMITTED,
                0, null, nowMs);
    }

    @Override
    public void markRetry(String exitKey, String ownerRunId, long retryAtMs, long nowMs) {
        update(exitKey, ownerRunId, FTSQL.HISTORICAL_EXIT_STATE_RETRY,
                retryAtMs, null, nowMs);
    }

    @Override
    public void markDropped(String exitKey, String ownerRunId, String reason, long nowMs) {
        update(exitKey, ownerRunId, FTSQL.HISTORICAL_EXIT_STATE_DROPPED,
                0, reason, nowMs);
    }

    @Override
    public void cleanup(long terminalBeforeMs) {
        Bundle extras = new Bundle();
        extras.putLong("terminal_before_ms", terminalBeforeMs);
        resolver.call(
                FTContentProvider.getUriViewData(),
                FTContentProvider.METHOD_HISTORICAL_EXIT_CLEANUP,
                null,
                extras);
    }

    private void update(String exitKey, String ownerRunId, int state,
                        long leaseUntilMs, String dropReason, long nowMs) {
        Bundle extras = new Bundle();
        extras.putString("exit_key", exitKey);
        extras.putString("owner_run_id", ownerRunId);
        extras.putInt("state", state);
        extras.putLong("lease_until_ms", leaseUntilMs);
        extras.putString("drop_reason", dropReason);
        extras.putLong("updated_at_ms", nowMs);
        resolver.call(
                FTContentProvider.getUriViewData(),
                FTContentProvider.METHOD_HISTORICAL_EXIT_UPDATE,
                null,
                extras);
    }
}
