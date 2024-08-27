package com.ft.sdk.garble;

import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.LogCacheDiscard;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.LogUtils;

/**
 * author: huangDianHua
 * time: 2020/8/4 15:41:54
 * description: 日志数据数据库缓存丢弃策略
 */
public class FTDBCachePolicy {
    private volatile static FTDBCachePolicy instance;

    /**
     * 当前数据量
     */
    private volatile int count = 0;

    /**
     * 限制数量
     */
    private int limitCount = 0;

    /**
     * 数据舍弃规则
     */
    private LogCacheDiscard logCacheDiscardStrategy = LogCacheDiscard.DISCARD;


    private FTDBCachePolicy() {
        count = FTDBManager.get().queryTotalCount(DataType.LOG);//初始化时从数据库中获取日志数据
    }

    public synchronized static FTDBCachePolicy get() {
        if (instance == null) {
            instance = new FTDBCachePolicy();
        }
        return instance;
    }

    /**
     * 初始化配置参数
     *
     * @param config
     */
    public void initParam(FTLoggerConfig config) {
        this.logCacheDiscardStrategy = config.getLogCacheDiscardStrategy();
        this.limitCount = config.getLogCacheLimitCount();
    }

    /**
     * 获取限制数量
     *
     * @return
     */
    public int getLimitCount() {
        return limitCount;
    }

    public static void release() {
        instance = null;
    }

    public LogCacheDiscard getLogCacheDiscardStrategy() {
        return logCacheDiscardStrategy;
    }

    /**
     * 操作 Log 日志计数
     *
     * @param optCount 写入数据数量
     */
    public synchronized void optCount(int optCount) {
        count += optCount;
    }

    /**
     * 如果写入数据量大于总限制的一半
     *
     * @return 是否达到一半
     */
    public boolean reachHalfLimit() {
        return limitCount > 0 && count > limitCount / 2;
    }

    /**
     * 执行 log 日志数据库缓存策略
     *
     * @return -1-表示直接丢弃，0-表示可以插入数据，n>0 表示需要删除 n 条数据
     */
    public int optLogCachePolicy(int limit) {
        int status = 0;
        if (count >= limitCount) {//当数据量大于配置的数据库最大存储量时，执行丢弃策略
            if (logCacheDiscardStrategy == LogCacheDiscard.DISCARD) {//直接丢弃数据
                status = -1;
            } else if (logCacheDiscardStrategy == LogCacheDiscard.DISCARD_OLDEST) {//丢弃数据库中的前几条数据
                FTDBManager.get().deleteOldestData(DataType.LOG, limit);
                count = FTDBManager.get().queryTotalCount(DataType.LOG);
                status = 0;
            }
        } else {
            int needInsert = 0;
            if (count + limit >= limitCount) {
                needInsert = limitCount - count;
                status = limit - needInsert;
                limit = needInsert;

            }
            optCount(limit);
        }
        return status;
    }
}
