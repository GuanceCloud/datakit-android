package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;

/**
 * BY huangDianHua
 * DATE:2019-12-19 18:59
 * Description:
 */
public class FTAutoTrackConfig {
    private boolean autoTrack;
    private static FTAutoTrackConfig instance;

    private FTAutoTrackConfig() {
    }

    public synchronized static FTAutoTrackConfig get() {
        if (instance == null) {
            instance = new FTAutoTrackConfig();
        }
        return instance;
    }

    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        autoTrack = ftsdkConfig.isAutoTrack();
    }

    public boolean isAutoTrack() {
        return autoTrack;
    }
}
