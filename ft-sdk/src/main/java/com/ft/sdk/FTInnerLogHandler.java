package com.ft.sdk;

/**
 * 内部日志输出接口
 */
public interface FTInnerLogHandler {
    /**
     *
     * @param level 日志等级，输出 I, D, E, V, W
     * @param tag 日志标签
     * @param logContent 日志内容
     */
    void printInnerLog(String level, String tag, String logContent);
}
