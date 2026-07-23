package com.ft.sdk.sessionreplay.utils;

/**
 * Logger abstraction used by Session Replay mappers and helpers for diagnostic messages.
 */
public interface InternalLogger {

    /**
     * Logs an informational message.
     */
    void i(String tag, String message);

    /**
     * Logs an informational message, optionally only once.
     */
    void i(String tag, String message, boolean onlyOnce);

    /**
     * Logs a debug message.
     */
    void d(String tag, String message);

    /**
     * Logs a debug message, optionally only once.
     */
    void d(String tag, String message, boolean onlyOnce);

    /**
     * Logs an error message.
     */
    void e(String tag, String message);

    /**
     * Logs an error message, optionally only once.
     */
    void e(String tag, String message, boolean onlyOnce);

    /**
     * Logs an error message with an associated throwable.
     */
    void e(String tag, String message, Throwable e);

    /**
     * Logs an error message with an associated throwable, optionally only once.
     */
    void e(String tag, String message, Throwable e, boolean onlyOnce);

    /**
     * Logs a verbose message.
     */
    void v(String tag, String message);

    /**
     * Logs a verbose message, optionally only once.
     */
    void v(String tag, String message, boolean onlyOnce);

    /**
     * Logs a warning message.
     */
    void w(String tag, String message);

    /**
     * Logs a warning message, optionally only once.
     */
    void w(String tag, String message, boolean onlyOnce);

    /**
     * Logs a warning message with an associated throwable.
     */
    void w(String tag, String message, Throwable e);

    /**
     * Logs a warning message with an associated throwable, optionally only once.
     */
    void w(String tag, String message, Throwable e, boolean onlyOnce);

}
