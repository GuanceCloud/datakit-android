package com.ft.sdk;

import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;

public class FTTraceConfigManager {

    private static final String TAG = "FTTraceConfigManager";


    private static class SingletonHolder {
        private static final FTTraceConfigManager INSTANCE = new FTTraceConfigManager();
    }

    public static FTTraceConfigManager get() {
        return FTTraceConfigManager.SingletonHolder.INSTANCE;
    }

    private FTTraceConfig config;


    void initWithConfig(FTTraceConfig config) {
        if (config.isEnableAutoTrace()) {
            if (!PackageUtils.isOKHttp3Support()) {
                LogUtils.e(TAG, "检测到 Trace EnableAutoTrace = true，" +
                        "但是你没有依赖 okHttp，此功能仅支持 okHttp");
            }
        }
        if (config.isEnableLinkRUMData() && config.getTraceType() != TraceType.DDTRACE) {
            LogUtils.e(TAG, "FTTraceConfig.isEnableLinkRUMData,仅支持 TraceType.DDTRACE");
        }
        this.config = config;
    }

    public FTTraceConfig getConfig() {
        return config;
    }

    public boolean isEnableAutoTrace() {
        return config != null && config.isEnableAutoTrace();
    }

    public boolean isEnableLinkRUMData() {
        return config != null && config.isEnableLinkRUMData();
    }

    public boolean isEnableWebTrace() {
        return config != null && config.isEnableWebTrace();
    }

    void release() {
        config = null;
    }

}
