package com.ft.application;

import android.app.Application;
import android.content.Context;

/**
 * author: huangDianHua
 * time: 2020/9/2 17:31:17
 * description:
 */
public class MockApplication extends Application {
    private static Context instance;

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
