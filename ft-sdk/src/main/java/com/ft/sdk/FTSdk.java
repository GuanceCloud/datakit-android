package com.ft.sdk;

import android.app.Application;

import com.ft.sdk.garble.FTActivityLifecycleCallbacks;
import com.ft.sdk.garble.FTAutoTrackConfig;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONObject;

import java.security.InvalidParameterException;


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

    public static synchronized FTSdk get(){
        if(FTSDK == null){
            throw new InvalidParameterException("请先安装SDK(在应用启动时调用FTSdk.install(FTSDKConfig))");
        }
        return FTSDK;
    }

    /**
     * 注销用户信息
     */
    public void unbindUserData(){
        if(mFtSDKConfig != null){
            if (mFtSDKConfig.isNeedBindUser()) {
                FTUserConfig.get().unbindUserData();
                FTUserConfig.get().clearSessionId();
            }
        }
    }

    /**
     * 绑定用户信息
     * @param name
     * @param id
     * @param exts
     */
    public void bindUserData(String name, String id, JSONObject exts){
        if(mFtSDKConfig != null){
            if(mFtSDKConfig.isNeedBindUser()){
                FTUserConfig.get().initSessionId();
                FTUserConfig.get().bindUserData(name,id,exts);
            }
        }
    }

    private void initFTConfig(){
        if(mFtSDKConfig != null) {
            FTHttpConfig.get().initParams(mFtSDKConfig);
            FTAutoTrackConfig.get().initParams(mFtSDKConfig);
            LogUtils.setDebug(mFtSDKConfig.isDebug());
            FTUserConfig.get().setNeedBindUser(mFtSDKConfig.isNeedBindUser());
            if(mFtSDKConfig.isNeedBindUser()){
                FTUserConfig.get().initSessionId();
                FTUserConfig.get().initUserDataFromDB();
            }
        }
    }

    private void trackStartApp(){
        FTAutoTrack.startApp();
    }



}
