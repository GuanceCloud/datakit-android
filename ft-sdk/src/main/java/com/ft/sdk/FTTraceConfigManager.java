package com.ft.sdk;

import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;

public class FTTraceConfigManager {

    private static final String TAG = "[FT-SDK]FTTraceConfigManager";


    private static class SingletonHolder {
        private static final FTTraceConfigManager INSTANCE = new FTTraceConfigManager();
    }

    public static FTTraceConfigManager get() {
        return FTTraceConfigManager.SingletonHolder.INSTANCE;
    }

    private FTTraceConfig config;


    /**
     *
     * @param config
     */
    void initWithConfig(FTTraceConfig config) {
        if (config.isEnableAutoTrace()) {
            if (!PackageUtils.isOKHttp3Support()) {
                LogUtils.e(TAG, "检测到 Trace EnableAutoTrace = true，" +
                        "但是你没有依赖 okHttp，此功能仅支持 okHttp");
            }
        }
        this.config = config;
    }

    /**
     *
     * @return
     */
    public FTTraceConfig getConfig() {
        return config;
    }

    /**
     *
     * @return
     */
    public boolean isEnableAutoTrace() {
        return config != null && config.isEnableAutoTrace();
    }

    /**
     *
     * @return
     */
    public boolean isEnableLinkRUMData() {
        return config != null && config.isEnableLinkRUMData();
    }

    /**
     *
     * @return
     */
    public boolean isEnableWebTrace() {
        return config != null && config.isEnableWebTrace();
    }

    /**
     *
     */
    void release() {
        config = null;
    }

}
