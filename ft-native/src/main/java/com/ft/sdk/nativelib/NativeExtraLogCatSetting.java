package com.ft.sdk.nativelib;

/**
 * 额外 log cat日志配置
 */
public class NativeExtraLogCatSetting {

    private static final int MINI_LINES = 0;
    private static final int MAX_LINES = 500;

    private final int logcatMainLines;
    private final int logcatSystemLines;
    private final int logcatEventsLines;

    /**
     * @param logcatMainLines   这是主要的日志缓冲区，包含大部分应用程序的日志输出，[0,500]，default 200
     * @param logcatSystemLines 系统日志缓冲区，包含系统级别的日志信息，[0,500]，default 50
     * @param logcatEventsLines 事件日志缓冲区，主要记录特定的事件信息，[0,500]，default 50
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