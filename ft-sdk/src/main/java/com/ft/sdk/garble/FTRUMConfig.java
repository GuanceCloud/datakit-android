package com.ft.sdk.garble;

public class FTRUMConfig {

    private static class SingletonHolder {
        private static final FTRUMConfig INSTANCE = new FTRUMConfig();
    }

    public static FTRUMConfig get() {
        return FTRUMConfig.SingletonHolder.INSTANCE;
    }

    public String appId;

    public boolean isRumEnable() {
        return true;
    }
}
