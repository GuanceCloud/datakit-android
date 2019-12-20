package com.ft.sdk;

import android.app.Application;

import com.ft.sdk.garble.FTActivityLifecycleCallbacks;
import com.ft.sdk.garble.FTAutoTrackConfig;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.utils.LogUtils;


/**
 * BY huangDianHua
 * DATE:2019-11-29 17:15
 * Description:
 */
public class FTSdk {
    private static FTSdk FTSDK;
    private FTSDKConfig mFtSDKConfig;
    private FTSdk(FTSDKConfig ftSDKConfig){
        FTActivityLifecycleCallbacks life = new FTActivityLifecycleCallbacks();
        Application app = FTApplication.getApplication();
        app.registerActivityLifecycleCallbacks(life);
        this.mFtSDKConfig = ftSDKConfig;
        initFTConfig();
        trackStartApp();
    }
    public static synchronized FTSdk install(FTSDKConfig ftSDKConfig){
        if (FTSDK == null) {
            FTSDK = new FTSdk(ftSDKConfig);
        }
        return FTSDK;
    }

    private void initFTConfig(){
        if(mFtSDKConfig != null) {
            FTHttpConfig.get().initParams(mFtSDKConfig);
            FTAutoTrackConfig.get().initParams(mFtSDKConfig);
            LogUtils.setDebug(mFtSDKConfig.isDebug());
        }
    }

    private void trackStartApp(){
        FTAutoTrack.startApp();
    }



}
