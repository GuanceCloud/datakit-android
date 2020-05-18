package com.ft.sdk;

import com.ft.sdk.garble.http.INetEngine;
import com.ft.sdk.garble.utils.DeviceUtils;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

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
    //高德逆向解析API 的 key
    private String geoKey;
    //是否使用高德作为逆向地址解析
    private boolean useGeoKey;
    //是否开启自动埋点
    private boolean autoTrack;
    //是否需要绑定用户数据
    private boolean needBindUser;
    //监控类别
    private int monitorType;
    //是否打开流程图
    private boolean openFlowChart;
    //以下三个为白名单
    private int enableAutoTrackType;
    private List<Class<?>> whiteActivityClass;
    private List<Class<?>> whiteViewClass;

    //以下三个为设置黑名单
    private int disableAutoTrackType;
    private List<Class<?>> blackActivityClass;
    private List<Class<?>> blackViewClass;
    private Class<? extends INetEngine> iNetEngineClass;

    //页面别名对应 map
    private Map<String,String> pageDescMap;
    //事件别名对应 map
    private Map<String,String> vtpDescMap;

    /**
     * 构建 SDK 必要的配置参数
     * @param metricsUrl 服务器地址
     * @param enableRequestSigning 是否需要对请求进行签名
     * @param akId 签名 id，当 enableRequestSigning 为 true 时必须设置
     * @param akSecret 签名 Secret，当 enableRequestSigning 为 true 时必须设置
     * @return
     */
    public static FTSDKConfig builder(String metricsUrl, boolean enableRequestSigning, String akId, String akSecret) {
        return new FTSDKConfig(metricsUrl, enableRequestSigning, akId, akSecret);
    }

    /**
     * 构建 SDK 必要的配置参数（当不需要签名时可以用此方法）
     * @param metricsUrl 服务器地址
     * @return
     */
    public static FTSDKConfig builder(String metricsUrl){
        return new FTSDKConfig(metricsUrl, false, null, null);
    }

    /**
     * SDK 配置项构造方法
     * @param metricsUrl
     * @param enableRequestSigning
     * @param akId
     * @param akSecret
     */
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

    public boolean isNeedBindUser() {
        return needBindUser;
    }

    public int getMonitorType() {
        return monitorType;
    }

    public boolean isOpenFlowChart() {
        return openFlowChart;
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

    public String getGeoKey() {
        return geoKey;
    }

    public boolean isUseGeoKey() {
        return useGeoKey;
    }

    public FTSDKConfig enableAutoTrack(boolean autoTrack) {
        this.autoTrack = autoTrack;
        return this;
    }
    public Class<? extends INetEngine> getINetEngineClass() {
        return iNetEngineClass;
    }

    public Map<String, String> getPageDescMap() {
        return pageDescMap;
    }

    public Map<String, String> getVtpDescMap() {
        return vtpDescMap;
    }

    /**
     * 是否使用 UseOAID 作为设备唯一识别号的替代字段
     *
     * @param useOAID
     * @return
     */
    public FTSDKConfig setUseOAID(boolean useOAID) {
        this.useOAID = useOAID;
        return this;
    }

    /**
     * 设置数据采集端的名称
     * @param uuid
     * @return
     */
    public FTSDKConfig setXDataKitUUID(String uuid){
        DeviceUtils.setSDKUUid(uuid);
        return this;
    }

    /**
     * 是否开启Debug，开启后将显示 SDK 运行日志
     *
     * @param debug
     * @return
     */
    public FTSDKConfig setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }

    /**
     * 设置自动埋点的事件类别
     *
     * @param type
     * @return
     */
    public FTSDKConfig setEnableAutoTrackType(int type) {
        enableAutoTrackType = type;
        return this;
    }

    /**
     * 设置监控类别
     *
     * @param monitorType
     * @return
     */
    public FTSDKConfig setMonitorType(int monitorType) {
        this.monitorType = monitorType;
        return this;
    }

    /**
     * 设置是否开启流程图
     *
     * @param openFlowChart
     * @return
     */
    public FTSDKConfig setOpenFlowChart(boolean openFlowChart) {
        this.openFlowChart = openFlowChart;
        return this;
    }

    /**
     * 是否需要绑定用户信息
     *
     * @param needBindUserVar
     * @return
     */
    public FTSDKConfig setNeedBindUser(boolean needBindUserVar) {
        needBindUser = needBindUserVar;
        return this;
    }

    /**
     * 设置白名单（Activity，Fragment）
     *
     * @param classes
     * @return
     */
    public FTSDKConfig setWhiteActivityClasses(List<Class<?>> classes) {
        whiteActivityClass = classes;
        return this;
    }

    /**
     * 设置控件白名单
     *
     * @param classes
     * @return
     */
    public FTSDKConfig setWhiteViewClasses(List<Class<?>> classes) {
        whiteViewClass = classes;
        return this;
    }

    /**
     * 设置关闭的自动埋点事件类别
     *
     * @param type
     * @return
     */
    public FTSDKConfig setDisableAutoTrackType(int type) {
        disableAutoTrackType = type;
        return this;
    }

    /**
     * 设置黑名单（Acitivty，Fragment）
     *
     * @param classes
     * @return
     */
    public FTSDKConfig setBlackActivityClasses(List<Class<?>> classes) {
        blackActivityClass = classes;
        return this;
    }

    /**
     * 设置控件黑名单
     *
     * @param classes
     * @return
     */
    public FTSDKConfig setBlackViewClasses(List<Class<?>> classes) {
        blackViewClass = classes;
        return this;
    }

    /**
     * 设置使用高德作为逆向地址解析
     * @param useGeoKey
     * @param geoKey
     * @return
     */
    public FTSDKConfig setGeoKey(boolean useGeoKey,String geoKey){
        this.useGeoKey = useGeoKey;
        this.geoKey = geoKey;
        return this;
    }

    public FTSDKConfig setINetEngineClass(Class<? extends INetEngine> iNetEngineClass) {
        this.iNetEngineClass = iNetEngineClass;
        return this;
    }

    /**
     * 页面别名对应 map
     * @param pageDescMap
     * @return
     */
    public FTSDKConfig addPageDesc(Map<String, String> pageDescMap) {
        this.pageDescMap = pageDescMap;
        return this;
    }

    /**
     * 事件别名对应 map
     * @param vtpDescMap
     * @return
     */
    public FTSDKConfig addVtpDesc(Map<String, String> vtpDescMap) {
        this.vtpDescMap = vtpDescMap;
        return this;
    }
}
