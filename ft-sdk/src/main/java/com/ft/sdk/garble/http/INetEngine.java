package com.ft.sdk.garble.http;

/**
 * create: by huangDianHua
 * time: 2020/4/21 15:44:27
 * description:
 */
public interface INetEngine {
    void defaultConfig(HttpBuilder httpBuilder);

    void createRequest(HttpBuilder httpBuilder);

    ResponseData execute();
}
