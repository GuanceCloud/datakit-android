package com.ft.sdk;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.db.FTDBCachePolicy;

/**
 * Log configuration management
 *
 * @author Brandon
 */
public class FTLoggerConfigManager {

    private static class SingletonHolder {
        private static final FTLoggerConfigManager INSTANCE = new FTLoggerConfigManager();
    }

    public static FTLoggerConfigManager get() {
        return FTLoggerConfigManager.SingletonHolder.INSTANCE;
    }

    private FTLoggerConfig config;


    /**
     * Log configuration initialization
     *
     * @param config
     */
    void initWithConfig(FTLoggerConfig config) {
        this.config = config;

        FTDBCachePolicy.get().initLogParam(config);
        FTLogger.getInstance().init(config);
        FTTrackInner.getInstance().initLogConfig(config);

    }

    /**
     * Get log configuration
     *
     * @return
     */
    @Nullable
    public FTLoggerConfig getConfig() {
        return config;
    }

    /**
     * Release configuration and set to null
     */
    void release() {
        config = null;
    }
}
