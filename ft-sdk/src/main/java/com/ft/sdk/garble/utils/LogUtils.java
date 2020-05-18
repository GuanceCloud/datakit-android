package com.ft.sdk.garble.utils;

import android.util.Log;

/**
 * BY huangDianHua
 * DATE:2019-12-03 15:56
 * Description:
 */
public class LogUtils {
    private static String TAG = "[FT-SDK]:";
    private static boolean mDebug = true;
    private static boolean aliasLogShow = true;

    public static boolean isDebug() {
        return mDebug;
    }

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }

    public static void setAliasLogShow(boolean aliasLogShow) {
        LogUtils.aliasLogShow = aliasLogShow;
    }

    public static void i(Object message){
        if(mDebug){
            showFullLog(""+message,LogType.I);
        }
    }

    public static void d(Object message){
        if(mDebug){
            showFullLog(""+message,LogType.D);
        }
    }
    public static void e(Object message){
        if(mDebug){
            showFullLog(""+message,LogType.E);
        }
    }
    public static void v(Object message){
        if(mDebug){
            showFullLog(""+message,LogType.V);
        }
    }

    public static void showAlias(Object message){
        if(aliasLogShow){
            showFullLog(""+message,LogType.D);
        }
    }



    private static void showFullLog(String message,LogType logType){
        int segmentSize = 3 * 1024;
        long length = message.length();
        if(length <= segmentSize){
            showLog(TAG,message,logType);
        }else{
            boolean isFirst = true;
            while (message.length() > segmentSize){
                String logContent = message.substring(0,segmentSize);
                message = message.replace(logContent,"");
                if(isFirst) {
                    showLog(TAG, logContent, logType);
                }else{
                    showLog(null, logContent, logType);
                }
                isFirst = false;
            }
            showLog(null,message,logType);
        }
    }

    private static void showLog(String tag,String message,LogType logType){
        switch (logType){
            case D:Log.println(Log.DEBUG,tag,message);
            break;
            case E:Log.println(Log.ERROR,tag,message);
            break;
            case I:Log.println(Log.INFO,tag,message);
            break;
            case V:Log.println(Log.VERBOSE,tag,message);
        }
    }

    enum LogType{
        I,D,E,V
    }
}
