package com.ft.sdk.garble.http;

/**
 * create: by huangDianHua
 * time: 2020/4/21 15:44:27
 * description: Network request engine, the purpose is to support different network request frameworks, currently supports Android {@link java.net.HttpURLConnection}
 * and {@link OkHttpEngine} two
 */
public interface INetEngine {
    /**
     * Request default configuration
     *
     * @param httpBuilder
     */
    void defaultConfig(HttpBuilder httpBuilder);

    /**
     * Establish synchronous network request
     *
     * @param httpBuilder
     */
    void createRequest(HttpBuilder httpBuilder);

    /**
     * Execute request
     *
     * @return
     */
    FTResponseData execute();
}
