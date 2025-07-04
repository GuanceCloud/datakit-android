package com.ft.sdk;

/**
 * BY huangDianHua
 * DATE:2019-12-20 14:36
 *
 * Declare automatic capture types, used to establish filtering system, for later {@link FTAutoTrack} blacklist and whitelist configuration
 *
 */
public enum FTAutoTrackType {
//    APP_START(1),
//    APP_END(1 << 1),
    /**
     * Application click event
     */
    APP_CLICK(1 << 2);

    /**
     * Auto track type
     */
    public final int type;

    /**
     * Internal constructor method
     * @param type
     */
    FTAutoTrackType(int type) {
        this.type = type;
    }
}
