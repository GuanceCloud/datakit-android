package com.ft.sdk.garble.utils;

import android.util.Log;

import com.ft.sdk.garble.FTExceptionHandler;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.manager.TrackLogManager;

/**
 * create: by huangDianHua
 * time: 2020/6/15 18:17:25
 * description:该类用于本地日志打印，同时供 AOP 方式插桩替换应用中的 android.util.Log 类
 */
public class TrackLog {
    public static int i(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.I);
    }

    public static int i(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.I);
    }

    public static int d(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.D);
    }

    public static int d(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.D);
    }

    public static int v(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.V);
    }

    public static int v(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.V);
    }

    public static int e(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.E);
    }

    public static int e(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.E);
    }

    public static int w(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.W);
    }

    public static int w(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.W);
    }

    public static int println(String tag, String msg) {
        return println(true,Log.INFO,tag,msg);
    }
    public static int println(boolean upload, int priority, String tag, String msg) {
        if (upload && FTExceptionHandler.get().isTrackConsoleLog()) {
            LogBean logBean = new LogBean(Constants.USER_AGENT, Utils.translateFieldValue(msg), System.currentTimeMillis());
            logBean.setServiceName(FTExceptionHandler.get().getTrackServiceName());
            logBean.setStatus(getStatus(priority));
            logBean.setEnv(FTExceptionHandler.get().getEnv());
            TrackLogManager.get().trackLog(logBean);
        }
        return Log.println(priority, tag, msg);
    }

    protected static int showFullLog(String TAG, String message, LogType logType) {
        return showFullLog(false, TAG, message, logType);
    }

    protected static int showFullLog(boolean upload, String TAG, String message, LogType logType) {
        int segmentSize = 3 * 1024;
        int length = message.length();
        if (length <= segmentSize) {
            showLog(upload, TAG, message, logType);
        } else {
            boolean isFirst = true;
            while (message.length() > segmentSize) {
                String logContent = message.substring(0, segmentSize);
                message = message.replace(logContent, "");
                if (isFirst) {
                    showLog(upload, TAG, logContent, logType);
                } else {
                    showLog(upload, null, logContent, logType);
                }
                isFirst = false;
            }
            showLog(upload, null, message, logType);
        }
        return length;
    }

    private static int showLog(boolean upload, String tag, String message, LogType logType) {
        switch (logType) {
            case E:
                return println(upload, Log.ERROR, tag, message);
            case I:
                return println(upload, Log.INFO, tag, message);
            case V:
                return println(upload, Log.VERBOSE, tag, message);
            case W:
                return println(upload, Log.WARN, tag, message);
            default:
                return println(upload, Log.DEBUG, tag, message);
        }
    }

    private static Status getStatus(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return Status.OK;
            case Log.INFO:
            case Log.DEBUG:
                return Status.INFO;
            case Log.ERROR:
                return Status.ERROR;
            case Log.WARN:
                return Status.WARNING;
            default:
                return Status.CRITICAL;
        }
    }

    enum LogType {
        I, D, E, V, W
    }
}
