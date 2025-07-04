package com.ft.sdk.garble.reflect;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.nativelib.ExceptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Brandon
 * <p>
 * Used to simulate generating {@link com.ft.sdk.garble.bean.ErrorType#NATIVE} type data in "User Access Monitoring" Error data
 * Data screening method, please refer to @see <a href="https://docs.guance.com/real-user-monitoring/explorer/error/">Error Viewer</a>
 */
public class ReflectUtils {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "ReflectUtils";
    /**
     * {@link ExceptionHandler } class path in ft-native, if {@link ExceptionHandler } changes, it needs to be changed accordingly
     */
    public static final String CLASS_NAME_EXCEPTION_HANDLER = "com.ft.sdk.nativelib.ExceptionHandler";

    /**
     * Used for module testing, after calling, an exception will be thrown in the c++ code layer, call {@link ExceptionHandler#crashAndGetExceptionMessage()}
     */
    public static void reflectCrashAndGetExceptionMessage() {
        reflectInvokeMethodV("crashAndGetExceptionMessage");
    }

    /**
     * Specify {@link ExceptionHandler } to call the method, this effect can achieve the effect of freely combining ft-native dependency packages
     * To avoid not depending on ft-native, it will capture and process {@link IllegalAccessException,NoSuchMethodException,InvocationTargetException,ClassNotFoundException }
     *
     * @param method The method name used in {@link ExceptionHandler }
     */
    private static void reflectInvokeMethodV(String method) {
        try {
            Class<?> exceptionHandlerClass = Class.forName(CLASS_NAME_EXCEPTION_HANDLER);
            Method getMethod = exceptionHandlerClass.getMethod("get");
            Object instance = getMethod.invoke(exceptionHandlerClass);
            Method handlerMethod = exceptionHandlerClass.getMethod(method);
            handlerMethod.invoke(instance);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException |
                 ClassNotFoundException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }
}
