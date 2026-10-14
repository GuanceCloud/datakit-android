package com.ft.sdk.diagnostics;

/** Immutable local limits. No SDK credentials or endpoints are accepted. */
public final class FTDiagnosticsConfig {
    public final long sessionBytes, totalBytes, retentionMs;
    public FTDiagnosticsConfig() { this(20L*1024*1024, 100L*1024*1024, 7L*24*60*60*1000); }
    public FTDiagnosticsConfig(long sessionBytes, long totalBytes, long retentionMs) {
        if (sessionBytes < 128*1024 || totalBytes < sessionBytes || retentionMs <= 0)
            throw new IllegalArgumentException("Invalid diagnostic limits");
        this.sessionBytes = sessionBytes; this.totalBytes = totalBytes; this.retentionMs = retentionMs;
    }
}
