package com.ft.sdk.garble.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * author: huangDianHua
 * time: 2020/9/27 16:24:15
 * description:反射调用 ft-native 中 ExceptionHandler 捕获 native 崩溃的方法
 */
public class ReflectUtils {
    public static final String CLASS_NAME_EXCEPTION_HANDLER = "com.ft.sdk.nativelib.ExceptionHandler";

    public static void reflectRegisterSignalHandler() {
        reflectInvokeMethodV("registerSignalHandler");
    }

    public static void reflectUnRegisterSignalHandler() {
        reflectInvokeMethodV("unRegisterSignalHandler");
    }

    public static void reflectCrashAndGetExceptionMessage() {
        reflectInvokeMethodV("crashAndGetExceptionMessage");
    }

    private static void reflectInvokeMethodV(String method) {
        try {
            Class<?> clazzRegisterSignalHandler = Class.forName(CLASS_NAME_EXCEPTION_HANDLER);
            Method getMethod = clazzRegisterSignalHandler.getMethod("get");
            Object objException = getMethod.invoke(clazzRegisterSignalHandler);
            Method registerSignalHandlerMethod = clazzRegisterSignalHandler.getMethod(method);
            registerSignalHandlerMethod.invoke(objException);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
