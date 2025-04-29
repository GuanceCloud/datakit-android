package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;

public class FTRUMConfig {
    /**
     * 采样率，[0,1],作用域为同一 session_id 下所有 View，Action，LongTask，Error 数据
     */
    private float samplingRate = 1;

    /**
     * 错误采样率，[0,1] 作用域为同一 session_id 下所有 View，Action，LongTask，Error 数据
     */
    private float sessionErrorSampleRate = 0;

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
     * 设置检测阻塞时间范围 [100，),单位 ms,默认 1000 ms
     */
    private long blockDurationMS = FTUIBlockManager.DEFAULT_TIME_BLOCK_MS;
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
     * 是否开启用户行为 view 追踪 Fragment View 的采集
     */
    private boolean enableTraceUserViewInFragment;

    /**
     * 是否开启用户行为 Resource 追踪
     */
    private boolean enableTraceUserResource;


    /**
     * 开启 resource host ip 采集
     */
    private boolean enableResourceHostIP;
    /**
     * 崩溃采集数据附加类型
     */
    private int extraMonitorTypeWithError = ErrorMonitorType.NO_SET;

    /**
     * 监控指标数据类型
     */
    private int deviceMetricsMonitorType = DeviceMetricsMonitorType.NO_SET;

    /**
     * 设备监测指标检测周期
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
     * 设置全局 tag
     */
    private final HashMap<String, Object> globalContext = new HashMap<>();


    /**
     * 服务名称 {@link Constants#KEY_SERVICE },默认为 {@link Constants#DEFAULT_SERVICE_NAME}
     */
    private String serviceName = Constants.DEFAULT_SERVICE_NAME;

    /**
     * Java Crash 时 logcat 附属信息
     */
    private ExtraLogCatSetting extraLogCatWithJavaCrash;

    /**
     * Native Crash 时 logcat 附属信息
     */
    private ExtraLogCatSetting extraLogCatWithNativeCrash;

    /**
     * ANR  时 logcat 附属信息
     */
    private ExtraLogCatSetting extraLogCatWithANR;


    private int rumCacheLimitCount = Constants.DEFAULT_DB_RUM_CACHE_NUM;


    private RUMCacheDiscard rumCacheDiscardStrategy = RUMCacheDiscard.DISCARD;


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
     * 获取错误采样率
     *
     * @return
     */
    public float getSessionErrorSampleRate() {
        return sessionErrorSampleRate;
    }

    /**
     * 设置错误采样率，默认为 0
     * @param sessionErrorSampleRate
     */
    public FTRUMConfig setSessionErrorSampleRate(float sessionErrorSampleRate) {
        this.sessionErrorSampleRate = sessionErrorSampleRate;
        return this;
    }

    /**
     * 设置 serviceName
     *
     * @param serviceName
     */
    void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * 获取 serviceName
     *
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


    public int getRumCacheLimitCount() {
        return rumCacheLimitCount;
    }


    /**
     * 设置 RUM 限制数量 [10000,),默认是 100_000，{@link Constants#DEFAULT_DB_RUM_CACHE_NUM}
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
     * 设置日志丢弃策略
     *
     * @param rumCacheDiscardStrategy
     * @return
     */
    public FTRUMConfig setRumCacheDiscardStrategy(RUMCacheDiscard rumCacheDiscardStrategy) {
        this.rumCacheDiscardStrategy = rumCacheDiscardStrategy;
        return this;
    }

    /**
     * 设置是否监测 App 崩溃
     *
     * @param enableTrackAppCrash
     * @return
     */
    public FTRUMConfig setEnableTrackAppCrash(boolean enableTrackAppCrash) {
        this.enableTrackAppCrash = enableTrackAppCrash;
        return this;
    }

    /**
     * 设置是否监测 App 崩溃
     *
     * @param enableTrackAppCrash        是否开启 Crash 最总
     * @param extraLogCatWithJavaCrash   Java Crash 附加配置 logcat
     * @param extraLogCatWithNativeCrash Native Crash 附加配置 logcat
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
     * 是否监测 App UI卡顿
     *
     * @param enableTrackAppUIBlock
     * @return
     */
    public FTRUMConfig setEnableTrackAppUIBlock(boolean enableTrackAppUIBlock) {
        this.enableTrackAppUIBlock = enableTrackAppUIBlock;
        return this;
    }

    /**
     * 设置检测阻塞时间范围 [100，) 默认 1000 ms
     *
     * @param enableTrackAppUIBlock
     * @param blockDurationMs       单位 ms,默认 1000 ms
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
     * 是否开启 ANR 检测，默认为 false
     *
     * @param enableTrackAppANR
     * @return
     */

    public FTRUMConfig setEnableTrackAppANR(boolean enableTrackAppANR) {
        this.enableTrackAppANR = enableTrackAppANR;
        return this;
    }

    /**
     * 是否开启 ANR 检测，默认为 false
     *
     * @param enableTrackAppANR
     * @param extraLogCatWithANR ANR Crash 附加配置 logcat
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
     * 是否自动追踪用户操作，目前只支持用户启动和点击操作，默认为 `false`
     *
     * @param enableTraceUserAction
     * @return
     */
    public FTRUMConfig setEnableTraceUserAction(boolean enableTraceUserAction) {
        this.enableTraceUserAction = enableTraceUserAction;
        return this;
    }

    /**
     * 是否监测用户 View 行为，页面跳转
     *
     * @param enableTraceUserView
     * @return
     */
    public FTRUMConfig setEnableTraceUserView(boolean enableTraceUserView) {
        this.enableTraceUserView = enableTraceUserView;
        return this;
    }

    /**
     * 是否监测用户 View 在 Fragment 的跳转采集
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
     * 是否自动追动用户网络请求 ，仅支持 `Okhttp`，默认为 `false
     *
     * @param enableTraceUserResource
     * @return
     */
    public FTRUMConfig setEnableTraceUserResource(boolean enableTraceUserResource) {
        this.enableTraceUserResource = enableTraceUserResource;
        return this;
    }

    /**
     * 是否采集请求目标域名地址的 IP。作用域：只影响 `EnableTraceUserResource`  为 true 的默认采集。
     * 自定义 Resource 采集，需要使用 `FTResourceEventListener.FTFactory(true)` 来开启这个功能。
     * 另外，单个 Okhttp 对相同域名存在 IP 缓存机制，相同 `OkhttpClient`，在连接服务端 IP 不发生变化的前提下，只会生成一次
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
     * 使用 {@link #setDeviceMetricsMonitorType(int)} 替代
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
     * 支持或参数 battery | cpu | memory
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
     * 使用 {@link #setDeviceMetricsMonitorType(int)} 替代
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
     * 使用 {@link #setDeviceMetricsMonitorType(int, DetectFrequency)} 替代
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
     * 设置 Url 过滤规则
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
     * ASM 设置全局 Okhttp EventListener
     *
     * @param okHttpResourceHandler
     * @return
     */
    public FTRUMConfig setOkHttpEventListenerHandler(FTOkHttpEventListenerHandler okHttpResourceHandler) {
        this.okHttpEventListenerHandler = okHttpResourceHandler;
        return this;
    }


    /**
     * ASM 设置全局 {@link FTResourceInterceptor.ContentHandlerHelper } ，默认不设置
     *
     * @param contentHandlerHelper
     * @return
     */
    public FTRUMConfig setOkHttpResourceContentHandler(FTResourceInterceptor.ContentHandlerHelper contentHandlerHelper) {
        this.contentHandlerHelper = contentHandlerHelper;
        return this;
    }

    public Boolean isEnableResourceHostIP() {
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
