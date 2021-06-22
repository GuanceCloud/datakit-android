package com.ft.sdk.garble.utils;

import android.util.Log;

import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTLoggerConfigManager;
import com.ft.sdk.FTSdk;
import com.ft.sdk.TrackLogManager;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;

/**
 * create: by huangDianHua
 * time: 2020/6/15 18:17:25
 * description:该类仅供 AOP 方式插桩替换应用中的 android.util.Log 类
 */
public class TrackLog {
    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int i(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.I);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int i(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.I);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int d(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.D);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int d(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.D);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int v(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.V);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int v(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.V);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int e(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.E);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int e(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.E);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int w(String tag, String msg) {
        return showFullLog(true, tag, msg, LogType.W);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int w(String tag, String msg, Throwable e) {
        return showFullLog(true, tag, msg + "\n" + Log.getStackTraceString(e), LogType.W);
    }

    /*该方法不能随意改动，变化后需要同步更新插件中相应的插桩方法*/
    public static int println(String tag, String msg) {
        return println(true, Log.INFO, tag, msg);
    }

    public static int println(boolean upload, int priority, String tag, String msg) {
        FTLoggerConfig config = FTLoggerConfigManager.get().getConfig();
        if (upload && config != null && config.isEnableConsoleLog()) {
            LogBean logBean = new LogBean(Utils.translateFieldValue(Utils.getCurrentTimeStamp()
                    + " " + getLevelMark(priority) + "/" + tag + ":" + msg), Utils.getCurrentNanoTime());
            logBean.setServiceName(config.getServiceName());
            logBean.setStatus(getStatus(priority));
            logBean.setEnv(FTSdk.get().getBaseConfig().getEnv());
            TrackLogManager.get().trackLog(logBean);
        }
        return Log.println(priority, tag, msg);
    }

    public static int showFullLog(String TAG, String message, LogType logType) {
        return showFullLog(false, TAG, message, logType);
    }

    protected static int showFullLog(boolean upload, String TAG, String message, LogType logType) {
        int segmentSize = 4 * 1024;
        int length = message != null ? message.length() : 0;
        if (length == 0) return 0;
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
            default:
                return "I";
        }
    }

    enum LogType {
        I, D, E, V, W
    }
}
