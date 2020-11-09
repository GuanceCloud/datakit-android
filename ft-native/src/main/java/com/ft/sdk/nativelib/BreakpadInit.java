package com.ft.sdk.nativelib;


public class BreakpadInit {
    static {
        System.loadLibrary("breakpad-core");
    }

    public static void initBreakpad(String path){
        initBreakpadNative(path);
    }

    private static native void initBreakpadNative(String path);
}
