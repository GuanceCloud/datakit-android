package com.ft.sdk.garble.http;

/**
 * create: by huangDianHua
 * time: 2020/4/21 15:44:27
 * description: 网络请求引擎，目的是为了支持不同网络请求框架设定的，目前支持 Android {@link java.net.HttpURLConnection}
 * 和 {@link OkHttpEngine} 两种
 *
 */
public interface INetEngine {
    /**
     * 请求默认配置
     * @param httpBuilder
     */
    void defaultConfig(HttpBuilder httpBuilder);

    /**
     * 建立同步网络请求
     * @param httpBuilder
     */
    void createRequest(HttpBuilder httpBuilder);

    /**
     * 执行请求
     * @return
     */
    FTResponseData execute();
}
