package com.ft.sdk.nativelib;

import java.lang.reflect.Method;

/**
 * author: huangDianHua
 * time: 2020/9/27 16:08:37
 * description: 反射调用 SDK 中 FTExceptionHandler.uploadCrashLog(String) 方法上传 native 崩溃日志
 */
public class ReflectSDKUtil {
    public static final String CLASS_NAME_FTEXCEPTIONHANDLER = "com.ft.sdk.garble.FTExceptionHandler";

    public static void reflectUploadCrashLog(String crash){
        try{
            Class<?> clazzFTExceptionHandler = Class.forName(CLASS_NAME_FTEXCEPTIONHANDLER);
            Method getMethod = clazzFTExceptionHandler.getMethod("get");
            Object objFTException = getMethod.invoke(clazzFTExceptionHandler);
            Method uploadCrashLogMethod = clazzFTExceptionHandler.getMethod("uploadCrashLog",String.class);
            uploadCrashLogMethod.invoke(objFTException,crash);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
