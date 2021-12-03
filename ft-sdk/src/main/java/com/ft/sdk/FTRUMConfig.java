package com.ft.sdk;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class FTRUMConfig {
    private float samplingRate = 1;

    private String rumAppId = "";
    //设置是否需要采集崩溃日志
    private boolean enableTrackAppCrash;
    //设置是否检测 UI 卡顿
    private boolean enableTrackAppUIBlock;
    //设置是否检测 ANR
    private boolean enableTrackAppANR;
    //是否开启用户行为 action 追踪
    private boolean enableTraceUserAction;
    //是否开启用户行为 view 追踪
    private boolean enableTraceUserView;
    //崩溃采集数据附加类型
    private int extraMonitorTypeWithError;

    //设置全局 tag
    private HashMap<String, String> globalContext = new HashMap<>();

    public float getSamplingRate() {
        return samplingRate;
    }

    public FTRUMConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    public String getRumAppId() {
        return rumAppId;
    }

    public FTRUMConfig setRumAppId(String rumAppId) {
        this.rumAppId = rumAppId;
        return this;
    }

    public boolean isEnableTrackAppCrash() {
        return enableTrackAppCrash;
    }

    public FTRUMConfig setEnableTrackAppCrash(boolean enableTrackAppCrash) {
        this.enableTrackAppCrash = enableTrackAppCrash;
        return this;
    }

    public boolean isEnableTrackAppUIBlock() {
        return enableTrackAppUIBlock;
    }

    public FTRUMConfig setEnableTrackAppUIBlock(boolean enableTrackAppUIBlock) {
        this.enableTrackAppUIBlock = enableTrackAppUIBlock;
        return this;
    }

    public boolean isEnableTrackAppANR() {
        return enableTrackAppANR;
    }

    public FTRUMConfig setEnableTrackAppANR(boolean enableTrackAppANR) {
        this.enableTrackAppANR = enableTrackAppANR;
        return this;
    }

    public boolean isEnableTraceUserAction() {
        return enableTraceUserAction;
    }

    public FTRUMConfig setEnableTraceUserAction(boolean enableTraceUserAction) {
        this.enableTraceUserAction = enableTraceUserAction;
        return this;
    }

    public FTRUMConfig setEnableTraceUserView(boolean enableTraceUserView) {
        this.enableTraceUserView = enableTraceUserView;
        return this;
    }

    public boolean isEnableTraceUserView() {
        return enableTraceUserView;
    }

    public int getExtraMonitorTypeWithError() {
        return extraMonitorTypeWithError;
    }

    public FTRUMConfig setExtraMonitorTypeWithError(int extraMonitorTypeWithError) {
        this.extraMonitorTypeWithError = extraMonitorTypeWithError;
        return this;
    }

    public boolean isRumEnable() {
        return rumAppId != null;
    }

    public FTRUMConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    public HashMap<String, String> getGlobalContext() {
        return globalContext;
    }
}
