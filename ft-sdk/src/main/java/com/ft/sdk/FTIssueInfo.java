package com.ft.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Immutable facts about an automatically collected crash or ANR.
 * Instances are created by the SDK.
 */
public final class FTIssueInfo {
    @NonNull
    private final FTIssueCategory category;
    @NonNull
    private final String errorType;
    @Nullable
    private final String message;
    @NonNull
    private final String stack;
    private final long occurredAtNanoseconds;
    @NonNull
    private final String appState;
    @Nullable
    private final String threadName;
    private final boolean historical;

    FTIssueInfo(@NonNull FTIssueCategory category,
                @NonNull String errorType,
                @Nullable String message,
                @NonNull String stack,
                long occurredAtNanoseconds,
                @NonNull String appState,
                @Nullable String threadName,
                boolean historical) {
        this.category = category;
        this.errorType = errorType;
        this.message = message;
        this.stack = stack;
        this.occurredAtNanoseconds = occurredAtNanoseconds;
        this.appState = appState;
        this.threadName = threadName;
        this.historical = historical;
    }

    @NonNull
    public FTIssueCategory getCategory() {
        return category;
    }

    /**
     * Returns the value used by the resulting RUM Error's {@code error.type} field.
     */
    @NonNull
    public String getErrorType() {
        return errorType;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @NonNull
    public String getStack() {
        return stack;
    }

    /**
     * Returns the issue occurrence time as Unix epoch nanoseconds.
     */
    public long getOccurredAtNanoseconds() {
        return occurredAtNanoseconds;
    }

    @NonNull
    public String getAppState() {
        return appState;
    }

    @Nullable
    public String getThreadName() {
        return threadName;
    }

    /**
     * Returns whether this issue was restored from a record created by an earlier process.
     */
    public boolean isHistorical() {
        return historical;
    }
}
