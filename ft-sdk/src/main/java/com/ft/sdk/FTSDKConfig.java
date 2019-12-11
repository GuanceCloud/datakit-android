package com.ft.sdk;

import java.security.InvalidParameterException;

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
    private boolean useOAID;
    private boolean isDebug;

    public FTSDKConfig(String metricsUrl,boolean enableRequestSigning,String akId,String akSecret){
        this.metricsUrl = metricsUrl;
        this.enableRequestSigning = enableRequestSigning;
        this.akId = akId;
        this.akSecret = akSecret;
        if(enableRequestSigning){
            if(akId == null){
                throw new InvalidParameterException("akId 未初始化");
            }
            if(akSecret == null){
                throw new InvalidParameterException("akSecret 未初始化");
            }
        }
    }

    public String getMetricsUrl() {
        return metricsUrl;
    }


    public boolean isEnableRequestSigning() {
        return enableRequestSigning;
    }


    public String getAkId() {
        return akId;
    }

    public String getAkSecret() {
        return akSecret;
    }

    public boolean isUseOAID() {
        return useOAID;
    }

    public void setUseOAID(boolean useOAID) {
        this.useOAID = useOAID;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }
}
