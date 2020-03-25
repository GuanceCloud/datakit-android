package com.ft.sdk;

import android.app.Application;

import java.lang.reflect.Method;

/**
 * BY huangDianHua
 * DATE:2020-03-6 17:58
 * Description:
 */
public class FTApplication{
    private static Application instance;
    public static Application getApplication(){
        if(instance == null){
            synchronized (FTApplication.class){
                if(instance == null){
                    new FTApplication();
                }
            }
        }
        return instance;
    }


    private FTApplication(){
        Object activityThread;
        try{
            Class acThreadClass = Class.forName("android.app.ActivityThread");
            if(acThreadClass == null){
                return;
            }
            Method acThreadMethod = acThreadClass.getMethod("currentActivityThread");
            if(acThreadMethod == null){
                return;
            }
            acThreadMethod.setAccessible(true);
            activityThread = acThreadMethod.invoke(null);
            Method applicationMethod = activityThread.getClass().getMethod("getApplication");
            if(applicationMethod == null){
                return;
            }
            Object app = applicationMethod.invoke(activityThread);
            instance = (Application) app;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
