package com.ft.plugin.garble;

/**
 * BY huangDianHua
 * DATE:2019-12-03 13:41
 * Description:
 */
public class Logger {
    /**
     * 输出日志前缀
     */
    private static final String TAG = "[FT-Plugin]:";
    /**
     * 设置 debug，开启后，会在 build 过程中输出日志
     */
    private static boolean debug = true;

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
        System.out.println(TAG + "setDebug:" + debug);
    }

    /**
     *  debug 级别日志
     * @param message
     */
    public static void debug(Object message) {
        if (debug) {
            System.out.println(TAG + message);
        }
    }

    /**
     * error 级别日志
     * @param message
     */
    public static void error(Object message) {
        System.err.println(TAG + message);
    }
}
