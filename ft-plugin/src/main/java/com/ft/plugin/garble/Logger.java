package com.ft.plugin.garble;

/**
 * BY huangDianHua
 * DATE:2019-12-03 13:41
 * Description:
 */
public class Logger {
    private static String TAG = "[FT-Plugin]:";
    private static boolean debug = true;

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
    }

    public static void info(Object message){
        if(debug){
            System.out.println(TAG+message);
        }
    }
}
