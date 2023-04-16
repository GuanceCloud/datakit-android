package com.ft;

import android.app.Application;

import com.ft.sdk.garble.utils.LogUtils;

public class BaseApplication  extends Application {
    private static final String TAG = "BaseApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG,"BaseApplication.onCreate()");
    }
}
