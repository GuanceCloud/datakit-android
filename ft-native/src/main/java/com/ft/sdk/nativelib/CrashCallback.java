package com.ft.sdk.nativelib;

/**
 * Native Crash callback object
 */
public interface CrashCallback {
    /**
     * Callback after Native Crash, returns crashPath crash file path
     *
     * @param crashPath Target crash dump file
     */
    void onCrash(String crashPath);
}