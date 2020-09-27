package com.ft.sdk.nativelib;

/**
 * author: huangDianHua
 * time: 2020/9/27 16:00:41
 * description: jni 交互类
 */
public class ExceptionHandler {
    static {
        System.loadLibrary("native_exception_lib");
    }
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
     * 注册 native crash 捕获
     */
    public native void registerSignalHandler();

    /**
     * 取消注册 native crash
     */
    public native void unRegisterSignalHandler();

    /**
     * 模拟 native 代码崩溃
     */
    public native void crashAndGetExceptionMessage();

    /**
     * 该方法不能改动，该方法在 jni 中调用
     *
     * @param crash
     */
    public void uploadNativeCrashLog(String crash) {
        ReflectSDKUtil.reflectUploadCrashLog(crash);
    }
}
