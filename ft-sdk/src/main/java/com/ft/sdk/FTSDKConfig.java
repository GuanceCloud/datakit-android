package com.ft.sdk;

import java.security.InvalidParameterException;
import java.util.List;

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
    private boolean autoTrack;
    //以下三个为白名单
    private int enableAutoTrackType;
    private List<Class<?>> onlyAutoTrackActivities;
    private List<Class<?>> onlyAutoTrackViews;

    //以下三个为设置黑名单
    private int disableAutoTrackType;
    private List<Class<?>> ignoreAutoTrackActivities;
    private List<Class<?>> ignoreAutoTrackViews;

    public static FTSDKConfig Builder(String metricsUrl, boolean enableRequestSigning, String akId, String akSecret) {
        return new FTSDKConfig(metricsUrl, enableRequestSigning, akId, akSecret);
    }

    private FTSDKConfig(String metricsUrl, boolean enableRequestSigning, String akId, String akSecret) {
        this.metricsUrl = metricsUrl;
        this.enableRequestSigning = enableRequestSigning;
        this.akId = akId;
        this.akSecret = akSecret;
        if (enableRequestSigning) {
            if (akId == null) {
                throw new InvalidParameterException("akId 未初始化");
            }
            if (akSecret == null) {
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

    public boolean isDebug() {
        return isDebug;
    }

    public boolean isAutoTrack() {
        return autoTrack;
    }

    public int getEnableAutoTrackType() {
        return enableAutoTrackType;
    }

    public List<Class<?>> getOnlyAutoTrackActivities(){
        return onlyAutoTrackActivities;
    }

    public List<Class<?>> getOnlyAutoTrackViews(){
        return onlyAutoTrackViews;
    }

    public int getDisableAutoTrackType(){
        return disableAutoTrackType;
    }

    public List<Class<?>> getIgnoreAutoTrackActivities() {
        return ignoreAutoTrackActivities;
    }

    public List<Class<?>> getIgnoreAutoTrackViews() {
        return ignoreAutoTrackViews;
    }

    public FTSDKConfig setAutoTrack(boolean autoTrack) {
        this.autoTrack = autoTrack;
        return this;
    }

    public FTSDKConfig setUseOAID(boolean useOAID) {
        this.useOAID = useOAID;
        return this;
    }

    public FTSDKConfig setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }

    public FTSDKConfig setEnableAutoTrackType(int type) {
        enableAutoTrackType = type;
        return this;
    }

    public FTSDKConfig setOnlyAutoTrackActivities(List<Class<?>> classes){
        onlyAutoTrackActivities = classes;
        return this;
    }

    public FTSDKConfig setOnlyAutoTrackViews(List<Class<?>> classes){
        onlyAutoTrackViews = classes;
        return this;
    }

    public FTSDKConfig setDisableAutoTrackType(int type){
        disableAutoTrackType = type;
        return this;
    }

    public FTSDKConfig setIgnoreAutoTrackActivities(List<Class<?>> classes) {
        ignoreAutoTrackActivities = classes;
        return this;
    }

    public FTSDKConfig setIgnoreAutoTrackViews(List<Class<?>> classes){
        ignoreAutoTrackViews = classes;
        return this;
    }

}
