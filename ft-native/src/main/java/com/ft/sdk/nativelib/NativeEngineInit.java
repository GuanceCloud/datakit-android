package com.ft.sdk.nativelib;


import android.content.Context;

import ftnative.NativeCrash;

public class NativeEngineInit {

    public static void init(Context context, String logPath, boolean enableNativeCrash, boolean enableAnrHandler) {
        NativeCrash.InitParameters parameters = new NativeCrash.InitParameters()
                .setNativeRethrow(true)
                .setNativeLogCountMax(10)
                .setNativeDumpAllThreadsCountMax(10)
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
