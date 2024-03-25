package com.ft.sdk.garble.utils;


import static com.ft.sdk.garble.utils.TrackLog.showFullLog;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTInnerLogHandler;
import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.garble.manager.LogFileHelper;

import java.io.File;

/**
 * BY huangDianHua
 * DATE:2019-12-03 15:56
 * Description:负责内部日志输出
 */
public class LogUtils {
    protected static String TAG = "[FT-SDK]:";

    /**
     * 是否为调试模式，为 true 时，输出控制台日志
     */
    private static boolean mDebug = true;
    /**
     *
     */
    private static boolean aliasLogShow = false;

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }

    public static void setDescLogShow(boolean aliasLogShow) {
        LogUtils.aliasLogShow = aliasLogShow;
    }

    public static void i(String tag, String message) {
        i(tag, message, mDebug);
    }

    public static void d(String tag, String message) {
        d(tag, message, mDebug);
    }

    public static void e(String tag, String message) {
        e(tag, message, mDebug);
    }

    public static void v(String tag, String message) {
        v(tag, message, mDebug);
    }

    public static void w(String tag, String message) {
        w(tag, message, mDebug);
    }

    public static void i(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, TrackLog.LogType.I);
        }
    }

    public static void d(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, TrackLog.LogType.D);
        }
    }

    public static void e(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, TrackLog.LogType.E);
        }
    }

    public static void v(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, TrackLog.LogType.V);
        }
    }

    public static void w(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, TrackLog.LogType.W);
        }
    }


    public static void showAlias(String message) {
        if (aliasLogShow) {
            showFullLog(TAG, message, TrackLog.LogType.D);
        }
    }


    /**
     * 设置内部日志输出对象
     * <p>
     * 注意:为避免与
     * <p>
     * {@link FTLoggerConfig#setPrintCustomLogToConsole(boolean)}  产生循环调用的情况
     * innerLogHandler 设置后，这个值自动为 false
     * <p>
     * {@link FTLogger}，如果使用 FTLogger 避免使用 {@link  com.ft.sdk.garble.bean.Status#OK}
     *
     * @param innerLogHandler
     */
    public static void registerInnerLogHandler(FTInnerLogHandler innerLogHandler) {
        TrackLog.setInnerLogHandler(innerLogHandler);
    }

    /**
     * 将内部日志转化成文件
     * <p>
     * {@link FTLoggerConfig#setPrintCustomLogToConsole(boolean)}  产生循环调用的情况
     * innerLogHandler 设置后，这个值自动为 false
     * <p>
     * {@link FTLogger}，如果使用 FTLogger 避免使用 {@link  com.ft.sdk.garble.bean.Status#OK}
     *
     * @param file 缓存文件
     */
    public static void registerInnerLogCacheToFile(File file) {
        registerInnerLogCacheToFile(file, false);
    }


    /**
     * {@link #registerInnerLogCacheToFile}
     * {@link com.ft.sdk.tests.InnerLogTest}
     * 将内部日志转化成文件
     *
     * @param file          缓存文件
     * @param isAndroidTest 是否是 Android Test
     */
    private static void registerInnerLogCacheToFile(File file, boolean isAndroidTest) {
        final LogFileHelper helper = new LogFileHelper(FTApplication.getApplication(), file, isAndroidTest);

        TrackLog.setInnerLogHandler(new FTInnerLogHandler() {
            @Override
            public void printInnerLog(String level, String tag, String logContent) {
                helper.appendLog(String.format("%s %s %s %s \n", Utils.getCurrentTimeStamp(), level, tag, logContent));
            }
        });
    }

    /**
     * 将内部日志转化成文件
     *
     * @param fileName 缓存文件名
     */
    public static void registerInnerLogCacheToFile(String fileName) {
        registerInnerLogCacheToFile(new File(fileName));
    }


}
