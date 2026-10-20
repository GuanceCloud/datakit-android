package com.ft.sdk;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Serializes RUM ownership with shutdown; no network I/O is performed under LOCK. */
final class FTURLConnectionResourceReporter {
    static final Object LOCK = new Object();
    static final int MAX_REQUESTS = 1000;
    static final long IDLE_NANOS = TimeUnit.MINUTES.toNanos(5);
    private static long epoch;
    private static boolean accepting;
    private static final HashMap<String, FTURLConnectionRequestState> ACTIVE = new HashMap<>();
    private static final HashSet<String> PENDING_STARTS = new HashSet<>();
    private static ScheduledExecutorService cleaner;

    static final FTURLConnectionRequestState.Reporter REPORTER = new FTURLConnectionRequestState.Reporter() {
        @Override public boolean start(FTURLConnectionRequestState state) {
            try {
                if (state.generation != epoch || ACTIVE.size() >= MAX_REQUESTS
                        || PENDING_STARTS.size() >= MAX_REQUESTS
                        || !FTURLConnectionInstrumentation.enabled(new URL(state.url))
                        || state.generation != epoch) return false;
                if (cleaner == null) {
                    cleaner = Executors.newSingleThreadScheduledExecutor(task -> {
                        Thread thread = new Thread(task, "FTURLConnectionCleanup");
                        thread.setDaemon(true);
                        return thread;
                    });
                    cleaner.scheduleWithFixedDelay(() -> cleanup(System.nanoTime()), 1, 1, TimeUnit.MINUTES);
                }
                ACTIVE.put(state.id, state);
                PENDING_STARTS.add(state.id);
                FTRUMInnerManager.get().startURLConnectionResource(state.id, state.startedAt, state.startedWallTime);
                if (state.visitorId != null) FTRUMInnerManager.get().setResourceVisitorId(state.id, state.visitorId);
                return true;
            } catch (Throwable ignored) {
                PENDING_STARTS.remove(state.id);
                discard(state);
                return false;
            }
        }

        @Override public void finish(FTURLConnectionRequestState state, ResourceParams params, NetStatusBean timing) {
            EventConsumerThreadPool.get().execute(() -> {
                synchronized (LOCK) {
                    if (state.generation != epoch || ACTIVE.remove(state.id) == null) return;
                    try {
                        // View/Action creation and pending increments precede this task on the same queue.
                        FTRUMInnerManager manager = FTRUMInnerManager.get();
                        if (state.traceId != null && state.spanId != null) {
                            FTTraceManager.get().putTraceHandler(state.id,
                                    new TraceContext.Simple(null, state.traceId, state.spanId));
                        }
                        manager.stopURLConnectionResource(state.id);
                        manager.addResource(state.id, params, timing);
                    } catch (Throwable ignored) {
                        discard(state);
                    }
                }
            });
        }

        @Override public void discard(FTURLConnectionRequestState state) {
            ACTIVE.remove(state.id);
            FTRUMInnerManager.get().discardResource(state.id);
        }
    };

    static void activate() {
        synchronized (LOCK) { accepting = true; }
    }

    static boolean isAccepting() {
        synchronized (LOCK) { return accepting; }
    }

    static long generation() {
        synchronized (LOCK) { return epoch; }
    }

    static void cleanup(long now) {
        synchronized (LOCK) {
            for (FTURLConnectionRequestState state : new ArrayList<>(ACTIVE.values())) {
                if (now - state.lastProgress >= IDLE_NANOS) state.discard();
            }
        }
    }

    static void pendingStartProcessed(String id) {
        synchronized (LOCK) { PENDING_STARTS.remove(id); }
    }

    static int pendingStartCount() {
        synchronized (LOCK) { return PENDING_STARTS.size(); }
    }

    static int activeCount() {
        synchronized (LOCK) { return ACTIVE.size(); }
    }

    static void shutdown() {
        synchronized (LOCK) {
            accepting = false;
            epoch++;
            if (cleaner != null) {
                cleaner.shutdownNow();
                cleaner = null;
            }
            for (FTURLConnectionRequestState state : new ArrayList<>(ACTIVE.values())) state.discard();
            ACTIVE.clear();
            PENDING_STARTS.clear();
            FTURLConnectionInstrumentation.clearConnections();
        }
    }
}
