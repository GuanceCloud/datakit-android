package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;
import java.util.List;

public class FTRUMConfig {
    /**
     * Sampling rate, [0,1], scope is all View, Action, LongTask, Error data under the same session_id
     */
    private float samplingRate = 1;

    /**
     * Error sampling rate, [0,1], scope is all View, Action, LongTask, Error data under the same session_id
     */
    private float sessionErrorSampleRate = 0;

    /**
     * RUM appID
     */
    private String rumAppId = "";
    /**
     * Set whether to collect crash logs
     */
    private boolean enableTrackAppCrash;
    /**
     * Set whether to detect UI block
     */
    private boolean enableTrackAppUIBlock;

    /**
     * Set the detection block time range [100, ), unit ms, default 1000 ms
     */
    private long blockDurationMS = FTUIBlockManager.DEFAULT_TIME_BLOCK_MS;
    /**
     * Set whether to detect ANR
     */
    private boolean enableTrackAppANR;
    /**
     * Whether to enable user action tracking
     */
    private boolean enableTraceUserAction;
    /**
     * Whether to enable user view tracking
     */
    private boolean enableTraceUserView;

    /**
     * Whether to enable user view tracking for Fragment View collection
     */
    private boolean enableTraceUserViewInFragment;

    /**
     * Whether to enable user Resource tracking
     */
    private boolean enableTraceUserResource;

    /**
     * Enable resource host ip collection
     */
    private boolean enableResourceHostIP;
    /**
     * Crash collection data additional type
     */
    private int extraMonitorTypeWithError = ErrorMonitorType.NO_SET;

    /**
     * Monitoring metric data type
     */
    private int deviceMetricsMonitorType = DeviceMetricsMonitorType.NO_SET;

    /**
     * Device monitoring metric detection period
     */
    private DetectFrequency deviceMetricsDetectFrequency = DetectFrequency.DEFAULT;

//    private boolean backendSample = false;

    private FTInTakeUrlHandler intTakeUrlHandler = new FTInTakeUrlHandler() {
        @Override
        public boolean isInTakeUrl(String url) {
            return false;
        }
    };

    /**
     *
     */
    private FTOkHttpEventListenerHandler okHttpEventListenerHandler;


    /**
     *
     */
    private FTResourceInterceptor.ContentHandlerHelper contentHandlerHelper;

    /**
     * Set global tag
     */
    private final HashMap<String, Object> globalContext = new HashMap<>();


    /**
     * Service name {@link Constants#KEY_SERVICE }, default is {@link Constants#DEFAULT_SERVICE_NAME}
     */
    private String serviceName = Constants.DEFAULT_SERVICE_NAME;

    /**
     * Java Crash logcat additional information
     */
    private ExtraLogCatSetting extraLogCatWithJavaCrash;

    /**
     * Native Crash logcat additional information
     */
    private ExtraLogCatSetting extraLogCatWithNativeCrash;

    /**
     * ANR logcat additional information
     */
    private ExtraLogCatSetting extraLogCatWithANR;


    private int rumCacheLimitCount = Constants.DEFAULT_DB_RUM_CACHE_NUM;


    private RUMCacheDiscard rumCacheDiscardStrategy = RUMCacheDiscard.DISCARD;

    private boolean enableTraceWebView = true;
    private String[] allowWebViewHost;

    /**
     * Configure whether to enable WebView data collection via Android SDK
     * @param enableTraceWebView
     * @return
     */
    public FTRUMConfig setEnableTraceWebView(boolean enableTraceWebView) {
        this.enableTraceWebView = enableTraceWebView;
        return this;
    }

    /**
     * Configure allowed WebView host addresses for data tracking
     * @param allowWebViewHost
     * @return
     */
    public FTRUMConfig setAllowWebViewHost(String[] allowWebViewHost) {
        this.allowWebViewHost = allowWebViewHost;
        return this;
    }

    public boolean isEnableTraceWebView() {
        return enableTraceWebView;
    }

    public String[] getAllowWebViewHost() {
        return allowWebViewHost;
    }

    /**
     * Get sampling rate
     *
     * @return Sampling rate, float, 0~1
     */
    public float getSamplingRate() {
        return samplingRate;
    }

    /**
     * Set sampling rate, range [0,1], 0 means no collection, 1 means full collection, default is 1. Scope is all View, Action, LongTask, Error data under the same session_id
     *
     * @param samplingRate
     * @return
     */
    public FTRUMConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    /**
     * Get error sampling rate
     *
     * @return
     */
    public float getSessionErrorSampleRate() {
        return sessionErrorSampleRate;
    }

    /**
     * Set error sampling rate. When the session is not sampled by `setSamplingRate`, if an error occurs during the session, data within 1 minute before the error can be collected. Range [0,1], 0 means no collection, 1 means full collection, default is 0. Scope is all View, Action, LongTask, Error under the same session_id
     *
     * @param sessionErrorSampleRate
     */
    public FTRUMConfig setSessionErrorSampleRate(float sessionErrorSampleRate) {
        this.sessionErrorSampleRate = sessionErrorSampleRate;
        return this;
    }

    /**
     * Set serviceName
     *
     * @param serviceName
     */
    void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Get serviceName
     *
     * @return
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * Get RUM AppId
     *
     * @return Returns RUM AppId string
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


    public int getRumCacheLimitCount() {
        return rumCacheLimitCount;
    }


    /**
     * Set RUM limit count [10000,), default is 100_000, {@link Constants#DEFAULT_DB_RUM_CACHE_NUM}
     *
     * @param rumCacheLimitCount
     * @return
     */
    public FTRUMConfig setRumCacheLimitCount(int rumCacheLimitCount) {
        this.rumCacheLimitCount = Math.max(Constants.MINI_DB_RUM_CACHE_NUM, rumCacheLimitCount);
//        this.rumCacheLimitCount =  rumCacheLimitCount;
        return this;
    }


    public RUMCacheDiscard getRumCacheDiscardStrategy() {
        return rumCacheDiscardStrategy;
    }

    /**
     * Set log discard strategy
     *
     * @param rumCacheDiscardStrategy
     * @return
     */
    public FTRUMConfig setRumCacheDiscardStrategy(RUMCacheDiscard rumCacheDiscardStrategy) {
        this.rumCacheDiscardStrategy = rumCacheDiscardStrategy;
        return this;
    }

    /**
     * Set whether to monitor App crash
     *
     * @param enableTrackAppCrash
     * @return
     */
    public FTRUMConfig setEnableTrackAppCrash(boolean enableTrackAppCrash) {
        this.enableTrackAppCrash = enableTrackAppCrash;
        return this;
    }

    /**
     * Set whether to monitor App crash
     *
     * @param enableTrackAppCrash        Whether to enable Crash
     * @param extraLogCatWithJavaCrash   Java Crash additional logcat config
     * @param extraLogCatWithNativeCrash Native Crash additional logcat config
     * @return
     */
    public FTRUMConfig setEnableTrackAppCrash(boolean enableTrackAppCrash,
                                              ExtraLogCatSetting extraLogCatWithJavaCrash,
                                              ExtraLogCatSetting extraLogCatWithNativeCrash) {
        this.enableTrackAppCrash = enableTrackAppCrash;
        this.extraLogCatWithJavaCrash = extraLogCatWithJavaCrash;
        this.extraLogCatWithNativeCrash = extraLogCatWithNativeCrash;
        return this;
    }

    public boolean isEnableTrackAppUIBlock() {
        return enableTrackAppUIBlock;
    }

    /**
     * Whether to monitor App UI block
     *
     * @param enableTrackAppUIBlock
     * @return
     */
    public FTRUMConfig setEnableTrackAppUIBlock(boolean enableTrackAppUIBlock) {
        this.enableTrackAppUIBlock = enableTrackAppUIBlock;
        return this;
    }

    /**
     * Set detection block time range [100, ) default 1000 ms
     *
     * @param enableTrackAppUIBlock
     * @param blockDurationMs       unit ms, default 1000 ms
     * @return
     */
    public FTRUMConfig setEnableTrackAppUIBlock(boolean enableTrackAppUIBlock, long blockDurationMs) {
        this.enableTrackAppUIBlock = enableTrackAppUIBlock;
        this.blockDurationMS = blockDurationMs;
        return this;
    }

    public boolean isEnableTrackAppANR() {
        return enableTrackAppANR;
    }

    /**
     * Whether to enable ANR detection, default is false
     *
     * @param enableTrackAppANR
     * @return
     */

    public FTRUMConfig setEnableTrackAppANR(boolean enableTrackAppANR) {
        this.enableTrackAppANR = enableTrackAppANR;
        return this;
    }

    /**
     * Whether to enable ANR detection, default is false
     *
     * @param enableTrackAppANR
     * @param extraLogCatWithANR ANR Crash additional logcat config
     * @return
     */
    public FTRUMConfig setEnableTrackAppANR(boolean enableTrackAppANR, ExtraLogCatSetting extraLogCatWithANR) {
        this.enableTrackAppANR = enableTrackAppANR;
        this.extraLogCatWithANR = extraLogCatWithANR;
        return this;
    }

    public boolean isEnableTraceUserAction() {
        return enableTraceUserAction;
    }

    /**
     * Whether to automatically track user actions, currently only supports user start and click actions, default is `false`
     *
     * @param enableTraceUserAction
     * @return
     */
    public FTRUMConfig setEnableTraceUserAction(boolean enableTraceUserAction) {
        this.enableTraceUserAction = enableTraceUserAction;
        return this;
    }

    /**
     * Whether to monitor user View behavior, page jump
     *
     * @param enableTraceUserView
     * @return
     */
    public FTRUMConfig setEnableTraceUserView(boolean enableTraceUserView) {
        this.enableTraceUserView = enableTraceUserView;
        return this;
    }

    /**
     * Whether to monitor user View collection in Fragment
     *
     * @param enableTraceUserViewInFragment
     * @return
     */
    public FTRUMConfig setEnableTraceUserViewInFragment(boolean enableTraceUserViewInFragment) {
        this.enableTraceUserViewInFragment = enableTraceUserViewInFragment;
        return this;
    }

    public boolean isEnableTraceUserView() {
        return enableTraceUserView;
    }

    public boolean isEnableTraceUserViewInFragment() {
        return enableTraceUserViewInFragment;
    }

    public boolean isEnableTraceUserResource() {
        return enableTraceUserResource;
    }

    /**
     * Whether to automatically track user network requests, only supports `Okhttp`, default is `false`
     *
     * @param enableTraceUserResource
     * @return
     */
    public FTRUMConfig setEnableTraceUserResource(boolean enableTraceUserResource) {
        this.enableTraceUserResource = enableTraceUserResource;
        return this;
    }

    /**
     * Whether to collect the IP address of the request target domain name. Scope: only affects the default collection when `EnableTraceUserResource` is true.
     * For custom Resource collection, you need to use `FTResourceEventListener.FTFactory(true)` to enable this function.
     * In addition, a single Okhttp has an IP cache mechanism for the same domain name. Under the premise that the connection to the server IP does not change, only one will be generated for the same `OkhttpClient`.
     *
     * @param enableTraceUserResource
     * @return
     */
    public FTRUMConfig setEnableResourceHostIP(boolean enableTraceUserResource) {
        this.enableResourceHostIP = enableTraceUserResource;
        return this;
    }

    public int getExtraMonitorTypeWithError() {
        return extraMonitorTypeWithError;
    }

    /**
     * Use {@link #setDeviceMetricsMonitorType(int)} instead
     * <p>
     * setExtraMonitorTypeWithError(ErrorMonitorType.ALL.getValue())
     *
     * @param extraMonitorTypeWithError
     * @return
     */
    @Deprecated
    public FTRUMConfig setExtraMonitorTypeWithError(ErrorMonitorType extraMonitorTypeWithError) {
        this.extraMonitorTypeWithError = extraMonitorTypeWithError.getValue();
        return this;
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

    public HashMap<String, Object> getGlobalContext() {
        return globalContext;
    }


    /**
     * Support or parameter battery | cpu | memory
     * <p>
     * setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL.getValue())
     *
     * @param deviceMetricsMonitorType
     * @return
     */
    public FTRUMConfig setDeviceMetricsMonitorType(int deviceMetricsMonitorType) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType;
        return this;
    }

    /**
     * Use {@link #setDeviceMetricsMonitorType(int)} instead
     *
     * @param deviceMetricsMonitorType
     * @return
     */
    @Deprecated
    public FTRUMConfig setDeviceMetricsMonitorType(DeviceMetricsMonitorType deviceMetricsMonitorType) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType.getValue();
        return this;
    }

    public FTRUMConfig setDeviceMetricsMonitorType(int deviceMetricsMonitorType, DetectFrequency frequency) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType;
        this.deviceMetricsDetectFrequency = frequency;
        return this;
    }

    /**
     * Use {@link #setDeviceMetricsMonitorType(int, DetectFrequency)} instead
     *
     * @param deviceMetricsMonitorType
     * @return
     */
    @Deprecated
    public FTRUMConfig setDeviceMetricsMonitorType(DeviceMetricsMonitorType deviceMetricsMonitorType, DetectFrequency frequency) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType.getValue();
        this.deviceMetricsDetectFrequency = frequency;
        return this;
    }

    public int getDeviceMetricsMonitorType() {
        return deviceMetricsMonitorType;
    }

    public DetectFrequency getDeviceMetricsDetectFrequency() {
        return deviceMetricsDetectFrequency;
    }

    /**
     * Set Url filter rule
     *
     * @param handler
     * @return
     */
    public FTRUMConfig setResourceUrlHandler(FTInTakeUrlHandler handler) {
        this.intTakeUrlHandler = handler;
        return this;
    }

    public FTInTakeUrlHandler getResourceUrlHandler() {
        return intTakeUrlHandler;
    }

    FTOkHttpEventListenerHandler getOkHttpEventListenerHandler() {
        return okHttpEventListenerHandler;
    }

    public FTResourceInterceptor.ContentHandlerHelper getOkHttpResourceContentHandler() {
        return contentHandlerHelper;
    }

    /**
     * ASM sets global Okhttp EventListener
     *
     * @param okHttpResourceHandler
     * @return
     */
    public FTRUMConfig setOkHttpEventListenerHandler(FTOkHttpEventListenerHandler okHttpResourceHandler) {
        this.okHttpEventListenerHandler = okHttpResourceHandler;
        return this;
    }


    /**
     * ASM sets global {@link FTResourceInterceptor.ContentHandlerHelper }, not set by default
     *
     * @param contentHandlerHelper
     * @return
     */
    public FTRUMConfig setOkHttpResourceContentHandler(FTResourceInterceptor.ContentHandlerHelper contentHandlerHelper) {
        this.contentHandlerHelper = contentHandlerHelper;
        return this;
    }

    public boolean isEnableResourceHostIP() {
        return this.enableResourceHostIP;
    }

    public ExtraLogCatSetting getExtraLogCatWithJavaCrash() {
        return extraLogCatWithJavaCrash;
    }

    public ExtraLogCatSetting getExtraLogCatWithNativeCrash() {
        return extraLogCatWithNativeCrash;
    }

    public ExtraLogCatSetting getExtraLogCatWithANR() {
        return extraLogCatWithANR;
    }

    public long getBlockDurationMS() {
        return blockDurationMS;
    }

    @Override
    public String toString() {
        return "FTRUMConfig{" +
                "samplingRate=" + samplingRate +
                ", sessionErrorSampleRate=" + sessionErrorSampleRate +
                ", enableTrackAppCrash=" + enableTrackAppCrash +
                ", enableTrackAppUIBlock=" + enableTrackAppUIBlock +
                ", blockDurationMS=" + blockDurationMS +
                ", enableTrackAppANR=" + enableTrackAppANR +
                ", enableTraceUserAction=" + enableTraceUserAction +
                ", enableTraceUserView=" + enableTraceUserView +
                ", enableTraceUserResource=" + enableTraceUserResource +
                ", enableResourceHostIP=" + enableResourceHostIP +
                ", deviceMetricsDetectFrequency=" + deviceMetricsDetectFrequency +
                ", intTakeUrlHandler=" + intTakeUrlHandler +
                ", okHttpEventListenerHandler=" + okHttpEventListenerHandler +
                ", contentHandlerHelper=" + contentHandlerHelper +
                ", globalContext=" + globalContext +
                ", serviceName='" + serviceName + '\'' +
                ", extraLogCatWithJavaCrash=" + extraLogCatWithJavaCrash +
                ", extraLogCatWithNativeCrash=" + extraLogCatWithNativeCrash +
                ", extraLogCatWithANR=" + extraLogCatWithANR +
                ", rumCacheLimitCount=" + rumCacheLimitCount +
                ", rumCacheDiscardStrategy=" + rumCacheDiscardStrategy +
                '}';
    }

}
