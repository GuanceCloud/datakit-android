package com.ft.sdk;

import android.app.Application;

/**
 * BY huangDianHua
 * DATE:2019-11-29 17:58
 * Description:
 */
public class FTApplication {
    private static Application instance;

    public static void setApplication(Application context) {
        instance = context;
    }

    public static Application getApplication() {
        return instance;
    }
}
