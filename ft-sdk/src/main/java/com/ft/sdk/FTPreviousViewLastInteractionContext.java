package com.ft.sdk;

import androidx.annotation.NonNull;

/**
 * Immutable facts about a previous View's selected interaction. Times use the process-local
 * {@link System#nanoTime()} clock. Action names and custom properties are not included.
 */
public final class FTPreviousViewLastInteractionContext {
    private final String actionType;
    private final long interactionTimeNanos;
    private final long nextViewStartTimeNanos;

    /** Creates a context for an interaction and the following View. */
    public FTPreviousViewLastInteractionContext(@NonNull String actionType, long interactionTimeNanos,
                                               long nextViewStartTimeNanos) {
        if (actionType == null) throw new NullPointerException("actionType");
        this.actionType = actionType;
        this.interactionTimeNanos = interactionTimeNanos;
        this.nextViewStartTimeNanos = nextViewStartTimeNanos;
    }

    /** Returns the selected Action type: click, including automatic back-key events. */
    @NonNull
    public String getActionType() {
        return actionType;
    }

    /** Returns Action start, or the explicit stop time of a waiting Action, in monotonic nanoseconds. */
    public long getInteractionTimeNanos() {
        return interactionTimeNanos;
    }

    /** Returns the following View's start in monotonic nanoseconds. */
    public long getNextViewStartTimeNanos() {
        return nextViewStartTimeNanos;
    }
}
