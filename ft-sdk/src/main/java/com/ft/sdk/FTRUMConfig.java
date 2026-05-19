package com.ft.sdk;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;

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

    private FTViewFragmentTrackingHandler viewFragmentTrackingHandler;

    private FTViewActivityTrackingHandler viewActivityTrackingHandler;

    private FTActionTrackingHandler actionTrackingHandler;

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
     *
     * @param enableTraceWebView true to collect supported WebView RUM data
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTraceWebView(boolean enableTraceWebView) {
        this.enableTraceWebView = enableTraceWebView;
        return this;
    }

    /**
     * Configure allowed WebView host addresses for data tracking
     *
     * @param allowWebViewHost allowed WebView host list, or null to allow all hosts
     * @return this config for chaining
     */
    public FTRUMConfig setAllowWebViewHost(String[] allowWebViewHost) {
        this.allowWebViewHost = allowWebViewHost;
        return this;
    }

    /**
     * Returns whether WebView RUM data collection is enabled.
     */
    public boolean isEnableTraceWebView() {
        return enableTraceWebView;
    }

    /**
     * Returns the allowed WebView host list, or null when all hosts are allowed.
     */
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
     * Set sampling rate, range [0,1], 0 means no collection, 1 means full collection, default is 1.
     * Scope is all View, Action, LongTask, Error data under the same session_id
     *
     * @param samplingRate RUM session sampling rate in the range [0, 1]
     * @return this config for chaining
     */
    public FTRUMConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    /**
     * Get error sampling rate
     *
     * @return RUM error-session sampling rate in the range [0, 1]
     */
    public float getSessionErrorSampleRate() {
        return sessionErrorSampleRate;
    }

    /**
     * Set error sampling rate. When the session is not sampled by `setSamplingRate`,
     * if an error occurs during the session, data within 1 minute before the error can be collected.
     * Range [0,1], 0 means no collection, 1 means full collection, default is 0.
     * Scope is all View, Action, LongTask, Error under the same session_id
     *
     * @param sessionErrorSampleRate error-session sampling rate in the range [0, 1]
     */
    public FTRUMConfig setSessionErrorSampleRate(float sessionErrorSampleRate) {
        this.sessionErrorSampleRate = sessionErrorSampleRate;
        return this;
    }

    /**
     * Set serviceName
     *
     * @param serviceName service name inherited from {@link FTSDKConfig}
     */
    void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Get serviceName
     *
     * @return service name attached to RUM data
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

    /**
     * Sets the RUM application id used to enable RUM collection.
     *
     * @param rumAppId RUM application id
     * @return this config for chaining
     */
    public FTRUMConfig setRumAppId(String rumAppId) {
        this.rumAppId = rumAppId;
        return this;
    }

    /**
     * Returns whether app crash collection is enabled.
     */
    public boolean isEnableTrackAppCrash() {
        return enableTrackAppCrash;
    }


    /**
     * Returns the maximum number of RUM cache rows.
     */
    public int getRumCacheLimitCount() {
        return rumCacheLimitCount;
    }


    /**
     * Set RUM limit count [10000,), default is 100_000, {@link Constants#DEFAULT_DB_RUM_CACHE_NUM}
     *
     * @param rumCacheLimitCount maximum RUM cache row count
     * @return this config for chaining
     */
    public FTRUMConfig setRumCacheLimitCount(int rumCacheLimitCount) {
        this.rumCacheLimitCount = Math.max(Constants.MINI_DB_RUM_CACHE_NUM, rumCacheLimitCount);
//        this.rumCacheLimitCount =  rumCacheLimitCount;
        return this;
    }

    /**
     * Returns the RUM cache discard strategy.
     */
    public RUMCacheDiscard getRumCacheDiscardStrategy() {
        return rumCacheDiscardStrategy;
    }

    /**
     * Set log discard strategy
     *
     * @param rumCacheDiscardStrategy strategy used when the RUM cache limit is reached
     * @return this config for chaining
     */
    public FTRUMConfig setRumCacheDiscardStrategy(RUMCacheDiscard rumCacheDiscardStrategy) {
        this.rumCacheDiscardStrategy = rumCacheDiscardStrategy;
        return this;
    }

    /**
     * Set whether to monitor App crash
     *
     * @param enableTrackAppCrash true to collect Java and native crash data
     * @return this config for chaining
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
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTrackAppCrash(boolean enableTrackAppCrash,
                                              ExtraLogCatSetting extraLogCatWithJavaCrash,
                                              ExtraLogCatSetting extraLogCatWithNativeCrash) {
        this.enableTrackAppCrash = enableTrackAppCrash;
        this.extraLogCatWithJavaCrash = extraLogCatWithJavaCrash;
        this.extraLogCatWithNativeCrash = extraLogCatWithNativeCrash;
        return this;
    }
    /**
     * Returns whether UI block detection is enabled.
     */
    public boolean isEnableTrackAppUIBlock() {
        return enableTrackAppUIBlock;
    }

    /**
     * Whether to monitor App UI block
     *
     * @param enableTrackAppUIBlock true to collect UI block events
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTrackAppUIBlock(boolean enableTrackAppUIBlock) {
        this.enableTrackAppUIBlock = enableTrackAppUIBlock;
        return this;
    }

    /**
     * Set detection block time range [100, ) default 1000 ms
     *
     * @param enableTrackAppUIBlock true to collect UI block events
     * @param blockDurationMs       unit ms, default 1000 ms
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTrackAppUIBlock(boolean enableTrackAppUIBlock, long blockDurationMs) {
        this.enableTrackAppUIBlock = enableTrackAppUIBlock;
        this.blockDurationMS = blockDurationMs;
        return this;
    }
    /**
     * Returns whether ANR detection is enabled.
     */
    public boolean isEnableTrackAppANR() {
        return enableTrackAppANR;
    }

    /**
     * Whether to enable ANR detection, default is false
     *
     * @param enableTrackAppANR true to collect ANR events
     * @return this config for chaining
     */

    public FTRUMConfig setEnableTrackAppANR(boolean enableTrackAppANR) {
        this.enableTrackAppANR = enableTrackAppANR;
        return this;
    }

    /**
     * Whether to enable ANR detection, default is false
     *
     * @param enableTrackAppANR true to collect ANR events
     * @param extraLogCatWithANR ANR Crash additional logcat config
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTrackAppANR(boolean enableTrackAppANR, ExtraLogCatSetting extraLogCatWithANR) {
        this.enableTrackAppANR = enableTrackAppANR;
        this.extraLogCatWithANR = extraLogCatWithANR;
        return this;
    }
    /**
     * Returns whether automatic user action tracking is enabled.
     */
    public boolean isEnableTraceUserAction() {
        return enableTraceUserAction;
    }

    /**
     * Whether to automatically track user actions, currently only supports user start and click actions, default is `false`
     *
     * @param enableTraceUserAction true to collect supported user actions automatically
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTraceUserAction(boolean enableTraceUserAction) {
        this.enableTraceUserAction = enableTraceUserAction;
        return this;
    }

    /**
     * Whether to monitor user View behavior, page jump
     *
     * @param enableTraceUserView true to collect Activity view lifecycle automatically
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTraceUserView(boolean enableTraceUserView) {
        this.enableTraceUserView = enableTraceUserView;
        return this;
    }

    /**
     * Whether to monitor user View collection in Fragment
     *
     * @param enableTraceUserViewInFragment true to collect Fragment view lifecycle automatically
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTraceUserViewInFragment(boolean enableTraceUserViewInFragment) {
        this.enableTraceUserViewInFragment = enableTraceUserViewInFragment;
        return this;
    }

    /**
     * Returns whether automatic Activity view tracking is enabled.
     */
    public boolean isEnableTraceUserView() {
        return enableTraceUserView;
    }

    /**
     * Returns whether automatic Fragment view tracking is enabled.
     */
    public boolean isEnableTraceUserViewInFragment() {
        return enableTraceUserViewInFragment;
    }

    /**
     * Returns whether automatic OkHttp resource tracking is enabled.
     */
    public boolean isEnableTraceUserResource() {
        return enableTraceUserResource;
    }

    /**
     * Whether to automatically track user network requests, only supports `Okhttp`, default is `false`
     *
     * @param enableTraceUserResource true to collect supported OkHttp requests automatically
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTraceUserResource(boolean enableTraceUserResource) {
        this.enableTraceUserResource = enableTraceUserResource;
        return this;
    }

    /**
     * Whether to collect the IP address of the request target domain name.
     * Scope: only affects the default collection when `EnableTraceUserResource` is true.
     * For custom Resource collection, you need to use `FTResourceEventListener.FTFactory(true)`
     * to enable this function.
     * In addition, a single Okhttp has an IP cache mechanism for the same domain name.
     * Under the premise that the connection to the server IP does not change, only one will be
     * generated for the same `OkhttpClient`.
     *
     * @param enableTraceUserResource true to collect resource host IP values
     * @return this config for chaining
     */
    public FTRUMConfig setEnableResourceHostIP(boolean enableTraceUserResource) {
        this.enableResourceHostIP = enableTraceUserResource;
        return this;
    }

    /**
     * Returns the configured error monitor metric bit mask.
     */
    public int getExtraMonitorTypeWithError() {
        return extraMonitorTypeWithError;
    }

    /**
     * Use {@link #setDeviceMetricsMonitorType(int)} instead
     * <p>
     * setExtraMonitorTypeWithError(ErrorMonitorType.ALL.getValue())
     *
     * @param extraMonitorTypeWithError metric type to collect when an error occurs
     * @return this config for chaining
     */
    @Deprecated
    public FTRUMConfig setExtraMonitorTypeWithError(ErrorMonitorType extraMonitorTypeWithError) {
        this.extraMonitorTypeWithError = extraMonitorTypeWithError.getValue();
        return this;
    }

    /**
     * Sets the error monitor metric bit mask.
     *
     * @param extraMonitorTypeWithError bit mask built from {@link ErrorMonitorType}
     * @return this config for chaining
     */
    public FTRUMConfig setExtraMonitorTypeWithError(int extraMonitorTypeWithError) {
        this.extraMonitorTypeWithError = extraMonitorTypeWithError;
        return this;
    }


    /**
     * Returns whether RUM collection is enabled.
     */
    public boolean isRumEnable() {
        return !Utils.isNullOrEmpty(rumAppId);
    }

    /***
     * Add global parameters in RUM
     * @param key attribute key
     * @param value attribute value
     * @return this config for chaining
     */
    public FTRUMConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    /**
     * Returns global RUM attributes configured on this object.
     */
    public HashMap<String, Object> getGlobalContext() {
        return globalContext;
    }


    /**
     * Support or parameter battery | cpu | memory
     * <p>
     * setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL.getValue())
     *
     * @param deviceMetricsMonitorType bit mask built from {@link DeviceMetricsMonitorType}
     * @return this config for chaining
     */
    public FTRUMConfig setDeviceMetricsMonitorType(int deviceMetricsMonitorType) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType;
        return this;
    }

    /**
     * Use {@link #setDeviceMetricsMonitorType(int)} instead
     *
     * @param deviceMetricsMonitorType device metric type to collect
     * @return this config for chaining
     */
    @Deprecated
    public FTRUMConfig setDeviceMetricsMonitorType(DeviceMetricsMonitorType deviceMetricsMonitorType) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType.getValue();
        return this;
    }

    /**
     * Sets the device metric types and collection frequency for RUM view monitoring.
     *
     * @param deviceMetricsMonitorType bit mask built from {@link DeviceMetricsMonitorType}
     * @param frequency                metric collection frequency
     * @return this config for chaining
     */
    public FTRUMConfig setDeviceMetricsMonitorType(int deviceMetricsMonitorType, DetectFrequency frequency) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType;
        this.deviceMetricsDetectFrequency = frequency;
        return this;
    }

    /**
     * Use {@link #setDeviceMetricsMonitorType(int, DetectFrequency)} instead
     *
     * @param deviceMetricsMonitorType device metric type to collect
     * @param frequency                metric collection frequency
     * @return this config for chaining
     */
    @Deprecated
    public FTRUMConfig setDeviceMetricsMonitorType(DeviceMetricsMonitorType deviceMetricsMonitorType, DetectFrequency frequency) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType.getValue();
        this.deviceMetricsDetectFrequency = frequency;
        return this;
    }

    /**
     * Returns the configured device metric bit mask.
     */
    public int getDeviceMetricsMonitorType() {
        return deviceMetricsMonitorType;
    }

    /**
     * Returns the configured device metric collection frequency.
     */
    public DetectFrequency getDeviceMetricsDetectFrequency() {
        return deviceMetricsDetectFrequency;
    }

    /**
     * Set Url filter rule
     *
     * @param handler URL filter; returning true skips SDK resource collection
     * @return this config for chaining
     */
    public FTRUMConfig setResourceUrlHandler(FTInTakeUrlHandler handler) {
        this.intTakeUrlHandler = handler;
        return this;
    }
    /**
     * Returns the Resource URL filter handler.
     */
    public FTInTakeUrlHandler getResourceUrlHandler() {
        return intTakeUrlHandler;
    }

    FTOkHttpEventListenerHandler getOkHttpEventListenerHandler() {
        return okHttpEventListenerHandler;
    }

    /**
     * Returns the OkHttp resource content handler used by automatic resource tracking.
     */
    public FTResourceInterceptor.ContentHandlerHelper getOkHttpResourceContentHandler() {
        return contentHandlerHelper;
    }

    /**
     * Get the Fragment view tracking handler
     * 
     * @return The current Fragment view tracking handler, or null if not set
     * @see FTViewFragmentTrackingHandler
     */
    public FTViewFragmentTrackingHandler getViewFragmentTrackingHandler() {
        return viewFragmentTrackingHandler;
    }
    /**
     * Set the Fragment view tracking handler
     * 
     * This handler allows you to customize how Fragment views are tracked in RUM data.
     * When a Fragment lifecycle event occurs, this handler will be called to determine
     * the custom view name and properties for the Fragment.
     * 
     * <p>Usage example:</p>
     * <pre>
     * .setViewFragmentTrackingHandler(new FTViewFragmentTrackingHandler() {
     *     {@literal @}Override
     *     public HandlerView resolveHandlerView(FragmentWrapper fragment) {
     *         String fragmentName = fragment.getSimpleClassName();
     *         if (fragmentName.equals("HomeFragment")) {
     *             HashMap&lt;String, Object&gt; properties = new HashMap&lt;&gt;();
     *             properties.put("fragment_type", "home");
     *             return new HandlerView("Custom_Home_View", properties);
     *         }
     *         return null; // Skip tracking for other fragments
     *     }
     * })
     * </pre>
     * 
     * @param viewFragmentTrackingHandler The Fragment view tracking handler to set
     * @return This FTRUMConfig instance for method chaining
     * @see FTViewFragmentTrackingHandler
     * @see HandlerView
     * @see FragmentWrapper
     */
    public FTRUMConfig setViewFragmentTrackingHandler(FTViewFragmentTrackingHandler viewFragmentTrackingHandler) {
        this.viewFragmentTrackingHandler = viewFragmentTrackingHandler;
        return this;
    }

    /**
     * Get the Activity view tracking handler
     * 
     * @return The current Activity view tracking handler, or null if not set
     * @see FTViewActivityTrackingHandler
     */
    public FTViewActivityTrackingHandler getViewActivityTrackingHandler() {
        return viewActivityTrackingHandler;
    }

    /**
     * Set the Activity view tracking handler
     * 
     * This handler allows you to customize how Activity views are tracked in RUM data.
     * When an Activity lifecycle event occurs, this handler will be called to determine
     * the custom view name and properties for the Activity.
     * 
     * <p>Usage example:</p>
     * <pre>
     * .setViewActivityTrackingHandler(new FTViewActivityTrackingHandler() {
     *     {@literal @}Override
     *     public HandlerView resolveHandlerView(Activity activity) {
     *         String activityName = activity.getClass().getSimpleName();
     *         if (activityName.startsWith("Main")) {
     *             HashMap&lt;String, Object&gt; properties = new HashMap&lt;&gt;();
     *             properties.put("activity_type", "main");
     *             return new HandlerView("Custom_Main_Activity", properties);
     *         }
     *         return null; // Skip tracking for other activities
     *     }
     * })
     * </pre>
     * 
     * @param viewActivityTrackingHandler The Activity view tracking handler to set
     * @return This FTRUMConfig instance for method chaining
     * @see FTViewActivityTrackingHandler
     * @see HandlerView
     * @see Activity
     */
    public FTRUMConfig setViewActivityTrackingHandler(FTViewActivityTrackingHandler viewActivityTrackingHandler) {
        this.viewActivityTrackingHandler = viewActivityTrackingHandler;
        return this;
    }

    /**
     * Get the Action tracking handler
     * 
     * @return The current Action tracking handler, or null if not set
     * @see FTActionTrackingHandler
     */
    public FTActionTrackingHandler getActionTrackingHandler() {
        return actionTrackingHandler;
    }

    /**
     * Set the Action tracking handler
     * 
     * This handler allows you to customize how user actions are tracked in RUM data.
     * When a user action occurs (click, touch, etc.), this handler will be called to determine
     * the custom action name and properties for the action.
     * 
     * <p>Usage example:</p>
     * <pre>
     * .setActionTrackingHandler(new FTActionTrackingHandler() {
     *     {@literal @}Override
     *     public HandlerAction resolveHandlerAction(ActionEventWrapper actionEventWrapper) {
     *         ActionSourceType actionType = actionEventWrapper.getSourceType();
     *         if (actionType == ActionSourceType.CLICK_VIEW) {
     *             HashMap&lt;String, Object&gt; properties = new HashMap&lt;&gt;();
     *             properties.put("action_type", "button_click");
     *             return new HandlerAction("Custom_Button_Click", properties);
     *         }
     *         return null; // Skip tracking for other actions
     *     }
     * })
     * </pre>
     * 
     * @param actionTrackingHandler The Action tracking handler to set
     * @return This FTRUMConfig instance for method chaining
     * @see FTActionTrackingHandler
     * @see HandlerAction
     * @see ActionEventWrapper
     * @see ActionSourceType
     */
    public FTRUMConfig setActionTrackingHandler(FTActionTrackingHandler actionTrackingHandler) {
        this.actionTrackingHandler = actionTrackingHandler;
        return this;
    }

    /**
     * ASM sets global Okhttp EventListener
     *
     * @param okHttpResourceHandler handler that supplies the OkHttp event listener factory
     * @return this config for chaining
     */
    public FTRUMConfig setOkHttpEventListenerHandler(FTOkHttpEventListenerHandler okHttpResourceHandler) {
        this.okHttpEventListenerHandler = okHttpResourceHandler;
        return this;
    }


    /**
     * ASM sets global {@link FTResourceInterceptor.ContentHandlerHelper }, not set by default
     *
     * @param contentHandlerHelper callback used to append request, response, or exception attributes
     * @return this config for chaining
     */
    public FTRUMConfig setOkHttpResourceContentHandler(FTResourceInterceptor.ContentHandlerHelper contentHandlerHelper) {
        this.contentHandlerHelper = contentHandlerHelper;
        return this;
    }

    /**
     * Returns whether host IP collection is enabled for automatically tracked resources.
     */
    public boolean isEnableResourceHostIP() {
        return this.enableResourceHostIP;
    }

    /**
     * Returns the Java crash logcat capture configuration.
     */
    public ExtraLogCatSetting getExtraLogCatWithJavaCrash() {
        return extraLogCatWithJavaCrash;
    }

    /**
     * Returns the native crash logcat capture configuration.
     */
    public ExtraLogCatSetting getExtraLogCatWithNativeCrash() {
        return extraLogCatWithNativeCrash;
    }

    /**
     * Returns the ANR logcat capture configuration.
     */
    public ExtraLogCatSetting getExtraLogCatWithANR() {
        return extraLogCatWithANR;
    }

    /**
     * Returns the UI block detection threshold in milliseconds.
     */
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
