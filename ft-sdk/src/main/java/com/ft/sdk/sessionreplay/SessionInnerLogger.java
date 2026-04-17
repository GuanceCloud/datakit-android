package com.ft.sdk.sessionreplay;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SessionInnerLogger implements InternalLogger {
    private final List<String> cachedList = new CopyOnWriteArrayList<>();

    @Override
    public void i(String tag, String message) {
        i(tag, message, false);
    }

    @Override
    public void i(String tag, String message, boolean onlyOnce) {
        if (onlyOnce && checkCached(message)) {
            return;
        }
        LogUtils.i(Constants.LOG_TAG_PREFIX + tag, message);

    }

    @Override
    public void d(String tag, String message) {
        d(tag, message, false);
    }

    @Override
    public void d(String tag, String message, boolean onlyOnce) {
        if (onlyOnce && checkCached(message)) {
            return;
        }
        LogUtils.d(Constants.LOG_TAG_PREFIX + tag, message);

    }

    @Override
    public void e(String tag, String message) {
        e(tag, message, false);
    }

    @Override
    public void e(String tag, String message, boolean onlyOnce) {
        if (onlyOnce && checkCached(message)) {
            return;
        }
        LogUtils.e(Constants.LOG_TAG_PREFIX + tag, message);
    }

    @Override
    public void e(String tag, String message, Throwable e) {
        e(tag, message, e, false);
    }

    @Override
    public void e(String tag, String message, Throwable e, boolean onlyOnce) {
        e(tag, message + "\n" + LogUtils.getStackTraceString(e), onlyOnce);
    }

    @Override
    public void v(String tag, String message) {
        v(tag, message, false);
    }

    @Override
    public void v(String tag, String message, boolean onlyOnce) {
        if (onlyOnce && checkCached(message)) {
            return;
        }
        LogUtils.v(Constants.LOG_TAG_PREFIX + tag, message);

    }

    @Override
    public void w(String tag, String message) {
        w(tag, message, false);
    }

    @Override
    public void w(String tag, String message, boolean onlyOnce) {
        if (onlyOnce && checkCached(message)) {
            return;
        }
        LogUtils.w(Constants.LOG_TAG_PREFIX + tag, message);
    }

    @Override
    public void w(String tag, String message, Throwable e, boolean onlyOnce) {
        w(tag, message + "\n" + LogUtils.getStackTraceString(e), onlyOnce);
    }

    @Override
    public void w(String tag, String message, Throwable e) {
        w(tag, message, e, false);
    }

    private boolean checkCached(String message) {
        if (!cachedList.contains(message)) {
            cachedList.add(message);
            return false;
        }
        return true;
    }
}

