package com.ft.sdk.garble;

import com.ft.sdk.BuildConfig;
import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.utils.DeviceUtils;

import java.util.Arrays;
import java.util.List;

import static com.ft.sdk.garble.utils.Constants.USER_AGENT;

/**
 * BY huangDianHua
 * DATE:2019-12-09 19:39
 * Description:
 */
public class FTHttpConfigManager {
    private static volatile FTHttpConfigManager instance;
    public String serverUrl;
    public String version;
    public String uuid;
    public String userAgent;
    public boolean useOaid;

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

    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        serverUrl = ftsdkConfig.getServerUrl();
        useOaid = ftsdkConfig.isUseOAID();
        version = BuildConfig.FT_SDK_VERSION;
        uuid = DeviceUtils.getSDKUUid(FTApplication.getApplication());
        userAgent = USER_AGENT;

    }


    public static void release() {
        instance = null;
    }
}
