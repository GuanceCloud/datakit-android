package com.ft.sdk.garble.utils;

import android.util.Log;

import com.ft.sdk.FTInnerLogHandler;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTLoggerConfigManager;
import com.ft.sdk.SDKLogLevel;
import com.ft.sdk.TrackLogManager;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * create: by huangDianHua
 * time: 2020/6/15 18:17:25
 * description: This class is only for AOP instrumentation to replace android.util.Log class in applications
 */
public class TrackLog {
    private final static List<String> cachedList = new CopyOnWriteArrayList<>();

    /*This method cannot be changed arbitrarily, changes need to be synchronized with the corresponding instrumentation method in the plugin*/
    public static int i(String tag, String msg) {
        return showFullLog(true, tag, msg, SDKLogLevel.I);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized with the corresponding instrumentation method in the plugin*/
    public static int i(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + LogUtils.getStackTraceString(e), SDKLogLevel.I);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized with the corresponding instrumentation method in the plugin*/
    public static int d(String tag, String msg) {
        return showFullLog(true, tag, msg, SDKLogLevel.D);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized with the corresponding instrumentation method in the plugin*/
    public static int d(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + LogUtils.getStackTraceString(e), SDKLogLevel.D);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized with the corresponding instrumentation method in the plugin*/
    public static int v(String tag, String msg) {
        return showFullLog(true, tag, msg, SDKLogLevel.V);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized with the corresponding instrumentation method in the plugin*/
    public static int v(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + LogUtils.getStackTraceString(e), SDKLogLevel.V);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized with the corresponding instrumentation method in the plugin*/
    public static int e(String tag, String msg) {
        return showFullLog(true, tag, msg, SDKLogLevel.E);
    }

    public static int e(String tag, String msg, boolean onlyOnce) {
        return showFullLog(true, tag, msg, SDKLogLevel.E);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized
     * with the corresponding instrumentation method in the plugin*/
    public static int e(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + LogUtils.getStackTraceString(e), SDKLogLevel.E);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized
     * with the corresponding instrumentation method in the plugin*/
    public static int w(String tag, String msg) {
        return showFullLog(true, tag, msg, SDKLogLevel.W);
    }

    public static int w(String tag, String msg, boolean onlyOnce) {
        return showFullLog(true, tag, msg, SDKLogLevel.W);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized
     * with the corresponding instrumentation method in the plugin*/
    public static int w(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + LogUtils.getStackTraceString(e), SDKLogLevel.W);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized
     * with the corresponding instrumentation method in the plugin*/
    public static int w(String tag, Throwable e) {
        return showFullLog(true, tag, LogUtils.getStackTraceString(e), SDKLogLevel.W);
    }

    /*This method cannot be changed arbitrarily, changes need to be synchronized
     * with the corresponding instrumentation method in the plugin*/
    public static int println(String tag, String msg) {
        return println(true, Log.INFO, tag, msg);
    }


    /*This method cannot be changed arbitrarily, changes need to be synchronized
     * with the corresponding instrumentation method in the plugin*/
    public static int println(int priority, String tag, String msg) {
        //Only collect VERBOSE, DEBUG, INFO, WARN, ERROR
        return println(priority >= Log.VERBOSE && priority <= Log.ERROR, priority, tag, msg);
    }

    /**
     * @param upload
     * @param priority
     * @param tag
     * @param msg
     * @return
     */
    public static int println(boolean upload, int priority, String tag, String msg) {
        FTLoggerConfig config = FTLoggerConfigManager.get().getConfig();
        if (upload && config != null && config.isEnableConsoleLog()) {
            LogBean logBean = new LogBean(getLevelMark(priority) + "/" + tag + ":" + msg, Utils.getCurrentNanoTime());
            logBean.setServiceName(config.getServiceName());
            logBean.setStatus(getStatus(priority).name);
            if (config.checkLogLevel(logBean.getStatus()) && config.checkPrefix(msg)) {
                TrackLogManager.get().trackLog(logBean, false);
            }
        }
        return Log.println(priority, tag, msg);
    }

    private static FTInnerLogHandler innerLogHandler;

    public static void setInnerLogHandler(FTInnerLogHandler innerLogHandler) {
        TrackLog.innerLogHandler = innerLogHandler;
    }

    public static boolean isSetInnerLogHandler() {
        return TrackLog.innerLogHandler != null;
    }

    /**
     * @param TAG
     * @param message
     * @param logType
     * @return
     */
    protected static int showFullInnerlog(String TAG, String message, SDKLogLevel logType, boolean onlyOnce) {
        if (isSetInnerLogHandler()) {
            innerLogHandler.printInnerLog(logType.toString(), TAG, message);
        }

        if (onlyOnce && checkCached(message)) {
            return 0;
        }
        return showFullLog(false, TAG, message, logType);
    }

    /**
     * @param upload
     * @param TAG
     * @param message
     * @param logType
     * @return
     */
    protected static int showFullLog(boolean upload, String TAG, String message, SDKLogLevel logType) {
        //The system limits the length of a single log message to 4 × 1024 characters.
        // The liblog package used by Logcat also mentions that messages printed with Log may be truncated by the log kernel driver.
        int segmentSize = 4000;
        int length = message != null ? message.length() : 0;
        if (length == 0) return 0;
        if (length <= segmentSize) {
            showLog(upload, TAG, message, logType);
        } else {
            boolean isFirst = true;
            int startIndex = 0;
            int messageLength = message.length();

            while (startIndex < messageLength) {
                int endIndex = Math.min(startIndex + segmentSize, messageLength);
                String logContent = message.substring(startIndex, endIndex);

                if (isFirst) {
                    showLog(upload, TAG, logContent, logType);
                    isFirst = false;
                } else {
                    showLog(upload, null, logContent, logType);
                }

                startIndex = endIndex;
            }
        }
        return length;
    }

    /**
     * @param upload
     * @param tag
     * @param message
     * @param logType
     * @return
     */
    private static int showLog(boolean upload, String tag, String message, SDKLogLevel logType) {
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

    /**
     * @param priority
     * @return
     */
    private static Status getStatus(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return Status.OK;
            case Log.INFO:
                return Status.INFO;
            case Log.DEBUG:
                return Status.DEBUG;
            case Log.ERROR:
                return Status.ERROR;
            case Log.WARN:
                return Status.WARNING;
            default:
                return Status.CRITICAL;
        }
    }

    /**
     * @param priority
     * @return
     */
    private static String getLevelMark(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.ERROR:
                return "E";
            case Log.WARN:
                return "W";
            case Log.ASSERT:
                return "A";
            default:
                return "I";
        }
    }

//    /**
//     * Log type
//     * 「」
//     */
//    enum LogType {
//        I, D, E, V, W
//    }

    private static boolean checkCached(String message) {
        if (!cachedList.contains(message)) {
            cachedList.add(message);
            return false;
        }
        return true;
    }

}
