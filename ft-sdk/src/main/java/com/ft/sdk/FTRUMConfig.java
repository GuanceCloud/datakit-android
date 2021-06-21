package com.ft.sdk;

public class FTRUMConfig {
    private float samplingRate = 1;

    private String rumAppId = "";
    //设置是否需要采集崩溃日志
    private boolean enableTrackAppCrash;
    //设置是否检测 UI 卡顿
    private boolean enableTrackAppUIBlock;
    //设置是否检测 ANR
    private boolean enableTrackAppANR;
    //是否开启用户行为追踪
    private boolean enableTraceUserAction;
    //崩溃采集数据附加类型
    private int extraMonitorTypeWithError;

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
}
