package com.ft.sdk;

/**
 * 额外 log cat日志配置
 */
public class ExtraLogCatSetting {

    private static final int MINI_LINES = 0;
    private static final int MAX_LINES = 500;

    private int logcatMainLines = 200;
    private int logcatSystemLines = 50;
    private int logcatEventsLines = 50;

    /**
     * logcatMainLines 默认 200
     * logcatSystemLines 默认 50
     * logcatEventsLines 默认 50
     * 自定义，请使用 {@link #ExtraLogCatSetting(int, int, int)}
     */

    public ExtraLogCatSetting() {

    }

    /**
     * @param logcatMainLines   这是主要的日志缓冲区，包含大部分应用程序的日志输出，[0,500]，默认 200
     * @param logcatSystemLines 系统日志缓冲区，包含系统级别的日志信息，[0,500]，默认 50
     * @param logcatEventsLines 事件日志缓冲区，主要记录特定的事件信息，[0,500]，默认 50
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