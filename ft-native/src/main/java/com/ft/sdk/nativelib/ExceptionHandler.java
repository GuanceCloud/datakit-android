package com.ft.sdk.nativelib;

import ftnative.NativeCrash;

/**
 * author: huangDianHua
 * time: 2020/9/27 16:00:41
 * description: JNI interaction class
 */
public class ExceptionHandler {
    private static ExceptionHandler instance;

    private ExceptionHandler() {
    }

    public static ExceptionHandler get() {
        if (instance == null) {
            instance = new ExceptionHandler();
        }
        return instance;
    }


    /**
     * Simulate native code crash
     */

    public void crashAndGetExceptionMessage() {
        NativeCrash.testNativeCrash(false);
    }


}
