package com.ft.sdk.internal.anr.historical;

import android.content.Context;

import androidx.annotation.RestrictTo;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.db.file.FTFileStorePaths;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class HistoricalAnrCoordinator {
    interface Clock {
        long nowMs();
    }

    private static final String TAG = Constants.LOG_TAG_PREFIX + "HistoricalAnr";
    private static final int MAX_RECORDS = 32;
    private static final int MAX_TRACE_BYTES = 1024 * 1024;
    private static final long CLOCK_TOLERANCE_MS = 5;
    private static final long MAX_VIEW_DISTANCE_MS = 5 * 60 * 1000L;
    private static final long CLAIM_LEASE_MS = 60 * 1000L;
    private static final long RETRY_DELAY_MS = 60 * 1000L;
    private static final long TERMINAL_RETENTION_MS = 90L * 24 * 60 * 60 * 1000;

    private final String packageName;
    private final ProcessRunIdentity processIdentity;
    private final ProcessExitSource exitSource;
    private final HistoricalRumContextStore contextStore;
    private final HistoricalExitLedger ledger;
    private final HistoricalAnrReporter reporter;
    private final Executor executor;
    private final Clock clock;
    private final AtomicBoolean scheduled = new AtomicBoolean(false);
    private volatile boolean released;

    HistoricalAnrCoordinator(String packageName,
                             ProcessRunIdentity processIdentity,
                             ProcessExitSource exitSource,
                             HistoricalRumContextStore contextStore,
                             HistoricalExitLedger ledger,
                             HistoricalAnrReporter reporter,
                             Executor executor,
                             Clock clock) {
        this.packageName = packageName;
        this.processIdentity = processIdentity;
        this.exitSource = exitSource;
        this.contextStore = contextStore;
        this.ledger = ledger;
        this.reporter = reporter;
        this.executor = executor;
        this.clock = clock;
    }

    public static HistoricalAnrCoordinator create(Context context) {
        HistoricalExitLedger ledger =
                new FileHistoricalExitLedger(new FTFileStorePaths(context));
        HistoricalRumContextStore contextStore =
                new V4MigratingHistoricalRumContextStore(
                        context, FileHistoricalRumContextStore.get(context));
        return new HistoricalAnrCoordinator(
                context.getPackageName(),
                ProcessRunIdentity.get(),
                new AndroidProcessExitSource(context),
                contextStore,
                ledger,
                new DefaultHistoricalAnrReporter(),
                new Executor() {
                    @Override
                    public void execute(Runnable command) {
                        EventConsumerThreadPool.get().execute(command);
                    }
                },
                new Clock() {
                    @Override
                    public long nowMs() {
                        return System.currentTimeMillis();
                    }
                });
    }

    public void consumePending() {
        if (released || !scheduled.compareAndSet(false, true)) {
            return;
        }
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    consumeInBackground();
                }
            });
        } catch (RuntimeException e) {
            scheduled.set(false);
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void shutdown() {
        released = true;
    }

    private void consumeInBackground() {
        if (released) {
            return;
        }
        long nowMs = clock.nowMs();
        try {
            contextStore.ensureReady();
            recoverPendingCommits();
            if (released) {
                return;
            }
            List<ProcessExitRecord> loaded = exitSource.load();
            ArrayList<ProcessExitRecord> exits = new ArrayList<>();
            if (loaded != null) {
                for (ProcessExitRecord exit : loaded) {
                    if (exit != null && exit.getReason() == ProcessExitRecord.REASON_ANR) {
                        exits.add(exit);
                    }
                }
            }
            Collections.sort(exits, new Comparator<ProcessExitRecord>() {
                @Override
                public int compare(ProcessExitRecord left, ProcessExitRecord right) {
                    return Long.compare(left.getTimestampMs(), right.getTimestampMs());
                }
            });
            int processed = 0;
            for (int i = 0; i < exits.size() && !released && processed < MAX_RECORDS; i++) {
                if (consumeOne(exits.get(i))) {
                    processed++;
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        } finally {
            if (!released) {
                ledger.cleanup(nowMs - TERMINAL_RETENTION_MS);
            }
        }
    }

    private boolean consumeOne(ProcessExitRecord exit) {
        String exitKey = ExitKeyFactory.create(packageName, exit);
        String eventDedupeKey = "historical-anr:v1:" + exitKey;
        HistoricalRumContext context = null;
        boolean contextLoadFailed = false;
        List<HistoricalRumContext> views = Collections.emptyList();
        try {
            views = contextStore.load(exit.getProcessName());
        } catch (HistoricalAnrMigrationException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            return false;
        } catch (HistoricalRumContextLoadException e) {
            views = e.getLoadedContexts();
            contextLoadFailed = true;
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        } catch (Exception e) {
            contextLoadFailed = true;
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        context = HistoricalViewMatcher.match(
                views,
                exit,
                processIdentity.getProcessRunId(),
                CLOCK_TOLERANCE_MS,
                MAX_VIEW_DISTANCE_MS);

        long nowMs = clock.nowMs();
        if (released) {
            return false;
        }
        HistoricalExitLedger.Claim claim = ledger.tryClaim(
                exitKey,
                processIdentity.getProcessRunId(),
                nowMs,
                nowMs + CLAIM_LEASE_MS,
                exit,
                context,
                eventDedupeKey);
        if (claim.isCommitted()) {
            HistoricalRumContext committedContext = claim.getContext() == null
                    ? context
                    : claim.getContext();
            if (committedContext != null) {
                completeCommittedEvent(
                        HistoricalAnrReporter.ReportResult.ALREADY_EXISTS,
                        exitKey,
                        eventDedupeKey,
                        committedContext.getCollectType());
            }
            return false;
        }
        if (!claim.isAcquired()) {
            return false;
        }
        context = claim.getContext();
        if (released) {
            return true;
        }
        if (contextLoadFailed && context == null) {
            markRetry(exitKey);
            return true;
        }
        if (context == null) {
            ledger.markDropped(exitKey, processIdentity.getProcessRunId(),
                    "no_context", clock.nowMs());
            return true;
        }

        try {
            String stack = readTrace(exit.openTrace());
            if (stack == null || stack.length() == 0) {
                ledger.markDropped(exitKey, processIdentity.getProcessRunId(),
                        "no_trace", clock.nowMs());
                return true;
            }
            HistoricalAnrReporter.ReportResult result =
                    reporter.report(exit, context, stack, eventDedupeKey);
            if (result == HistoricalAnrReporter.ReportResult.INSERTED
                    || result == HistoricalAnrReporter.ReportResult.ALREADY_EXISTS) {
                if (ledger.markCommitted(
                        exitKey, processIdentity.getProcessRunId(), clock.nowMs())) {
                    completeCommittedEvent(
                            result, exitKey, eventDedupeKey, context.getCollectType());
                } else {
                    markRetry(exitKey);
                }
            } else {
                markRetry(exitKey);
            }
        } catch (IOException e) {
            markRetry(exitKey);
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        } catch (RuntimeException e) {
            markRetry(exitKey);
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return true;
    }

    private void recoverPendingCommits() {
        List<HistoricalExitLedger.PendingCommit> pending = ledger.loadPendingCommits();
        if (pending == null) {
            return;
        }
        for (HistoricalExitLedger.PendingCommit commit : pending) {
            if (released) {
                return;
            }
            completeCommittedEvent(
                    HistoricalAnrReporter.ReportResult.ALREADY_EXISTS,
                    commit.getExitKey(),
                    commit.getEventDedupeKey(),
                    commit.getCollectType());
        }
    }

    private boolean completeCommittedEvent(HistoricalAnrReporter.ReportResult result,
                                           String exitKey,
                                           String eventDedupeKey,
                                           CollectType collectType) {
        boolean visible = collectType == CollectType.NOT_COLLECT
                || reporter.onCommitted(result, eventDedupeKey, collectType);
        return visible && ledger.markEventVisible(
                exitKey, eventDedupeKey, clock.nowMs());
    }

    private void markRetry(String exitKey) {
        long nowMs = clock.nowMs();
        ledger.markRetry(exitKey, processIdentity.getProcessRunId(),
                nowMs + RETRY_DELAY_MS, nowMs);
    }

    private String readTrace(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        try (InputStream input = stream;
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8 * 1024];
            int remaining = MAX_TRACE_BYTES;
            while (remaining > 0) {
                int read = input.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read == -1) {
                    break;
                }
                output.write(buffer, 0, read);
                remaining -= read;
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
