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

    public static boolean isDebug() {
        return mDebug;
    }

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }

    public static void i(Object message){
        if(mDebug){
            Log.i(TAG,""+message);
        }
    }

    public static void d(Object message){
        if(mDebug){
            Log.d(TAG,""+message);
        }
    }
    public static void e(Object message){
        if(mDebug){
            Log.e(TAG,""+message);
        }
    }
    public static void v(Object message){
        if(mDebug){
            Log.v(TAG,""+message);
        }
    }
}
