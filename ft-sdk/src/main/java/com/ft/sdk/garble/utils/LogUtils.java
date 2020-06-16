package com.ft.sdk.garble.utils;

import android.util.Log;

/**
 * BY huangDianHua
 * DATE:2019-12-03 15:56
 * Description:
 */
public class LogUtils extends TrackLog{
    protected static String TAG = "[FT-LOG]:";

    private static boolean mDebug = true;
    private static boolean aliasLogShow = true;

    public static boolean isDebug() {
        return mDebug;
    }

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }

    public static void setDescLogShow(boolean aliasLogShow) {
        LogUtils.aliasLogShow = aliasLogShow;
    }

    public static void i(String message){
        if(mDebug){
            showFullLog(TAG,message, TrackLog.LogType.I);
        }
    }

    public static void d(String message){
        if(mDebug){
            showFullLog(TAG,message, TrackLog.LogType.D);
        }
    }
    public static void e(String message){
        if(mDebug){
            showFullLog(TAG,message, TrackLog.LogType.E);
        }
    }
    public static void v(String message){
        if(mDebug){
            showFullLog(TAG,message, TrackLog.LogType.V);
        }
    }

    public static void w(String message){
        if(mDebug){
            showFullLog(TAG,message, TrackLog.LogType.W);
        }
    }

    public static void showAlias(String message){
        if(aliasLogShow){
            showFullLog(TAG,message, TrackLog.LogType.D);
        }
    }




}
