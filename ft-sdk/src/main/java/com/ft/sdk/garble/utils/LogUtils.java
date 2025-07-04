package com.ft.sdk.garble.utils;


import static com.ft.sdk.garble.utils.TrackLog.showFullLog;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTInnerLogHandler;
import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.SDKLogLevel;
import com.ft.sdk.garble.manager.LogFileHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * BY huangDianHua
 * DATE:2019-12-03 15:56
 * Description: Responsible for internal log output
 */
public class LogUtils {

    /**
     * Default cache file name
     */
    private final static String DEFAULT_INNER_LOG_FILE = "LogInner.log";

    protected static String TAG = "[FT-SDK]:";

    /**
     * Whether it is debug mode, when true, output console logs
     */
    private static boolean mDebug = true;

    private static SDKLogLevel currentLevel = SDKLogLevel.V;

    /**
     *
     */
    private static boolean aliasLogShow = false;

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }

    public static void setSDKLogLevel(SDKLogLevel logLevel) {
        if (logLevel != null) {
            currentLevel = logLevel;
        }
    }

    public static void setDescLogShow(boolean aliasLogShow) {
        LogUtils.aliasLogShow = aliasLogShow;
    }

    public static void i(String tag, String message) {
        i(tag, message, currentLevel.ordinal() <= SDKLogLevel.I.ordinal() && mDebug);
    }

    public static void d(String tag, String message) {
        d(tag, message, currentLevel.ordinal() <= SDKLogLevel.D.ordinal() && mDebug);
    }

    public static void e(String tag, String message) {
        e(tag, message, currentLevel.ordinal() <= SDKLogLevel.E.ordinal() && mDebug);
    }

    public static void eOnce(String tag, String message) {
        eOnce(tag, message, currentLevel.ordinal() <= SDKLogLevel.E.ordinal() && mDebug);
    }

    public static void v(String tag, String message) {
        v(tag, message, currentLevel.ordinal() <= SDKLogLevel.V.ordinal() && mDebug);
    }

    public static void w(String tag, String message) {
        w(tag, message, currentLevel.ordinal() <= SDKLogLevel.W.ordinal() && mDebug);
    }

    public static void wOnce(String tag, String message) {
        wOnce(tag, message, currentLevel.ordinal() <= SDKLogLevel.W.ordinal() && mDebug);
    }

    public static void i(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, SDKLogLevel.I, false);
        }
    }

    public static void d(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, SDKLogLevel.D, false);
        }
    }

    public static void e(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, SDKLogLevel.E, false);
        }
    }

    private static void eOnce(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, SDKLogLevel.E, true);
        }
    }

    public static void v(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, SDKLogLevel.V, false);
        }
    }

    public static void w(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, SDKLogLevel.W, false);
        }
    }

    private static void wOnce(String tag, String message, boolean showLog) {
        if (showLog) {
            showFullLog(tag, message, SDKLogLevel.W, true);
        }
    }


    public static void showAlias(String message) {
        if (aliasLogShow) {
            showFullLog(TAG, message, SDKLogLevel.D, false);
        }
    }


    /**
     * Set internal log output object
     * <p>
     * Note: To avoid circular calls with
     * <p>
     * {@link FTLoggerConfig#setPrintCustomLogToConsole(boolean)}
     * innerLogHandler set, this value is automatically false
     * <p>
     * {@link FTLogger}, if using FTLogger level D level logs will cause a dead loop, because data writing will generate related Inner Debug
     *
     * @param innerLogHandler
     */
    public static void registerInnerLogHandler(FTInnerLogHandler innerLogHandler) {
        TrackLog.setInnerLogHandler(innerLogHandler);
    }

    /**
     * Convert internal log to file
     * <p>
     * Need to enable {@link com.ft.sdk.FTSDKConfig#setDebug(boolean)} Set to true to enable debug mode
     * <p>
     * {@link FTLoggerConfig#setPrintCustomLogToConsole(boolean)}
     * innerLogHandler set, this value is automatically false
     * <p>
     * {@link FTLogger}, if using FTLogger level D level logs will cause a dead loop, because data writing will generate related Inner Debug
     *
     * @param file Cache file
     */
    public static void registerInnerLogCacheToFile(File file) {
        registerInnerLogCacheToFile(file, false);
    }


    /**
     * {@link #registerInnerLogCacheToFile}
     * {@link com.ft.sdk.tests.InnerLogTest}
     * Convert internal log to file
     *
     * @param file           Cache file
     * @param isAndroidTest  Whether it is Android Test
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
     * Convert internal log to file
     * <p>
     * Need to enable {@link com.ft.sdk.FTSDKConfig#setDebug(boolean)} Set to true to enable debug mode
     * <p>
     * {@link FTLoggerConfig#setPrintCustomLogToConsole(boolean)}
     * innerLogHandler set, this value is automatically false
     * <p>
     * {@link FTLogger}, if using FTLogger avoid using {@link  com.ft.sdk.garble.bean.Status#OK}
     *
     * @param fileName Cache file name
     */
    public static void registerInnerLogCacheToFile(String fileName) {
        registerInnerLogCacheToFile(new File(fileName));
    }

    /**
     * Convert internal log to file
     * Default path: /data/data/{package_name}/files/LogInner.log
     * <p>
     * Need to enable {@link com.ft.sdk.FTSDKConfig#setDebug(boolean)} Set to true to enable debug mode
     * <p>
     * {@link FTLoggerConfig#setPrintCustomLogToConsole(boolean)}
     * innerLogHandler set, this value is automatically false
     * <p>
     * {@link FTLogger}, if using FTLogger avoid using {@link  com.ft.sdk.garble.bean.Status#OK}
     */
    public static void registerInnerLogCacheToFile() {
        String filePath = FTApplication.getApplication().getFilesDir().toString() + File.separator + DEFAULT_INNER_LOG_FILE;
        registerInnerLogCacheToFile(new File(filePath));
    }


    /**
     * {@link android.util.Log#getStackTraceString(Throwable)} Remove UnknownHostException exclusion logic
     *
     * @param tr
     * @return
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
//        Throwable t = tr;
//        while (t != null) {
////            if (t instanceof UnknownHostException) {
////                return "";
////            }
//            t = t.getCause();
//        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 256);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * Get network error description
     *
     * @param e IOException occurred during network request
     * @return
     */
    public static String getNetworkExceptionDesc(IOException e) {
        if (e instanceof java.net.SocketTimeoutException) {
            return "Network Timeout";
        } else if (e instanceof java.net.UnknownHostException) {
            return "Unknown Host";
        } else if (e instanceof java.net.ConnectException) {
            return "Connection Refused";
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            return "SSL Handshake Failed";
        } else if (e instanceof javax.net.ssl.SSLPeerUnverifiedException) {
            return "SSL Peer Unverified";
        } else if (e instanceof java.net.ProtocolException) {
            return "Protocol Error";
        } else if (e instanceof java.io.InterruptedIOException) {
            return "Request Interrupted";
        } else if (e instanceof java.io.EOFException) {
            return "Connection Closed Prematurely";
        } else if (e instanceof java.net.NoRouteToHostException) {
            return "No Route to Host";
        } else if (e instanceof java.net.BindException) {
            return "Address Already in Use";
        } else if (e instanceof java.net.PortUnreachableException) {
            return "Port Unreachable";
        } else if (e instanceof java.net.MalformedURLException) {
            return "Malformed URL";
        } else if (e instanceof java.net.HttpRetryException) {
            return "HTTP Retry Required";
        } else if (e instanceof java.net.UnknownServiceException) {
            return "Unknown Service";
        } else {
            return "";
        }
    }


}
