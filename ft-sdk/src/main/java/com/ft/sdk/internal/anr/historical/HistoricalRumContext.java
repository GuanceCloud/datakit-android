package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.garble.bean.CollectType;

final class HistoricalRumContext {
    private final String processName;
    private final String processRunId;
    private final long processStartMs;
    private final String sessionId;
    private final String viewId;
    private final String viewName;
    private final long viewStartNs;
    private final long viewTimeSpentNs;
    private final boolean viewClosed;
    private final CollectType collectType;

    HistoricalRumContext(String processName, String processRunId, long processStartMs,
                         String sessionId, String viewId, String viewName, long viewStartNs,
                         long viewTimeSpentNs, boolean viewClosed) {
        this(processName, processRunId, processStartMs, sessionId, viewId, viewName,
                viewStartNs, viewTimeSpentNs, viewClosed, CollectType.COLLECT_BY_SAMPLE);
    }

    HistoricalRumContext(String processName, String processRunId, long processStartMs,
                         String sessionId, String viewId, String viewName, long viewStartNs,
                         long viewTimeSpentNs, boolean viewClosed, CollectType collectType) {
        this.processName = processName;
        this.processRunId = processRunId;
        this.processStartMs = processStartMs;
        this.sessionId = sessionId;
        this.viewId = viewId;
        this.viewName = viewName;
        this.viewStartNs = viewStartNs;
        this.viewTimeSpentNs = viewTimeSpentNs;
        this.viewClosed = viewClosed;
        this.collectType = collectType == null ? CollectType.NOT_COLLECT : collectType;
    }

    static HistoricalRumContext fromSnapshot(String processName, String sessionId,
                                             String viewId, String viewName,
                                             String collectType) {
        if (sessionId == null || viewId == null) {
            return null;
        }
        return new HistoricalRumContext(
                processName, null, 0, sessionId, viewId, viewName, 0, 0, true,
                CollectType.fromValue(collectType));
    }

    String getProcessName() {
        return processName;
    }

    String getProcessRunId() {
        return processRunId;
    }

    long getProcessStartMs() {
        return processStartMs;
    }

    String getSessionId() {
        return sessionId;
    }

    String getViewId() {
        return viewId;
    }

    String getViewName() {
        return viewName;
    }

    long getViewStartNs() {
        return viewStartNs;
    }

    long getViewTimeSpentNs() {
        return viewTimeSpentNs;
    }

    boolean isViewClosed() {
        return viewClosed;
    }

    CollectType getCollectType() {
        return collectType;
    }

    long getViewStartMs() {
        return viewStartNs / 1_000_000L;
    }

    long getViewEndMs() {
        return getViewStartMs() + Math.max(0, viewTimeSpentNs / 1_000_000L);
    }
}
