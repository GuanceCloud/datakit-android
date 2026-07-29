package com.ft.sdk.internal.anr.historical;

import androidx.annotation.RestrictTo;

import java.util.UUID;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class ProcessRunIdentity {
    private static volatile ProcessRunIdentity current;

    private final String processName;
    private final String processRunId;
    private final long processStartMs;

    ProcessRunIdentity(String processName, String processRunId, long processStartMs) {
        this.processName = processName;
        this.processRunId = processRunId;
        this.processStartMs = processStartMs;
    }

    public static ProcessRunIdentity initialize(String processName, long processStartMs) {
        ProcessRunIdentity identity = new ProcessRunIdentity(
                processName, UUID.randomUUID().toString(), processStartMs);
        current = identity;
        return identity;
    }

    public static ProcessRunIdentity get() {
        return current;
    }

    public static void clear() {
        current = null;
    }

    public String getProcessName() {
        return processName;
    }

    public String getProcessRunId() {
        return processRunId;
    }

    public long getProcessStartMs() {
        return processStartMs;
    }
}
