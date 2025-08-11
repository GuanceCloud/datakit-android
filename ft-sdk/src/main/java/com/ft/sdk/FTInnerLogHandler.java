package com.ft.sdk;

/**
 * Internal log output interface
 */
public interface FTInnerLogHandler {
    /**
     *
     * @param level log level, outputs I, D, E, V, W
     * @param tag log tag
     * @param logContent log content
     */
    void printInnerLog(String level, String tag, String logContent);
}
