package com.ft.sdk.garble.db;

import com.ft.sdk.DBCacheDiscard;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.LogCacheDiscard;
import com.ft.sdk.RUMCacheDiscard;
import com.ft.sdk.garble.bean.DataType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

    private final AtomicLong currentDbSize = new AtomicLong();

    private final Object rumLock = new Object();

    private final Object logLock = new Object();

    public Object getRumLock() {
        return rumLock;
    }

    public Object getLogLock() {
        return logLock;
    }

    /**
     * 日志限制数量
     */
    private int logLimitCount = 0;


    /**
     * RUM 限制数据量
     */
    private int rumLimitCount = 0;

    private boolean enableLimitWithDbSize;

    private long dbLimitSize;

    private DBCacheDiscard dbCacheDiscard = DBCacheDiscard.DISCARD;

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
     * 设置 db 限制
     *
     * @param config
     */
    public void initSDKParams(FTSDKConfig config) {
        this.enableLimitWithDbSize = config.isLimitWithDbSize();
        this.dbLimitSize = config.getDbCacheLimit();
        this.dbCacheDiscard = config.getDbCacheDiscard();
    }

    /**
     * 初始化 log 配置参数
     *
     * @param config
     */
    public void initLogParam(FTLoggerConfig config) {
        this.logCacheDiscardStrategy = config.getLogCacheDiscardStrategy();
        this.logLimitCount = config.getLogCacheLimitCount();
    }

    /**
     * 初始化 RUM 配置参数
     *
     * @param config
     */
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
    public void optLogCount(int optCount) {
        if (enableLimitWithDbSize) return;
        logCount.addAndGet(optCount);
    }

    /**
     * 操作 RUM 数量
     *
     * @param optCount 写入数据数量
     */
    public void optRUMCount(int optCount) {
        if (enableLimitWithDbSize) return;
        rumCount.addAndGet(optCount);
    }

    /**
     *  设置当前 db 缓存文件大小
     * @param currentDbSize
     */
    public void setCurrentDBSize(long currentDbSize) {
        this.currentDbSize.set(currentDbSize);
    }

    /**
     * 是否达到 db 缓存限制
     * @return
     */
    public boolean isReachDbLimit() {
        return currentDbSize.get() >= dbLimitSize;
    }

    /**
     * 是否达到 db 缓存限制的一半
     * @return
     */
    public boolean reachHalfLimit() {
        if (enableLimitWithDbSize) {
            return currentDbSize.get() >= dbLimitSize / 2;
        } else {
            return reachLogHalfLimit() || reachRumHalfLimit();
        }
    }

    /**
     * 如果写入数据量大于总限制的一半
     *
     * @return 是否达到一半
     */
    private boolean reachLogHalfLimit() {
        return logLimitCount > 0 && logCount.get() > logLimitCount / 2;
    }

    /**
     * 如果写入数据大于总限制的一半
     *
     * @return
     */
    private boolean reachRumHalfLimit() {
        return rumLimitCount > 0 && rumCount.get() > rumLimitCount / 2;
    }

    /**
     * 执行 log 日志数据库缓存策略
     *
     * @return -1-表示直接丢弃，0-表示可以插入数据，n>0 表示需要删除 n 条数据
     */
    public int optLogCachePolicy(int limit) {
        if (enableLimitWithDbSize) {
            if (dbCacheDiscard == DBCacheDiscard.DISCARD) {
                if (isReachDbLimit()) {
                    return -1;
                }
            }
            return 0;
        }

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
            }
        }
        return status;
    }

    /**
     * 执行RUM丢弃策略
     *
     * @param limit
     * @return @return -1-表示直接丢弃，0-表示可以插入数据, 1-表示丢弃删除旧数据并删除
     */
    public int optRUMCachePolicy(int limit) {
        if (enableLimitWithDbSize) {
            if (dbCacheDiscard == DBCacheDiscard.DISCARD) {
                if (isReachDbLimit()) {
                    return -1;
                }
            }
            return 0;
        }

        int status = 0;
        int currentRUMCount = rumCount.get();
        if (currentRUMCount >= rumLimitCount) {//当数据量大于配置的数据库最大存储量时，执行丢弃策略
            if (rumCacheDiscardStrategy == RUMCacheDiscard.DISCARD) {//直接丢弃数据
                status = -1;
            } else if (rumCacheDiscardStrategy == RUMCacheDiscard.DISCARD_OLDEST) {//丢弃数据库中的前几条数据
                FTDBManager.get().deleteOldestData(new DataType[]{DataType.RUM_APP, DataType.RUM_WEBVIEW}, limit);
                rumCount.set(FTDBManager.get().queryTotalCount(new DataType[]{DataType.RUM_APP, DataType.RUM_WEBVIEW}));
                status = 0;
            }
        } else {
            status = 1;
        }
        return status;
    }

    /**
     * 是否开启 db 限制
     * @return
     */
    boolean isEnableLimitWithDbSize() {
        return enableLimitWithDbSize;
    }

    /**
     * db 缓存丢弃策略
     * @return
     */
    DBCacheDiscard getDbCacheDiscard() {
        return dbCacheDiscard;
    }
}
