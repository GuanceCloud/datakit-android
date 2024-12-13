package com.ft.sdk.garble;

import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.LogCacheDiscard;
import com.ft.sdk.RUMCacheDiscard;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.db.FTDBManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * author: huangDianHua
 * time: 2020/8/4 15:41:54
 * description: 日志数据数据库缓存丢弃策略
 */
public class FTDBCachePolicy {
    private volatile static FTDBCachePolicy instance;

    /**
     * 当前日志数据量
     */
    private final AtomicInteger logCount = new AtomicInteger(0);

    /**
     * 当前 RUM 数据量
     */
    private final AtomicInteger rumCount = new AtomicInteger(0);

    /**
     * 日志限制数量
     */
    private int logLimitCount = 0;


    /**
     * RUM 限制数据量
     */
    private int rumLimitCount = 0;

    /**
     * log 数据舍弃规则
     */
    private LogCacheDiscard logCacheDiscardStrategy = LogCacheDiscard.DISCARD;

    /**
     * rum 数据舍弃规则
     */
    private RUMCacheDiscard rumCacheDiscardStrategy = RUMCacheDiscard.DISCARD;


    private FTDBCachePolicy() {
        logCount.set(FTDBManager.get().queryTotalCount(DataType.LOG));//初始化时从数据库中获取日志数据
        rumCount.set(FTDBManager.get().queryTotalCount(new DataType[]{DataType.RUM_APP, DataType.RUM_WEBVIEW}));//初始化时从数据库中获取日志数据
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
    public void initLogParam(FTLoggerConfig config) {
        this.logCacheDiscardStrategy = config.getLogCacheDiscardStrategy();
        this.logLimitCount = config.getLogCacheLimitCount();
    }

    public void initRUMParam(FTRUMConfig config) {
        this.rumCacheDiscardStrategy = config.getRumCacheDiscardStrategy();
        this.rumLimitCount = config.getRumCacheLimitCount();
    }

    /**
     * 获取限制数量
     *
     * @return
     */
    public int getLogLimitCount() {
        return logLimitCount;
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
    public synchronized void optLogCount(int optCount) {
        logCount.addAndGet(optCount);
    }

    /**
     * 操作 RUM 数量
     *
     * @param optCount 写入数据数量
     */
    public synchronized void optRUMCount(int optCount) {
        rumCount.addAndGet(optCount);
    }


    /**
     * 如果写入数据量大于总限制的一半
     *
     * @return 是否达到一半
     */
    public boolean reachLogHalfLimit() {
        return logLimitCount > 0 && logCount.get() > logLimitCount / 2;
    }

    /**
     * 如果写入数据大于总限制的一半
     *
     * @return
     */
    public boolean reachRumHalfLimit() {
        return rumLimitCount > 0 && rumCount.get() > rumLimitCount / 2;
    }

    /**
     * 执行 log 日志数据库缓存策略
     *
     * @return -1-表示直接丢弃，0-表示可以插入数据，n>0 表示需要删除 n 条数据
     */
    public  int optLogCachePolicy(int limit) {
        int status = 0;
        int currentLogCount = logCount.get();
        if (currentLogCount >= logLimitCount) {//当数据量大于配置的数据库最大存储量时，执行丢弃策略
            if (logCacheDiscardStrategy == LogCacheDiscard.DISCARD) {//直接丢弃数据
                status = -1;
            } else if (logCacheDiscardStrategy == LogCacheDiscard.DISCARD_OLDEST) {//丢弃数据库中的前几条数据
                FTDBManager.get().deleteOldestData(DataType.LOG, limit);
                logCount.set(FTDBManager.get().queryTotalCount(DataType.LOG));
                status = 0;
            }
        } else {
            int needInsert = 0;
            if (currentLogCount + limit >= logLimitCount) {
                needInsert = logLimitCount - currentLogCount;
                status = limit - needInsert;
                limit = needInsert;
            }
            optLogCount(limit);
        }
        return status;
    }

    /**
     * 执行RUM丢弃策略
     *
     * @param limit
     * @return
     */
    public int optRUMCachePolicy(int limit) {
        int status = 0;
        int currentRUMCount = rumCount.get();
        if (currentRUMCount >= rumLimitCount) {//当数据量大于配置的数据库最大存储量时，执行丢弃策略
            if (rumCacheDiscardStrategy == RUMCacheDiscard.DISCARD) {//直接丢弃数据
                status = -1;
            } else if (rumCacheDiscardStrategy == RUMCacheDiscard.DISCARD_OLDEST) {//丢弃数据库中的前几条数据
                FTDBManager.get().deleteOldestData(new DataType[]{DataType.RUM_APP, DataType.RUM_WEBVIEW}, limit);
                status = 0;
            }
        } else {
            optRUMCount(limit);
            status = 1;
        }
        return status;
    }

}
