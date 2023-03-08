package com.ft.sdk.garble.http;

import com.ft.sdk.garble.utils.PackageUtils;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:42:36
 * description:智能适配网络请求引擎
 */
public class EngineFactory {
    public static final String TAG = "EngineFactory";

    public static INetEngine createEngine() {
        //如果监测到 okhttp 依赖库就是用 Okhttp
        if (PackageUtils.isOKHttp3Support()) {
            return new OkHttpEngine();
        }
        //如果没有检测到，那么就是用 httpUrlConnection
        return new NativeNetEngine();
    }
}
