package com.ft.plugin.garble;

/**
 * BY huangDianHua
 * DATE:2019-12-03 13:41
 * Description:
 */
public class Logger {
    private static final String TAG = "[FT-Plugin]:";
    /**
     * 设置 debug，开启后，会在 build 过程中输出日志
     */
    private static boolean debug = true;

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
        System.out.println(TAG + "setDebug:" + debug);
    }

    public static void debug(Object message) {
        if (debug) {
            System.out.println(TAG + message);
        }
    }

    public static void error(Object message) {
        System.err.println(TAG + message);
    }
}
