package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;


/**
 * author: huangDianHua
 * time: 2020/8/3 19:33:32
 * description:日志数据库缓存丢弃策略
 * 当日志堆积数量为 {@link FTLoggerConfig#getLogCacheLimitCount()},触发丢弃机制
 */

public enum LogCacheDiscard {
    /**
     * 丢弃新数据,默认
     */
    DISCARD,
    /**
     * 丢弃老数据
     */
    DISCARD_OLDEST
}
