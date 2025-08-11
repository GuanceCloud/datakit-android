package com.ft.sdk.nativelib;


import android.content.Context;

import ftnative.NativeCrash;

public class NativeEngineInit {

    /**
     * Data initialization
     *
     * @param context
     * @param logPath           Log generation path
     * @param enableNativeCrash Whether to enable native crash collection
     * @param enableAnrHandler  Whether to start ANR data collection
     */
    public static void init(Context context, String logPath, boolean enableNativeCrash, boolean enableAnrHandler) {
        init(context, logPath, enableNativeCrash, enableAnrHandler, null);
    }

    /**
     * Data initialization
     *
     * @param context
     * @param logPath             Log generation path
     * @param enableNativeCrash   Whether to enable native crash collection
     * @param enableAnrHandler    Whether to start ANR data collection
     * @param nativeCrashCallback Whether to receive native crash callback
     */
    public static void init(Context context, String logPath, boolean enableNativeCrash, boolean enableAnrHandler,
                            CrashCallback nativeCrashCallback) {
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


    /**
     * Data initialization
     *
     * @param context
     * @param logPath             Log generation path
     * @param enableNativeCrash   Whether to enable native crash collection
     * @param enableAnrHandler    Whether to start ANR data collection
     * @param nativeCrashCallback Whether to receive native crash callback
     */
    public static void init(Context context, String logPath, boolean enableNativeCrash, boolean enableAnrHandler,
                            CrashCallback nativeCrashCallback,
                            NativeExtraLogCatSetting nativeCrashLogCatSetting,
                            NativeExtraLogCatSetting anrCrashLogCatSetting) {
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
        } else {
            if (nativeCrashLogCatSetting != null) {
                parameters.setNativeLogcatMainLines(nativeCrashLogCatSetting.getLogcatMainLines());
                parameters.setNativeLogcatSystemLines(nativeCrashLogCatSetting.getLogcatSystemLines());
                parameters.setNativeLogcatEventsLines(nativeCrashLogCatSetting.getLogcatEventsLines());
            }
        }

        if (!enableNativeCrash) {
            parameters.disableNativeCrashHandler();
        } else {
            if (anrCrashLogCatSetting != null) {
                parameters.setAnrLogcatMainLines(anrCrashLogCatSetting.getLogcatMainLines());
                parameters.setAnrLogcatSystemLines(anrCrashLogCatSetting.getLogcatSystemLines());
                parameters.setAnrLogcatEventsLines(anrCrashLogCatSetting.getLogcatEventsLines());
            }

        }

        NativeCrash.init(context, parameters);
    }


}
