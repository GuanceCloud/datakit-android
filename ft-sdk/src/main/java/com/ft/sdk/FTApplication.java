package com.ft.sdk;

import android.app.Application;

/**
 * BY huangDianHua
 * DATE:2019-11-29 17:58
 * Description:
 */
public class FTApplication extends Application {
    private static Application instance;
    public static Application getApplication(){
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
