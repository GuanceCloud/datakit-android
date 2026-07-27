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

    /**
     * Returns the stable high-level category of this issue.
     *
     * @return {@link FTIssueCategory#CRASH} or {@link FTIssueCategory#ANR}
     */
    @NonNull
    public FTIssueCategory getCategory() {
        return category;
    }

    /**
     * Returns the value used by the resulting RUM Error's {@code error.type} field.
     *
     * @return an SDK error type such as {@code java_crash}, {@code native_crash},
     * {@code anr_error}, or {@code anr_crash}
     */
    @NonNull
    public String getErrorType() {
        return errorType;
    }

    /**
     * Returns the value used by the resulting RUM Error's {@code error.message} field.
     *
     * @return the issue message, or {@code null} when no message is available
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * Returns the diagnostic text used by the resulting RUM Error's {@code error.stack} field.
     *
     * <p>The value may include multiple thread stacks and configured Logcat output, so providers
     * should avoid copying or performing expensive processing on it.</p>
     *
     * @return the stack and diagnostic text; never {@code null}
     */
    @NonNull
    public String getStack() {
        return stack;
    }

    /**
     * Returns the issue occurrence time as Unix epoch nanoseconds.
     *
     * @return the occurrence timestamp, not the later recovery or upload timestamp
     */
    public long getOccurredAtNanoseconds() {
        return occurredAtNanoseconds;
    }

    /**
     * Returns the application state captured when the issue occurred.
     *
     * @return the value used by the resulting RUM Error's {@code error.situation} tag:
     * {@code unknown}, {@code startup}, {@code run}, or {@code background}
     */
    @NonNull
    public String getAppState() {
        return appState;
    }

    /**
     * Returns the thread associated with this issue.
     *
     * <p>For a Java crash this is the uncaught-exception thread; for a live ANR this is the
     * blocked main thread. Native crash and ANR records may not contain a thread name.</p>
     *
     * @return the thread name, or {@code null} when unavailable
     */
    @Nullable
    public String getThreadName() {
        return threadName;
    }

    /**
     * Returns whether this issue was restored from a record created by an earlier process.
     *
     * <p>This does not indicate that a current-process issue was merely cached for later upload.
     * When this method returns {@code true}, current in-memory application context belongs to a
     * later process and may not describe the original issue.</p>
     *
     * @return {@code true} for an issue restored after process restart; otherwise {@code false}
     */
    public boolean isHistorical() {
        return historical;
    }
}
