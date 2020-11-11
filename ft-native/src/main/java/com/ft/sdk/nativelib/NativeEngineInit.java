package com.ft.sdk.nativelib;


import android.content.Context;

import ftnative.NativeCrash;

public class NativeEngineInit {

    public static void init(Context context, String logPath) {
        NativeCrash.init(context, new NativeCrash.InitParameters()
                .setNativeRethrow(true)
                .setNativeLogCountMax(10)
                .setNativeDumpAllThreadsCountMax(10)
                .setPlaceholderCountMax(3)
                .setPlaceholderSizeKb(512)
                .setLogDir(logPath)
                .setLogFileMaintainDelayMs(1000));
    }

}
