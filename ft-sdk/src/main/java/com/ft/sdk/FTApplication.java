package com.ft.sdk;

import android.app.Application;

import java.lang.reflect.Method;

/**
 * BY huangDianHua
 * DATE:2020-03-6 17:58
 * Description:
 * 用于处理需要 {@link android.content.Context} 参数的情况，此为类的映射方式获取应该尽量避免此对象的使用，
 * 避免后期被禁用使用的风险
 */
public class FTApplication {
    private static volatile Application instance;

    public static Application getApplication() {
        if (instance == null) {
            synchronized (FTApplication.class) {
                if (instance == null) {
                    instance = getCurrentApplication();
                }
            }
        }
        return instance;
    }


    private FTApplication() {

    }

    /**
     * 获取当前应用实例
     *
     * @return 当前 {@link Application} 实例
     */
    private static Application getCurrentApplication() {
        Object activityThread;
        try {
            Class acThreadClass = Class.forName("android.app.ActivityThread");
            if (acThreadClass == null) {
                return null;
            }
            Method acThreadMethod = acThreadClass.getMethod("currentActivityThread");
            if (acThreadMethod == null) {
                return null;
            }
            acThreadMethod.setAccessible(true);
            activityThread = acThreadMethod.invoke(null);
            Method applicationMethod = activityThread.getClass().getMethod("getApplication");
            if (applicationMethod == null) {
                return null;
            }
            Object app = applicationMethod.invoke(activityThread);
            return (Application) app;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
