package com.ft.sdk.sessionreplay.webview;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DataBatcher {


    private final int MAX_BATCH_SIZE;
    private final long TIMEOUT_MS;
    private final boolean FLUSH_ON_VIEW_SWITCH;

    private final ScheduledExecutorService scheduler;
    private final Map<String, Batch> batchMap = new ConcurrentHashMap<>();
    private final RecordWriter writer;

    private String lastViewId = null;

    public DataBatcher(RecordWriter writer) {
        this(writer, 20, 500, true);
    }

    public DataBatcher(RecordWriter writer, int maxBatchSize, long timeoutMs, boolean flushOnViewSwitch) {
        this.writer = writer;
        this.MAX_BATCH_SIZE = maxBatchSize;
        this.TIMEOUT_MS = timeoutMs;
        this.FLUSH_ON_VIEW_SWITCH = flushOnViewSwitch;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void onData(SessionReplayRumContext context, MobileRecord data) {
        if (FLUSH_ON_VIEW_SWITCH) {
            synchronized (this) {
                if (lastViewId != null && !lastViewId.equals(context.getViewId())) {
                    Batch prev = batchMap.get(lastViewId);
                    if (prev != null) prev.flushNow();
                }
                lastViewId = context.getViewId();
            }
        }

        Batch batch = batchMap.get(context.getViewId());
        if (batch == null) {
            Batch created = new Batch(context);
            Batch existing = batchMap.get(context.getViewId());
            batch = (existing != null) ? existing : created;
        }
        batch.addData(data);
    }


    public void flush(String viewId) {
        Batch batch = batchMap.get(viewId);
        if (batch != null) batch.flushNow();
    }


    public void shutdown() {
        for (Batch b : batchMap.values()) {
            b.flushNow();
        }
        scheduler.shutdownNow();
        batchMap.clear();
    }

    private class Batch {
        private final SessionReplayRumContext context;
        private final List<MobileRecord> buffer = new ArrayList<>();
        private ScheduledFuture<?> flushTask;

        Batch(SessionReplayRumContext context) {
            this.context = context;
        }

        synchronized void addData(MobileRecord data) {
            buffer.add(data);

            if (buffer.size() >= MAX_BATCH_SIZE) {
                flushNow();
                return;
            }

            if (flushTask != null) {
                flushTask.cancel(false);
                flushTask = null;
            }
            flushTask = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    flushTimeout();
                }
            }, TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }

        private synchronized void flushTimeout() {
            doFlush();
        }

        synchronized void flushNow() {
            if (flushTask != null) {
                flushTask.cancel(false);
                flushTask = null;
            }
            doFlush();
        }

        private void doFlush() {
            List<MobileRecord> toSend;
            synchronized (this) {
                if (buffer.isEmpty()) return;
                toSend = new ArrayList<>(buffer);
                buffer.clear();
            }

            try {
                writer.write(new EnrichedRecord(context.getApplicationId(),
                        context.getSessionId(), context.getViewId(), true, toSend));

            } catch (Throwable t) {
                t.printStackTrace();
            }
            Batch cur = batchMap.get(context.getViewId());
            if (cur == this) {
                batchMap.remove(context.getViewId());
            }
        }
    }
}