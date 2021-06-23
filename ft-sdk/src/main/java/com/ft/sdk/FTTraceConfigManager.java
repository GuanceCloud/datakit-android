package com.ft.sdk;

public class FTTraceConfigManager {


    private static class SingletonHolder {
        private static final FTTraceConfigManager INSTANCE = new FTTraceConfigManager();
    }

    public static FTTraceConfigManager get() {
        return FTTraceConfigManager.SingletonHolder.INSTANCE;
    }

    private FTTraceConfig config;


    void initWithConfig(FTTraceConfig config) {
        if (config.isEnableLinkRUMData() && config.getTraceType() != TraceType.DDTRACE) {
            throw new InitSDKProcessException("FTTraceConfig.isEnableLinkRUMData,仅支持 TraceType.DDTRACE");
        }
        this.config = config;
    }

    public FTTraceConfig getConfig() {
        return config;
    }

    public boolean isNetworkTrace() {
        return config != null && config.isNetworkTrace();
    }

    public boolean isEnableLinkRUMData() {
        return config != null && config.isEnableLinkRUMData();
    }


    void release() {
        config = null;
    }

}
