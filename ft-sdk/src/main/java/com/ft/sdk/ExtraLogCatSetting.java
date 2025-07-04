package com.ft.sdk;

/**
 * Extra log cat log configuration
 */
public class ExtraLogCatSetting {

    private static final int MINI_LINES = 0;
    private static final int MAX_LINES = 500;

    private int logcatMainLines = 200;
    private int logcatSystemLines = 50;
    private int logcatEventsLines = 50;

    /**
     * logcatMainLines default 200
     * logcatSystemLines default 50
     * logcatEventsLines default 50
     * Custom, please use {@link #ExtraLogCatSetting(int, int, int)}
     */

    public ExtraLogCatSetting() {

    }

    /**
     * @param logcatMainLines   This is the main log buffer, containing most of the application's log output, [0,500], default 200
     * @param logcatSystemLines System log buffer, containing system-level log information, [0,500], default 50
     * @param logcatEventsLines Event log buffer, mainly records specific event information, [0,500], default 50
     */
    public ExtraLogCatSetting(int logcatMainLines, int logcatSystemLines, int logcatEventsLines) {
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