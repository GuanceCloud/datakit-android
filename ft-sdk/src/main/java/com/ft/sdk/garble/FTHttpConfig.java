package com.ft.sdk.garble;

import com.ft.sdk.BuildConfig;
import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.DeviceUtils;

import static com.ft.sdk.garble.utils.Constants.USER_AGENT;

/**
 * BY huangDianHua
 * DATE:2019-12-09 19:39
 * Description:
 */
public class FTHttpConfig {
    private static volatile FTHttpConfig instance;
    public String metricsUrl;
    public boolean enableRequestSigning;
    public String akId;
    public String akSecret;
    public String version;
    public String uuid;
    public String userAgent;
    public boolean useOaid;
    private FTHttpConfig() {

    }

    public synchronized static FTHttpConfig get() {
        if (instance == null) {
            instance = new FTHttpConfig();
        }
        return instance;
    }

    public void initParams(FTSDKConfig ftsdkConfig){
        if(ftsdkConfig == null){
            return;
        }
        metricsUrl = ftsdkConfig.getMetricsUrl();
        enableRequestSigning = ftsdkConfig.isEnableRequestSigning();
        akId = ftsdkConfig.getAkId();
        akSecret = ftsdkConfig.getAkSecret();
        useOaid = ftsdkConfig.isUseOAID();
        version = BuildConfig.VERSION_NAME;
        uuid = DeviceUtils.getSDKUUid(FTApplication.getApplication());
        userAgent = USER_AGENT;
    }


}
