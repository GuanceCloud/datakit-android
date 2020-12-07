package com.ft.sdk.garble.utils;


import static com.ft.sdk.garble.utils.TrackLog.showFullLog;

/**
 * BY huangDianHua
 * DATE:2019-12-03 15:56
 * Description:
 */
public class LogUtils{
    protected static String TAG = "[FT-SDK]:";

    private static boolean mDebug = true;
    private static boolean aliasLogShow = false;

    public static boolean isDebug() {
        return mDebug;
    }

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }

    public static void setDescLogShow(boolean aliasLogShow) {
        LogUtils.aliasLogShow = aliasLogShow;
    }

    public static void i(String tag,String message){
        if(mDebug){
            showFullLog(tag,message, TrackLog.LogType.I);
        }
    }

    public static void d(String tag,String message){
        if(mDebug){
            showFullLog(tag,message, TrackLog.LogType.D);
        }
    }
    public static void e(String tag,String message){
        if(mDebug){
            showFullLog(tag,message, TrackLog.LogType.E);
        }
    }
    public static void v(String tag,String message){
        if(mDebug){
            showFullLog(tag,message, TrackLog.LogType.V);
        }
    }

    public static void w(String tag,String message){
        if(mDebug){
            showFullLog(tag,message, TrackLog.LogType.W);
        }
    }

    public static void showAlias(String message){
        if(aliasLogShow){
            showFullLog(TAG,message, TrackLog.LogType.D);
        }
    }




}
