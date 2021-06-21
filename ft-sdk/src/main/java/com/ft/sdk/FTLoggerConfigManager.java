package com.ft.sdk;

import com.ft.sdk.garble.FTDBCachePolicy;

public class FTLoggerConfigManager {

    private static class SingletonHolder {
        private static final FTLoggerConfigManager INSTANCE = new FTLoggerConfigManager();
    }

    public static FTLoggerConfigManager get() {
        return FTLoggerConfigManager.SingletonHolder.INSTANCE;
    }

    private FTLoggerConfig config;


    public void initWithConfig(FTLoggerConfig config) {
        this.config = config;

        FTDBCachePolicy.get().initParam(config);
        FTLogger.getInstance().init(config);

    }

    public FTLoggerConfig getConfig() {
        return config;
    }

    void release() {
        config = null;
    }
}
