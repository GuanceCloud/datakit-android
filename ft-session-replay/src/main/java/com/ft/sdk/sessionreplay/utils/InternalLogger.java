package com.ft.sdk.sessionreplay.utils;

public interface InternalLogger {

    void i(String tag, String message);

    void i(String tag, String message, boolean onlyOnce);

    void d(String tag, String message);

    void d(String tag, String message, boolean onlyOnce);

    void e(String tag, String message);

    void e(String tag, String message, boolean onlyOnce);

    void e(String tag, String message, Throwable e);

    void e(String tag, String message, Throwable e, boolean onlyOnce);

    void v(String tag, String message);

    void v(String tag, String message, boolean onlyOnce);

    void w(String tag, String message);

    void w(String tag, String message, boolean onlyOnce);

    void w(String tag, String message, Throwable e);

    void w(String tag, String message, Throwable e, boolean onlyOnce);

}

