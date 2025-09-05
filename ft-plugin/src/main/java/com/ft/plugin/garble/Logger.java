package com.ft.plugin.garble;

/**
 * BY huangDianHua
 * DATE:2019-12-03 13:41
 * Description:
 */
public class Logger {
    /**
     * Log output prefix
     */
    private static final String TAG = "[FT-Plugin]:";
    /**
     * Set debug. When enabled, logs will be output during the build process
     */
    private static boolean debug = true;

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
        System.out.println(TAG + "setDebug:" + debug);
    }

    /**
     * debug level log
     * @param message
     */
    public static void debug(Object message) {
        if (debug) {
            System.out.println(TAG + message);
        }
    }

    /**
     * error level log
     * @param message
     */
    public static void error(Object message) {
        System.err.println(TAG + message);
    }
}
