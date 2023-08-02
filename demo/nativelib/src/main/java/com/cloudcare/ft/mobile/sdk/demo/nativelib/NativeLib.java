package com.cloudcare.ft.mobile.sdk.demo.nativelib;

public class NativeLib {

    // Used to load the 'nativelib' library on application startup.
    static {
        System.loadLibrary("nativelib");
    }
    public native String crashTest();
}