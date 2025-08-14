package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

public class RecordedQueuedItemContext {

    private final long timestamp;
    private final SessionReplayRumContext newRumContext;

    public RecordedQueuedItemContext(long timestamp, SessionReplayRumContext newRumContext) {
        this.timestamp = timestamp;
        this.newRumContext = newRumContext;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public SessionReplayRumContext getNewRumContext() {
        return newRumContext;
    }
}
