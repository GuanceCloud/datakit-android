package com.ft.sdk.garble.http;

import com.ft.sdk.garble.utils.PackageUtils;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:42:36
 * description:
 */
public class EngineFactory {
    public static final String TAG = "EngineFactory";

    public static INetEngine createEngine() {
        if (PackageUtils.isOKHttp3Support()) {
            return new OkHttpEngine();
        }
        return new NativeNetEngine();
    }
}
