package com.ft.sdk.nativelib;

/**
 * Additional logcat log configuration
 */
public class NativeExtraLogCatSetting {

    private static final int MINI_LINES = 0;
    private static final int MAX_LINES = 500;

    private final int logcatMainLines;
    private final int logcatSystemLines;
    private final int logcatEventsLines;

    /**
     * @param logcatMainLines   This is the main log buffer, containing most application log output, [0,500], default 200
     * @param logcatSystemLines System log buffer, containing system-level log information, [0,500], default 50
     * @param logcatEventsLines Event log buffer, mainly recording specific event information, [0,500], default 50
     */
    public NativeExtraLogCatSetting(int logcatMainLines, int logcatSystemLines, int logcatEventsLines) {
        this.logcatMainLines = Math.min(Math.max(logcatMainLines, MINI_LINES), MAX_LINES);
        this.logcatSystemLines = Math.min(Math.max(logcatSystemLines, MINI_LINES), MAX_LINES);
        this.logcatEventsLines = Math.min(Math.max(logcatEventsLines, MINI_LINES), MAX_LINES);
    }

    public int getLogcatMainLines() {
        return logcatMainLines;
    }

    public int getLogcatSystemLines() {
        return logcatSystemLines;
    }

    public int getLogcatEventsLines() {
        return logcatEventsLines;
    }
}