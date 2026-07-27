package com.ft.sdk;

/**
 * Stable category for an automatically collected crash or ANR issue.
 */
public enum FTIssueCategory {
    /**
     * An automatically collected Java or native crash.
     */
    CRASH,

    /**
     * An ANR detected by the watchdog or collected from a native ANR record.
     */
    ANR
}
