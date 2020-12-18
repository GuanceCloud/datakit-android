package com.ft.sdk.garble;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.LogUtils;

public class FTRUMConfig {

    private static final String TAG = "FTRUMConfig";
    private EnvType envType;

    private static class SingletonHolder {
        private static final FTRUMConfig INSTANCE = new FTRUMConfig();
    }

    public static FTRUMConfig get() {
        return FTRUMConfig.SingletonHolder.INSTANCE;
    }

    public void initParam(FTSDKConfig config) {
        if (config == null) {
            return;
        }
        appId = config.getRUMAppId();
        envType = config.getEnv();
        LogUtils.d(TAG, "appid:" + appId);
    }

    private String appId;

    public String getAppId() {
        return appId;
    }

    public boolean isRumEnable() {
        return appId != null && !appId.isEmpty();
    }


    public EnvType getEnvType() {
        return envType;
    }
}
