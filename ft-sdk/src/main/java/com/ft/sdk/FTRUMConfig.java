package com.ft.sdk;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;

public class FTRUMConfig {
    /**
     * Sampling rate, [0,1], scope is all View, Action, LongTask, Error data under the same session_id
     */
    private float samplingRate = 1;
    private boolean percentageSamplingRateSet;

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
     * Custom field provider for automatically collected Crash and ANR Errors.
     */
    @Nullable
    private FTIssueDataProvider issueDataProvider;
    /**
     * Whether to enable user action tracking
     */
    private boolean enableTraceUserAction;
    /** Whether to collect experimental Heatmap data. Disabled by default. */
    private boolean enableHeatMap;
    /**
     * Whether to enable user view tracking
     */
    private boolean enableTraceUserView;

    /**
     * Whether to enable user view tracking for Fragment View collection
     */
    private boolean enableTraceUserViewInFragment;

    /** Opt-in URLConnection Resource collection, also gated by the Resource master switch. */
    private volatile boolean enableHttpURLConnectionResource = false;

    /** Whether to enable automatic user Resource tracking. */
    private volatile boolean enableTraceUserResource;

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
     * @deprecated Configure the shared RUM and Log WebView host list with
     * {@link FTSDKConfig#setAllowWebViewHost(String[])}.
     */
    @Deprecated
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
     * @deprecated Use {@link #getSessionSamplingRate()} with a percentage.
     */
    @Deprecated
    public float getSamplingRate() {
        return samplingRate;
    }

    /**
     * Set sampling rate, range [0,1], 0 means no collection, 1 means full collection, default is 1.
     * Scope is all View, Action, LongTask, Error data under the same session_id
     *
     * @param samplingRate RUM session sampling rate in the range [0, 1]
     * @return this config for chaining
     * @deprecated Use {@link #setSessionSamplingRate(float)} with a percentage.
     */
    @Deprecated
    public FTRUMConfig setSamplingRate(float samplingRate) {
        if (!percentageSamplingRateSet) {
            this.samplingRate = samplingRate;
        }
        return this;
    }

    /**
     * Returns the effective sessionSamplingRate percentage in [0, 100]. Default: 100.
     */
    public float getSessionSamplingRate() {
        return samplingRate * 100f;
    }

    /**
     * Sets sessionSamplingRate as a percentage. For example, 0.1f means 0.1%.
     * Takes precedence over the deprecated setSamplingRate, regardless of call order.
     * Remote configuration continues to override local configuration.
     *
     * @param sessionSamplingRate finite percentage in [0, 100]
     * @return this config for chaining
     * @throws IllegalArgumentException for a non-finite or out-of-range value
     */
    public FTRUMConfig setSessionSamplingRate(float sessionSamplingRate) {
        if (Float.isNaN(sessionSamplingRate) || Float.isInfinite(sessionSamplingRate)
                || sessionSamplingRate < 0f || sessionSamplingRate > 100f) {
            throw new IllegalArgumentException("sessionSamplingRate must be a finite number in [0, 100]");
        }
        this.samplingRate = sessionSamplingRate / 100f;
        this.percentageSamplingRateSet = true;
        return this;
    }

    // Remote values have already been normalized to the legacy probability scale.
    void setRemoteSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
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
     * Returns whether automatic ANR collection is enabled.
     *
     * @see #setEnableTrackAppANR(boolean)
     */
    public boolean isEnableTrackAppANR() {
        return enableTrackAppANR;
    }

    /**
     * Enables automatic ANR collection. The default is {@code false}.
     *
     * <p>On Android 11 (API 30) and above, the SDK does not run the live ANR watchdog or the
     * native ANR collector. Fatal ANRs are recovered from {@code ApplicationExitInfo} after a
     * later process start and reported as historical {@code anr_crash} Errors. Non-fatal live
     * ANRs are not collected on these API levels.</p>
     *
     * <p>On Android 10 (API 29) and below, the SDK uses the live watchdog and, when the native
     * component is available, the native ANR collector.</p>
     *
     * @param enableTrackAppANR {@code true} to collect ANR Errors using the strategy for the
     *                          device API level
     * @return this config for chaining
     */

    public FTRUMConfig setEnableTrackAppANR(boolean enableTrackAppANR) {
        this.enableTrackAppANR = enableTrackAppANR;
        return this;
    }

    /**
     * Enables automatic ANR collection and configures additional Logcat capture. The default is
     * {@code false}.
     *
     * <p>The API-level collection strategy is the same as
     * {@link #setEnableTrackAppANR(boolean)}. {@code extraLogCatWithANR} is used only by the live
     * watchdog/native ANR paths on Android 10 (API 29) and below. It is not applied to historical
     * {@code ApplicationExitInfo} reports on Android 11 (API 30) and above.</p>
     *
     * @param enableTrackAppANR {@code true} to collect ANR Errors using the strategy for the
     *                          device API level
     * @param extraLogCatWithANR additional Logcat configuration for supported live/native ANR
     *                           collection paths
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTrackAppANR(boolean enableTrackAppANR, ExtraLogCatSetting extraLogCatWithANR) {
        this.enableTrackAppANR = enableTrackAppANR;
        this.extraLogCatWithANR = extraLogCatWithANR;
        return this;
    }

    /**
     * Sets a synchronous provider for custom fields on automatically collected Crash and ANR Errors.
     * The SDK snapshots this provider during RUM initialization. Changing this config afterwards does
     * not affect the active SDK instance.
     *
     * <p>Setting a provider does not enable Crash or ANR collection. Enable the required automatic
     * collection sources with {@link #setEnableTrackAppCrash(boolean)} and/or
     * {@link #setEnableTrackAppANR(boolean)}.</p>
     *
     * @param provider provider to invoke, or {@code null} to disable custom issue fields
     * @return this config for chaining
     */
    public FTRUMConfig setIssueDataProvider(@Nullable FTIssueDataProvider provider) {
        this.issueDataProvider = provider;
        return this;
    }

    @Nullable
    FTIssueDataProvider getIssueDataProvider() {
        return issueDataProvider;
    }

    /**
     * Returns whether automatic user action tracking is enabled.
     */
    public boolean isEnableTraceUserAction() {
        return enableTraceUserAction;
    }

    /** Returns whether experimental Heatmap collection is requested. */
    public boolean isEnableHeatMap() {
        return enableHeatMap;
    }

    /**
     * Enables experimental Heatmap collection for RUM and native Session Replay. Default is false.
     * Configure before {@link FTSdk#initRUMWithConfig(FTRUMConfig)}; runtime switching is not supported.
     * Disabling skips Heatmap target lookup, resource-name/path resolution and replay bindings,
     * while ordinary RUM and Session Replay recording remain available. Enabling does not enable
     * automatic Action tracking or change Session Replay sampling.
     *
     * @param enableHeatMap true to collect Heatmap data
     * @return this config for chaining
     */
    public FTRUMConfig setEnableHeatMap(boolean enableHeatMap) {
        this.enableHeatMap = enableHeatMap;
        return this;
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
     * Returns whether the automatic Resource master switch is enabled.
     */
    public boolean isEnableTraceUserResource() {
        return enableTraceUserResource;
    }

    /**
     * Enables automatic Resource collection. The default is {@code false}.
     * URLConnection also requires {@link #setEnableHttpURLConnectionResource(boolean)}.
     *
     * <p>With a compatible {@code ft-plugin} version, this also instruments OkHttp
     * {@code newWebSocket()} calls and records one Resource for each WebSocket handshake. Without
     * plug-in instrumentation, use the explicit {@link FTWebSocket} entry points for the same
     * collection behavior. The Resource ends when the handshake succeeds, is rejected, or fails;
     * it does not measure the connection lifetime or collect messages.</p>
     *
     * <p>Existing Resource URL filtering and RUM sampling rules also apply to WebSocket handshake
     * Resources. A third-party library that hides both its OkHttp {@code newWebSocket()} call site
     * and its {@link okhttp3.WebSocket.Factory} requires plug-in instrumentation. WebSocket
     * implementations that do not use OkHttp are not supported.</p>
     *
     * @param enableTraceUserResource {@code true} to collect supported OkHttp HTTP requests and
     *                                WebSocket handshakes automatically or explicitly
     * @return this config for chaining
     */
    public FTRUMConfig setEnableTraceUserResource(boolean enableTraceUserResource) {
        this.enableTraceUserResource = enableTraceUserResource;
        return this;
    }

    /**
     * Enables HTTP/HTTPS URLConnection Resources when the Resource master switch is also enabled.
     * Default false. Requires ft-plugin 1.3.9-alpha01 or newer at the creation call site.
     * Total duration includes application stream consumption. No bodies are collected.
     * Trace headers and RUM trace/span linkage follow {@link FTTraceConfig}.
     * Connections created while disabled are not retroactively wrapped. Long-lived streams should be
     * excluded with {@link #setResourceUrlHandler(FTInTakeUrlHandler)} (five-minute idle retention).
     */
    public FTRUMConfig setEnableHttpURLConnectionResource(boolean enabled) {
        enableHttpURLConnectionResource = enabled;
        return this;
    }

    public boolean isEnableHttpURLConnectionResource() {
        return enableHttpURLConnectionResource;
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

    private FTVisitorIdConfig visitorIdConfig;

    /**
     * Setting a non-null configuration enables visitor headers; null (the default) disables them.
     * Independent of RUM/Trace sampling and Resource collection.
     * Configuration is captured during initRUMWithConfig; subsequent mutations do not apply.
     */
    public FTRUMConfig setVisitorIdConfig(FTVisitorIdConfig config) {
        visitorIdConfig = config;
        return this;
    }

    /** Returns the initialization-only Visitor ID configuration, or null when not configured. */
    public FTVisitorIdConfig getVisitorIdConfig() {
        return visitorIdConfig;
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
     * Selects device metrics for RUM views without manually building a bit mask.
     * Configure this before passing the config to {@link FTSdk#initRUMWithConfig(FTRUMConfig)}.
     * <pre>{@code
     * config.setDeviceMetricsMonitorTypes(
     *         DeviceMetricsMonitorType.CPU,
     *         DeviceMetricsMonitorType.MEMORY,
     *         DeviceMetricsMonitorType.FPS);
     *
     * // All metrics
     * config.setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType.ALL);
     *
     * // Disable device metrics
     * config.setDeviceMetricsMonitorTypes();
     * }</pre>
     * Each call replaces the previous selection. Duplicate types have no additional effect.
     * The configured collection interval is preserved, initially
     * {@link DetectFrequency#DEFAULT} (500 ms). See
     * {@link #setDeviceMetricsMonitorTypes(DetectFrequency, DeviceMetricsMonitorType...)}
     * to configure the interval.
     *
     * @param types non-null metric types; an empty argument list disables device metrics
     * @return this config for chaining
     * @throws IllegalArgumentException if the array or any element is null;
     *                                  the previous configuration is preserved
     */
    public FTRUMConfig setDeviceMetricsMonitorTypes(@NonNull DeviceMetricsMonitorType... types) {
        return setDeviceMetricsMonitorType(toDeviceMetricsMonitorMask(types));
    }

    /**
     * Selects device metrics and their collection interval for RUM views.
     * Configure this before passing the config to {@link FTSdk#initRUMWithConfig(FTRUMConfig)}.
     * <pre>{@code
     * config.setDeviceMetricsMonitorTypes(
     *         DetectFrequency.RARE,
     *         DeviceMetricsMonitorType.CPU,
     *         DeviceMetricsMonitorType.MEMORY);
     * }</pre>
     * Each call replaces both the previous selection and collection interval.
     * Duplicate types have no additional effect; {@link DeviceMetricsMonitorType#ALL}
     * selects all metrics. No metric arguments disable device metrics while updating the interval.
     * <p>
     * Prefer {@link DetectFrequency#DEFAULT} (500 ms) for normal use.
     * {@link DetectFrequency#RARE} (1000 ms) reduces sampling frequency but may miss
     * short-lived changes. Reserve {@link DetectFrequency#FREQUENT} (100 ms) for
     * short-term diagnostics after evaluating collection overhead.
     *
     * @param frequency non-null metric collection frequency
     * @param types non-null metric types; an empty argument list disables device metrics
     * @return this config for chaining
     * @throws IllegalArgumentException if the frequency, array, or any element is null;
     *                                  the previous configuration is preserved
     */
    public FTRUMConfig setDeviceMetricsMonitorTypes(@NonNull DetectFrequency frequency,
                                                  @NonNull DeviceMetricsMonitorType... types) {
        if (frequency == null) {
            throw new IllegalArgumentException("Device metric collection frequency must not be null");
        }
        return setDeviceMetricsMonitorType(toDeviceMetricsMonitorMask(types), frequency);
    }

    private static int toDeviceMetricsMonitorMask(DeviceMetricsMonitorType... types) {
        if (types == null) {
            throw new IllegalArgumentException("Device metric types must not be null");
        }
        int mask = DeviceMetricsMonitorType.NO_SET;
        for (DeviceMetricsMonitorType type : types) {
            if (type == null) {
                throw new IllegalArgumentException("Device metric type must not be null");
            }
            mask |= type.getValue();
        }
        return mask;
    }

    /**
     * Sets the device metrics collected for RUM views. Monitoring is disabled by default.
     * For enum-based selection, prefer {@link #setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType...)}.
     * Configure this before passing the config to {@link FTSdk#initRUMWithConfig(FTRUMConfig)}.
     * <p>
     * Select only the metrics your application needs. For general view monitoring, consider
     * CPU, memory, and FPS; add battery monitoring when needed (unsupported on TV devices).
     * Use {@link DeviceMetricsMonitorType#getValue()} for each type and combine values with
     * the bitwise OR operator ({@code |}):
     * <pre>{@code
     * // Single metric
     * config.setDeviceMetricsMonitorType(DeviceMetricsMonitorType.CPU.getValue());
     *
     * // Multiple metrics
     * config.setDeviceMetricsMonitorType(
     *         DeviceMetricsMonitorType.CPU.getValue()
     *                 | DeviceMetricsMonitorType.MEMORY.getValue()
     *                 | DeviceMetricsMonitorType.FPS.getValue());
     *
     * // All metrics
     * config.setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL.getValue());
     *
     * // Disable device metrics
     * config.setDeviceMetricsMonitorType(DeviceMetricsMonitorType.NO_SET);
     * }</pre>
     * Each call replaces the previous selection; repeated calls do not add metrics.
     * This overload preserves the configured collection interval, initially
     * {@link DetectFrequency#DEFAULT} (500 ms). Use
     * {@link #setDeviceMetricsMonitorType(int, DetectFrequency)} to change the interval.
     *
     * @param deviceMetricsMonitorType bit mask built from {@link DeviceMetricsMonitorType}
     * @return this config for chaining
     * @deprecated Use {@link #setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType...)}
     * and pass enum values directly instead of combining their integer values.
     */
    @Deprecated
    public FTRUMConfig setDeviceMetricsMonitorType(int deviceMetricsMonitorType) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType;
        return this;
    }

    /**
     * Use {@link #setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType...)} instead.
     *
     * @param deviceMetricsMonitorType device metric type to collect
     * @return this config for chaining
     * @deprecated Use {@link #setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType...)}
     * and pass enum values directly.
     */
    @Deprecated
    public FTRUMConfig setDeviceMetricsMonitorType(DeviceMetricsMonitorType deviceMetricsMonitorType) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType.getValue();
        return this;
    }

    /**
     * Sets the device metric types and collection frequency for RUM view monitoring.
     * For enum-based selection, prefer
     * {@link #setDeviceMetricsMonitorTypes(DetectFrequency, DeviceMetricsMonitorType...)}.
     * See {@link #setDeviceMetricsMonitorType(int)} for metric selection examples.
     * Each call replaces both the previous selection and collection interval.
     * <p>
     * Prefer {@link DetectFrequency#DEFAULT} (500 ms) for normal use.
     * {@link DetectFrequency#RARE} (1000 ms) reduces sampling frequency but may miss
     * short-lived changes. Reserve {@link DetectFrequency#FREQUENT} (100 ms) for
     * short-term diagnostics after evaluating collection overhead.
     * <pre>{@code
     * config.setDeviceMetricsMonitorType(
     *         DeviceMetricsMonitorType.CPU.getValue()
     *                 | DeviceMetricsMonitorType.MEMORY.getValue(),
     *         DetectFrequency.RARE);
     * }</pre>
     *
     * @param deviceMetricsMonitorType bit mask built from {@link DeviceMetricsMonitorType}
     * @param frequency                non-null metric collection frequency
     * @return this config for chaining
     * @deprecated Use {@link #setDeviceMetricsMonitorTypes(DetectFrequency, DeviceMetricsMonitorType...)}
     * with the frequency first, followed by enum values.
     */
    @Deprecated
    public FTRUMConfig setDeviceMetricsMonitorType(int deviceMetricsMonitorType, DetectFrequency frequency) {
        this.deviceMetricsMonitorType = deviceMetricsMonitorType;
        this.deviceMetricsDetectFrequency = frequency;
        return this;
    }

    /**
     * Use {@link #setDeviceMetricsMonitorTypes(DetectFrequency, DeviceMetricsMonitorType...)} instead.
     *
     * @param deviceMetricsMonitorType device metric type to collect
     * @param frequency                non-null metric collection frequency
     * @return this config for chaining
     * @deprecated Use {@link #setDeviceMetricsMonitorTypes(DetectFrequency, DeviceMetricsMonitorType...)}
     * with the frequency first, followed by enum values.
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
                ", enableHeatMap=" + enableHeatMap +
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
