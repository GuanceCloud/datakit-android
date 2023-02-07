package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;

public class FTRUMConfig {
    /**
     * 采样率
     */
    private float samplingRate = 1;

    /**
     * RUM appID
     */
    private String rumAppId = "";
    /**
     * 设置是否需要采集崩溃日志
     */
    private boolean enableTrackAppCrash;
    /**
     * 设置是否检测 UI 卡顿
     */
    private boolean enableTrackAppUIBlock;
    /**
     * 设置是否检测 ANR
     */
    private boolean enableTrackAppANR;
    /**
     * 是否开启用户行为 action 追踪
     */
    private boolean enableTraceUserAction;
    /**
     * 是否开启用户行为 view 追踪
     */
    private boolean enableTraceUserView;
    /**
     * 是否开启用户欣慰 Resource 追踪
     */
    private boolean enableTraceUserResource;
    /**
     * 崩溃采集数据附加类型
     */
    private ErrorMonitorType extraMonitorTypeWithError = ErrorMonitorType.NO_SET;

    /**
     * 监控指标数据类型
     */
    private DeviceMetricsMonitorType deviceMetricsMonitorType = DeviceMetricsMonitorType.NO_SET;

    /**
     * 设备监测指标检测周期
     */
    private DetectFrequency deviceMetricsDetectFrequency = DetectFrequency.DEFAULT;

//    private boolean backendSample = false;

    private FTInTakeUrlHandler handler = url -> false;

    /**
     * 设置全局 tag
     */
    private final HashMap<String, Object> globalContext = new HashMap<>();


    private String serviceName = Constants.DEFAULT_SERVICE_NAME;

    /**
     * 获取采样率
     *
     * @return 采样率，浮点，0～1
     */
    public float getSamplingRate() {
        return samplingRate;
    }

    /**
     * 设置采用率
     *
     * @param samplingRate
     * @return
     */
    public FTRUMConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    /**
     * 设置 serviceName
     *
     * @param serviceName
     * @return
     */
    public FTRUMConfig setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * 获取 serviceName
     * @return
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * 获取 RUM AppId
     *
     * @return 返回 RUM AppId 字符
     */
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

    /**
     * 设置是否监测 App 崩溃
     * @param enableTrackAppCrash
     * @return
     */
    public FTRUMConfig setEnableTrackAppCrash(boolean enableTrackAppCrash) {
        this.enableTrackAppCrash = enableTrackAppCrash;
        return this;
    }

    public boolean isEnableTrackAppUIBlock() {
        return enableTrackAppUIBlock;
    }

    /**
     * 是否监测 App UI卡顿
     * @param enableTrackAppUIBlock
     * @return
     */
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

    public boolean isEnableTraceUserResource() {
        return enableTraceUserResource;
    }

    public FTRUMConfig setEnableTraceUserResource(boolean enableTraceUserResource) {
        this.enableTraceUserResource = enableTraceUserResource;
        return this;
    }

    public ErrorMonitorType getExtraMonitorTypeWithError() {
        return extraMonitorTypeWithError;
    }

    public FTRUMConfig setExtraMonitorTypeWithError(ErrorMonitorType extraMonitorTypeWithError) {
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

    public HashMap<String, Object> getGlobalContext() {
        return globalContext;
    }


    public FTRUMConfig setDeviceMetricsMonitorType(DeviceMetricsMonitorType deviceMetricsMonitorType) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType;
        return this;
    }

    public FTRUMConfig setDeviceMetricsMonitorType(DeviceMetricsMonitorType deviceMetricsMonitorType, DetectFrequency frequency) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType;
        this.deviceMetricsDetectFrequency = frequency;
        return this;
    }

    public DeviceMetricsMonitorType getDeviceMetricsMonitorType() {
        return deviceMetricsMonitorType;
    }

    public DetectFrequency getDeviceMetricsDetectFrequency() {
        return deviceMetricsDetectFrequency;
    }

    public FTRUMConfig setResourceUrlHandler(FTInTakeUrlHandler handler) {
        this.handler = handler;
        return this;
    }

    public FTInTakeUrlHandler getResourceUrlHandler() {
        return handler;
    }

    //    /**
//     * 设置 BackendSample 后端采样，当为 true 时，rum sampleRate 设置不再起效
//     *
//     * @param backendSample
//     * @return
//     */
//    public FTRUMConfig setBackendSample(boolean backendSample) {
//        this.backendSample = backendSample;
//        return this;
//    }
//
//    public boolean isBackendSample() {
//        return this.backendSample;
//    }
}
