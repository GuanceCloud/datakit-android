package com.ft.sdk;

import android.app.Application;

import com.ft.sdk.garble.utils.Utils;

import java.lang.reflect.Method;

/**
 * BY huangDianHua
 * DATE:2020-03-6 17:58
 * Description:
 * Used to handle situations requiring a {@link android.content.Context} parameter. This is a reflective way to obtain the class and should be avoided to reduce the risk of being disabled in the future.
 */
public class FTApplication {
    public static long APP_START_TIME = Utils.getCurrentNanoTime();
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
     * Get the current application instance
     *
     * @return The current {@link Application} instance
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
