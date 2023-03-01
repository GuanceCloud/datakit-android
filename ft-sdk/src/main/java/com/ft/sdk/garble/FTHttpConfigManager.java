package com.ft.sdk.garble;

import static com.ft.sdk.garble.utils.Constants.USER_AGENT;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;

/**
 * BY huangDianHua
 * DATE:2019-12-09 19:39
 * Description:
 */
public class FTHttpConfigManager {
    private static final String TAG = "FTHttpConfigManager";
    private static volatile FTHttpConfigManager instance;
    public String serverUrl;
    public String uuid;
    public String userAgent;

    public int sendOutTime = 10000;
    public int readOutTime = 10000;


    private FTHttpConfigManager() {

    }

    public synchronized static FTHttpConfigManager get() {
        if (instance == null) {
            instance = new FTHttpConfigManager();
        }
        return instance;
    }

    /**
     *
     * @param ftsdkConfig
     */
    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        serverUrl = ftsdkConfig.getMetricsUrl();
        uuid = DeviceUtils.getSDKUUid(FTApplication.getApplication());
        userAgent = USER_AGENT;

        LogUtils.d(TAG, "serverUrl:" + serverUrl);

    }


    /**
     * 释放 SDK 相关
     */
    public static void release() {
        instance = null;
    }
}
