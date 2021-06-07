package com.ft.application;

import android.app.Application;
import android.content.Context;

import com.ft.AccountUtils;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;

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
