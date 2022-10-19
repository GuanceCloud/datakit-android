package com.ft.sdk;

/**
 * author: huangDianHua
 * time: 2020/8/3 19:33:32
 * description:日志数据库缓存丢弃策略
 */
public enum LogCacheDiscard {
    //丢弃前置
    DISCARD,
    //丢弃后置
    DISCARD_OLDEST
}
