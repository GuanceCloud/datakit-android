package com.ft.sdk;

import android.app.Application;

import com.ft.sdk.garble.FTActivityLifecycleCallbacks;
import com.ft.sdk.garble.FTHttpConfig;


/**
 * BY huangDianHua
 * DATE:2019-11-29 17:15
 * Description:
 */
public class FTSDKInstall {
    private static FTSDKInstall FTSDKInstall;
    private FTSDKConfig mFtSDKConfig;
    private FTSDKInstall(FTSDKConfig ftSDKConfig){
        FTActivityLifecycleCallbacks life = new FTActivityLifecycleCallbacks();
        Application app = FTApplication.getApplication();
        app.registerActivityLifecycleCallbacks(life);
        this.mFtSDKConfig = ftSDKConfig;
        initFTHttpConfig();
    }
    public static synchronized FTSDKInstall getInstance(FTSDKConfig ftSDKConfig){
        if (FTSDKInstall == null) {
            FTSDKInstall = new FTSDKInstall(ftSDKConfig);
        }
        return FTSDKInstall;
    }

    private void initFTHttpConfig(){
        FTHttpConfig.get().initParams(mFtSDKConfig);
    }


}
