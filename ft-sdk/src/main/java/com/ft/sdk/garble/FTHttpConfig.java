package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;

/**
 * BY huangDianHua
 * DATE:2019-12-09 19:39
 * Description:
 */
public class FTHttpConfig {
    private static volatile FTHttpConfig instance;
    public static String USER_AGENT= "ft_mobile_sdk_android";
    public String metricsUrl;
    public boolean enableRequestSigning;
    public String akId;
    public String akSecret;
    public String version;
    public String uuid;
    public String userAgent;
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
        if(enableRequestSigning) {
            akId = ftsdkConfig.getAkId();
            akSecret = ftsdkConfig.getAkSecret();
        }
        version = ftsdkConfig.getVersion();
        uuid = ftsdkConfig.getUuid();
        userAgent = ftsdkConfig.getUserAgent();
    }


}
