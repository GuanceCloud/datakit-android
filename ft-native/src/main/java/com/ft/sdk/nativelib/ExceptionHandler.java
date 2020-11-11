package com.ft.sdk.nativelib;

import ftnative.NativeCrash;

/**
 * author: huangDianHua
 * time: 2020/9/27 16:00:41
 * description: jni 交互类
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
     * 模拟 native 代码崩溃
     */

    public void crashAndGetExceptionMessage() {
        NativeCrash.testNativeCrash(false);
    }


}
