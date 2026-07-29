package com.ft.sdk.internal.anr.historical;

import java.io.IOException;
import java.io.InputStream;

final class ProcessExitRecord {
    static final int REASON_ANR = 6;

    interface TraceSource {
        InputStream open() throws IOException;
    }

    private final String processName;
    private final int pid;
    private final long timestampMs;
    private final int reason;
    private final int importance;
    private final TraceSource traceSource;

    ProcessExitRecord(String processName, int pid, long timestampMs, int reason,
                      int importance, TraceSource traceSource) {
        this.processName = processName;
        this.pid = pid;
        this.timestampMs = timestampMs;
        this.reason = reason;
        this.importance = importance;
        this.traceSource = traceSource;
    }

    String getProcessName() {
        return processName;
    }

    int getPid() {
        return pid;
    }

    long getTimestampMs() {
        return timestampMs;
    }

    int getReason() {
        return reason;
    }

    int getImportance() {
        return importance;
    }

    InputStream openTrace() throws IOException {
        return traceSource == null ? null : traceSource.open();
    }
}
