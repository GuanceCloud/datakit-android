package com.ft.sdk;

import android.app.Application;
import android.content.Context;

import com.ft.sdk.garble.FTActivityLifecycleCallbacks;
import com.ft.sdk.garble.manager.FTManager;

/**
 * BY huangDianHua
 * DATE:2019-11-29 17:15
 * Description:
 */
public class FTSDKInstall {
    private static FTSDKInstall FTSDKInstall;
    private Context mContext;
    private FTSDKConfig mFtSDKConfig;
    private FTSDKInstall(Context context,FTSDKConfig ftsdkConfig){
        mContext = context;
        mFtSDKConfig = ftsdkConfig;
        FTActivityLifecycleCallbacks life = new FTActivityLifecycleCallbacks();
        Application app = (Application) context.getApplicationContext();
        app.registerActivityLifecycleCallbacks(life);
        FTManager.getSyncTaskManaget().executeSyncPoll();
    }
    public static synchronized FTSDKInstall install(Context context,FTSDKConfig ftsdkConfig){
        if (FTSDKInstall == null) {
            FTSDKInstall = new FTSDKInstall(context,ftsdkConfig);
        }
        return FTSDKInstall;
    }

}
