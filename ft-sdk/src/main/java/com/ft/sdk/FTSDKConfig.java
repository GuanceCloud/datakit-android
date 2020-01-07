package com.ft.sdk;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:40
 * Description:
 */
public class FTSDKConfig {
    //服务器地址
    private String metricsUrl;
    //是否签名
    private boolean enableRequestSigning;
    private String akId;
    private String akSecret;
    //是否使用OAID
    private boolean useOAID;
    //是否是Debug
    private boolean isDebug;
    //是否开启自动埋点
    private boolean autoTrack;
    //是否需要绑定用户数据
    private boolean needBindUser;
    //以下三个为白名单
    private int enableAutoTrackType;
    private List<Class<?>> whiteActivityClass;
    private List<Class<?>> whiteViewClass;

    //以下三个为设置黑名单
    private int disableAutoTrackType;
    private List<Class<?>> blackActivityClass;
    private List<Class<?>> blackViewClass;

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

    public boolean isNeedBindUser(){
        return needBindUser;
    }

    public int getEnableAutoTrackType() {
        return enableAutoTrackType;
    }

    public List<Class<?>> getWhiteActivityClass() {
        return whiteActivityClass;
    }

    public List<Class<?>> getWhiteViewClass() {
        return whiteViewClass;
    }

    public int getDisableAutoTrackType() {
        return disableAutoTrackType;
    }

    public List<Class<?>> getBlackActivityClass() {
        return blackActivityClass;
    }

    public List<Class<?>> getBlackViewClass() {
        return blackViewClass;
    }

    public FTSDKConfig enableAutoTrack(boolean autoTrack) {
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

    public FTSDKConfig setNeedBindUser(boolean needBindUserVar){
        needBindUser = needBindUserVar;
        return this;
    }

    public FTSDKConfig setWhiteActivityClasses(List<Class<?>> classes) {
        whiteActivityClass = classes;
        return this;
    }

    public FTSDKConfig setWhiteViewClasses(List<Class<?>> classes) {
        whiteViewClass = classes;
        return this;
    }

    public FTSDKConfig setDisableAutoTrackType(int type) {
        disableAutoTrackType = type;
        return this;
    }

    public FTSDKConfig setBlackActivityClasses(List<Class<?>> classes) {
        blackActivityClass = classes;
        return this;
    }

    public FTSDKConfig setBlackViewClasses(List<Class<?>> classes) {
        blackViewClass = classes;
        return this;
    }

}
