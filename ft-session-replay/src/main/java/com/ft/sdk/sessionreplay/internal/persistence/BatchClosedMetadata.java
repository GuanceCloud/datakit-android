package com.ft.sdk.sessionreplay.internal.persistence;

public class BatchClosedMetadata {
    private final long lastTimeWasUsedInMs;
    private final boolean forcedNew;
    private final long eventsCount;

    public BatchClosedMetadata(long lastTimeWasUsedInMs, boolean forcedNew, long eventsCount) {
        this.lastTimeWasUsedInMs = lastTimeWasUsedInMs;
        this.forcedNew = forcedNew;
        this.eventsCount = eventsCount;
    }

    public long getLastTimeWasUsedInMs() {
        return lastTimeWasUsedInMs;
    }

    public boolean isForcedNew() {
        return forcedNew;
    }

    public long getEventsCount() {
        return eventsCount;
    }

    @Override
    public String toString() {
        return "BatchClosedMetadata{" +
                "lastTimeWasUsedInMs=" + lastTimeWasUsedInMs +
                ", forcedNew=" + forcedNew +
                ", eventsCount=" + eventsCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BatchClosedMetadata that = (BatchClosedMetadata) o;

        if (lastTimeWasUsedInMs != that.lastTimeWasUsedInMs) return false;
        if (forcedNew != that.forcedNew) return false;
        return eventsCount == that.eventsCount;
    }

    @Override
    public int hashCode() {
        int result = (int) (lastTimeWasUsedInMs ^ (lastTimeWasUsedInMs >>> 32));
        result = 31 * result + (forcedNew ? 1 : 0);
        result = 31 * result + (int) (eventsCount ^ (eventsCount >>> 32));
        return result;
    }
}
