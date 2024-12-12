package com.ft.sdk;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.FTDBCachePolicy;

/**
 * 日志配置管理
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
     * 日志配置初始化
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
     * 获取日志配置
     *
     * @return
     */
    @Nullable
    public FTLoggerConfig getConfig() {
        return config;
    }

    /**
     * 配置释放置空
     */
    void release() {
        config = null;
    }
}
