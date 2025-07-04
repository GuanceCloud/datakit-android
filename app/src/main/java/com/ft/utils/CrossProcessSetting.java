package com.ft.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Used in Android Test to get configuration in a sub-thread, {@link com.ft.service.TestService}
 */
public class CrossProcessSetting {
    private static final String KEY_MAIN_PROCESS = "only_main_process";
    private static final String KEY_CROSS_PROCESS_SETTING = "cross_process_setting";

    private static SharedPreferences getSharePreference(Context context) {
        return context.getSharedPreferences(KEY_CROSS_PROCESS_SETTING, Context.MODE_PRIVATE);
    }

    /**
     * Get isOnlyMainProcess setting status
     *
     * @param context
     * @return
     */
    public static boolean isOnlyMainProcess(Context context) {
        SharedPreferences sp = getSharePreference(context);
        return sp.getBoolean(KEY_MAIN_PROCESS, false);
    }

    /**
     * Set isOnlyMainProcess status
     *
     * @param context
     * @param isOnlyMainProcess
     */
    public static void setOnlyMainProcess(Context context, boolean isOnlyMainProcess) {
        SharedPreferences sp = getSharePreference(context);
        sp.edit().putBoolean(KEY_MAIN_PROCESS, isOnlyMainProcess).commit();
    }

    /**
     * Clear cross_process_setting setting
     *
     * @param context
     */
    public static void clearProcessSetting(Context context) {
        SharedPreferences sp = getSharePreference(context);
        sp.edit().clear().commit();
    }
}
