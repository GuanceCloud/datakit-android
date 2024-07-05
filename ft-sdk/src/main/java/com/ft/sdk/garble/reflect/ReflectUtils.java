package com.ft.sdk.garble.reflect;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.nativelib.ExceptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Brandon
 * <p>
 * 用于模拟生成"用户访问监测"中 Error 数据中 {@link com.ft.sdk.garble.bean.ErrorType#NATIVE} 类型数据
 * 数据筛查使用方法，请参考 @see <a href="https://docs.guance.com/real-user-monitoring/explorer/error/">查看器 Error</a>
 */
public class ReflectUtils {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "ReflectUtils";
    /**
     * ft-native 中 {@link ExceptionHandler } 类路径，如果 {@link ExceptionHandler } 发生更改，需要对应进行更改
     */
    public static final String CLASS_NAME_EXCEPTION_HANDLER = "com.ft.sdk.nativelib.ExceptionHandler";

    /**
     * 模块测试使用，调用后，会在 c++ 代码层抛出异常,调用 {@link ExceptionHandler#crashAndGetExceptionMessage()}  }
     */
    public static void reflectCrashAndGetExceptionMessage() {
        reflectInvokeMethodV("crashAndGetExceptionMessage");
    }

    /**
     * 指定 {@link ExceptionHandler } 调用方法，这种效果可以达到自由组合 ft-native 依赖包的效果
     * 为避免未依赖 ft-native，会对  {@link IllegalAccessException,NoSuchMethodException,InvocationTargetException,ClassNotFoundException }
     * 进行捕获处理
     *
     * @param method {@link ExceptionHandler } 中 使用的方法名
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
