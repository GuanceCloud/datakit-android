package com.ft.sdk.garble.http;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.PackageUtils;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:42:36
 * description: Intelligent network request engine adapter
 */
public class EngineFactory {
    public static final String TAG = Constants.LOG_TAG_PREFIX + "EngineFactory";

    /**
     * Create synchronous engine object
     * @return
     */
    public static INetEngine createEngine() {
        //If the okhttp dependency library is detected, use Okhttp
        if (PackageUtils.isOKHttp3Support()) {
            return new OkHttpEngine();
        }
        //If no detection is found, use httpUrlConnection
        return new NativeNetEngine();
    }
}
