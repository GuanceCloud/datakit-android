package com.ft.sdk;

import com.ft.sdk.garble.utils.DeviceUtils;

import java.security.InvalidParameterException;

import static com.ft.sdk.garble.FTHttpConfig.USER_AGENT;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:40
 * Description:
 */
public class FTSDKConfig {
    private String metricsUrl;
    private boolean enableRequestSigning;
    private String akId;
    private String akSecret;
    private String version;
    private String uuid;
    private String userAgent;

    public FTSDKConfig(String metricsUrl){
        this.metricsUrl = metricsUrl;
    }

    public String getMetricsUrl() {
        return metricsUrl;
    }


    public boolean isEnableRequestSigning() {
        return enableRequestSigning;
    }

    public void setEnableRequestSigning(boolean enableRequestSigning) {
        this.enableRequestSigning = enableRequestSigning;
    }

    public String getAkId() {
        if(akId == null && enableRequestSigning){
            throw new InvalidParameterException("akId 未初始化");
        }
        return akId;
    }

    public void setAkId(String akId) {
        this.akId = akId;
    }

    public String getAkSecret() {
        if(akSecret == null && enableRequestSigning){
            throw new InvalidParameterException("akSecret 未初始化");
        }
        return akSecret;
    }

    public void setAkSecret(String akSecret) {
        this.akSecret = akSecret;
    }

    public String getVersion() {
        if(version == null){
            version = BuildConfig.VERSION_NAME;
        }
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        if(uuid == null){
            uuid = DeviceUtils.getSDKUUid(FTApplication.getApplication());
        }
        return uuid;
    }

    public String getUserAgent() {
        if(userAgent == null){
            userAgent = USER_AGENT;
        }
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

}
