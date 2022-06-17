package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.DeviceUtils;

import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:40
 * Description:
 */
public class FTSDKConfig {
    //服务器地址
    private final String metricsUrl;
    //是否是Debug
    private boolean isDebug;

    //崩溃日志的环境
    private EnvType env = EnvType.PROD;

    // SDK 是否只支持在主进程中初始化
    private boolean onlySupportMainProcess = true;

    private final HashMap<String, Object> globalContext = new HashMap<>();

    /**
     * 构建 SDK 必要的配置参数
     *
     * @param metricsUrl 服务器地址
     * @return
     */
    public static FTSDKConfig builder(String metricsUrl) {
        return new FTSDKConfig(metricsUrl);
    }


    /**
     * SDK 配置项构造方法
     *
     * @param metricsUrl
     */
    private FTSDKConfig(String metricsUrl) {
        this.metricsUrl = metricsUrl;
    }

    public String getMetricsUrl() {
        return metricsUrl;
    }


    public boolean isDebug() {
        return isDebug;
    }

    public EnvType getEnv() {
        return env;
    }

    public boolean isOnlySupportMainProcess() {
        return onlySupportMainProcess;
    }


    /**
     * 设置数据采集端的名称
     *
     * @param uuid
     * @return
     */
    public FTSDKConfig setXDataKitUUID(String uuid) {
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
     * 设置数据传输的环境
     *
     * @param env
     * @return
     */
    public FTSDKConfig setEnv(EnvType env) {
        if (env != null) {
            this.env = env;
        }
        return this;
    }


    /**
     * 是否只支持在主进程中初始化 SDK
     *
     * @param onlySupportMainProcess
     * @return
     */
    public FTSDKConfig setOnlySupportMainProcess(boolean onlySupportMainProcess) {
        this.onlySupportMainProcess = onlySupportMainProcess;
        return this;
    }

    /**
     * 添加全局属性
     *
     * @param key
     * @param value
     * @return
     */
    public FTSDKConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }



    /**
     * 获取全局属性
     *
     * @return
     */
    public HashMap<String, Object> getGlobalContext() {
        return this.globalContext;
    }

}
