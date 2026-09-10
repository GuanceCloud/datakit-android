package com.ft.sdk;

import androidx.annotation.Nullable;

public class FTTraceConfigManager {

    private static class SingletonHolder {
        private static final FTTraceConfigManager INSTANCE = new FTTraceConfigManager();
    }

    public static FTTraceConfigManager get() {
        return FTTraceConfigManager.SingletonHolder.INSTANCE;
    }

    private FTTraceConfig config;


    /**
     * Trace configuration initialization
     *
     * @param config
     */
    void initWithConfig(FTTraceConfig config) {
        this.config = config;
    }

    /**
     * Return custom override for global {@link FTTraceInterceptor}
     *
     * @return
     */
    FTTraceInterceptor.HeaderHandler getOverrideHeaderHandler() {
        if (config == null) return null;
        if (config.getOkHttpTraceHeaderHandler() == null) return null;
        return config.getOkHttpTraceHeaderHandler();
    }


    /**
     * @return
     */
    @Nullable
    public FTTraceConfig getConfig() {
        return config;
    }

    /**
     * @return
     */
    public boolean isEnableAutoTrace() {
        return config != null && config.isEnableAutoTrace();
    }

    /**
     * @return
     */
    public boolean isEnableLinkRUMData() {
        return config != null && config.isEnableLinkRUMData();
    }

    /**
     * Release resources
     */
    void release() {
        config = null;
    }

}
