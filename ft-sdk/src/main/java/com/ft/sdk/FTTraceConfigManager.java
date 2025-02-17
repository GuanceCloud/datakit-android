package com.ft.sdk;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;

public class FTTraceConfigManager {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTTraceConfigManager";


    private static class SingletonHolder {
        private static final FTTraceConfigManager INSTANCE = new FTTraceConfigManager();
    }

    public static FTTraceConfigManager get() {
        return FTTraceConfigManager.SingletonHolder.INSTANCE;
    }

    private FTTraceConfig config;


    /**
     * Trace 配置初始化
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
        FTTrackInner.getInstance().initTraceConfig(config);
    }

    /**
     * 返回自定义覆盖全局的 {@link FTTraceInterceptor}
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
     * 释放资源
     */
    void release() {
        config = null;
    }

}
