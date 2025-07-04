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
     * Trace configuration initialization
     *
     * @param config
     */
    void initWithConfig(FTTraceConfig config) {
        if (config.isEnableAutoTrace()) {
            if (!PackageUtils.isOKHttp3Support()) {
                LogUtils.e(TAG, "Detected Trace EnableAutoTrace = true, but you do not depend on okHttp, this feature only supports okHttp");
            }
        }
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
