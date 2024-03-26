package com.ft.sdk.nativelib;


import android.content.Context;

import ftnative.NativeCrash;

public class NativeEngineInit {

    /**
     * 数据初始化
     *
     * @param context
     * @param logPath           日志生成路径
     * @param enableNativeCrash 是否开启 native crash 收集
     * @param enableAnrHandler  是否开始 ANR 数据收集
     */
    public static void init(Context context, String logPath, boolean enableNativeCrash, boolean enableAnrHandler) {
        init(context, logPath, enableNativeCrash, enableAnrHandler, null);
    }

    /**
     * 数据初始化
     *
     * @param context
     * @param logPath             日志生成路径
     * @param enableNativeCrash   是否开启 native crash 收集
     * @param enableAnrHandler    是否开始 ANR 数据收集
     * @param nativeCrashCallback 是否接收 native crash 回调
     */
    public static void init(Context context, String logPath, boolean enableNativeCrash, boolean enableAnrHandler, CrashCallback nativeCrashCallback) {
        NativeCrash.InitParameters parameters = new NativeCrash.InitParameters()
                .setNativeRethrow(true)
                .setNativeLogCountMax(10)
                .setNativeDumpAllThreadsCountMax(10)
                .setNativeCallback(new ftnative.CrashCallback() {
                    @Override
                    public void onCrash(String crashPath, String s) {
                        if (nativeCrashCallback != null) {
                            nativeCrashCallback.onCrash(crashPath);
                        }
                    }
                })
                .setPlaceholderCountMax(3)
                .setAnrRethrow(true)
                .setAnrLogCountMax(10)
                .setPlaceholderSizeKb(512)
                .setLogDir(logPath)
                .setLogFileMaintainDelayMs(1000);
        if (!enableAnrHandler) {
            parameters.disableAnrCrashHandler();
        }

        if (!enableNativeCrash) {
            parameters.disableNativeCrashHandler();
        }

        NativeCrash.init(context, parameters);
    }

}
