package com.ft.sdk.garble.http;

import com.ft.sdk.FTTraceConfigManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:42:36
 * description:
 */
public class EngineFactory {
    public static final String TAG = "EngineFactory";

    public static INetEngine createEngine() {
        if (FTTraceConfigManager.get().isNetworkTrace()) {
            try {
                if (!PackageUtils.isOKHttp3Support()) {
                    LogUtils.e(TAG, "检测到你开启了网络链路追踪，但是你没有依赖 okHttp。使用该功能请先在项目中依赖 okHttp");
                    return new NativeNetEngine();
                }
                return new OkHttpEngine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new NativeNetEngine();
    }
}
