package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;


/**
 * author: huangDianHua
 * time: 2020/8/3 19:33:32
 * description: Log database cache discard strategy
 * When the number of accumulated logs reaches {@link FTLoggerConfig#getLogCacheLimitCount()}, the discard mechanism is triggered
 */

public enum LogCacheDiscard {
    /**
     * Discard from the front, default
     */
    DISCARD,
    /**
     * Discard from the end
     */
    DISCARD_OLDEST
}
