package com.ft.sdk;

import java.util.Arrays;
import java.util.List;

public class FTTraceConfigManager {

    public List<String> traceContentType = Arrays.asList("application/json",
            "application/javascript", "application/xml", "application/x-www-form-urlencoded",
            "text/html", "text/xml", "text/plain",
            "multipart/form-data"
    );

    private static class SingletonHolder {
        private static final FTTraceConfigManager INSTANCE = new FTTraceConfigManager();
    }

    public static FTTraceConfigManager get() {
        return FTTraceConfigManager.SingletonHolder.INSTANCE;
    }

    private FTTraceConfig config;


    public void initWithConfig(FTTraceConfig config) {
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
