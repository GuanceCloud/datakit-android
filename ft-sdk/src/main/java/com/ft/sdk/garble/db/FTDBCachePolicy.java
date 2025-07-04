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
 * description: Log data database cache discard policy
 */
public class FTDBCachePolicy {
    private volatile static FTDBCachePolicy instance;

    /**
     * Current log data count
     */
    private final AtomicInteger logCount = new AtomicInteger(0);

    /**
     * Current RUM data count
     */
    private final AtomicInteger rumCount = new AtomicInteger(0);

    /**
     * Get current db cache size
     */
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
     * Log limit count
     */
    private int logLimitCount = 0;


    /**
     * RUM limit data count
     */
    private int rumLimitCount = 0;

    private boolean enableLimitWithDbSize;

    private long dbLimitSize;

    private DBCacheDiscard dbCacheDiscard = DBCacheDiscard.DISCARD;

    /**
     * log data discard rule
     */
    private LogCacheDiscard logCacheDiscardStrategy = LogCacheDiscard.DISCARD;

    /**
     * rum data discard rule
     */
    private RUMCacheDiscard rumCacheDiscardStrategy = RUMCacheDiscard.DISCARD;


    private FTDBCachePolicy() {
        logCount.set(FTDBManager.get().queryTotalCount(DataType.LOG));//Get log data from database during initialization
        rumCount.set(FTDBManager.get().queryTotalCount(new DataType[]{
                DataType.RUM_APP,
                DataType.RUM_WEBVIEW,
                DataType.RUM_APP_ERROR_SAMPLED,
                DataType.RUM_WEBVIEW_ERROR_SAMPLED,
        }));//Get log data from database during initialization
    }

    public synchronized static FTDBCachePolicy get() {
        if (instance == null) {
            instance = new FTDBCachePolicy();
        }
        return instance;
    }

    /**
     * Set db limit
     *
     * @param config
     */
    public void initSDKParams(FTSDKConfig config) {
        this.enableLimitWithDbSize = config.isLimitWithDbSize();
        this.dbLimitSize = config.getDbCacheLimit();
        this.dbCacheDiscard = config.getDbCacheDiscard();
    }

    /**
     * Initialize log configuration parameters
     *
     * @param config
     */
    public void initLogParam(FTLoggerConfig config) {
        this.logCacheDiscardStrategy = config.getLogCacheDiscardStrategy();
        this.logLimitCount = config.getLogCacheLimitCount();
    }

    /**
     * Initialize RUM configuration parameters
     *
     * @param config
     */
    public void initRUMParam(FTRUMConfig config) {
        this.rumCacheDiscardStrategy = config.getRumCacheDiscardStrategy();
        this.rumLimitCount = config.getRumCacheLimitCount();
    }

    /**
     * Get limit count
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
     * Operate Log log count
     *
     * @param optCount Number of data written
     */
    public void optLogCount(int optCount) {
        if (enableLimitWithDbSize) return;
        logCount.addAndGet(optCount);
    }

    /**
     * Operate RUM count
     *
     * @param optCount Number of data written
     */
    public void optRUMCount(int optCount) {
        if (enableLimitWithDbSize) return;
        rumCount.addAndGet(optCount);
    }

    /**
     * Set current db cache file size
     *
     * @param currentDbSize
     */
    public void setCurrentDBSize(long currentDbSize) {
        this.currentDbSize.set(currentDbSize);
    }

    /**
     * Whether db cache limit is reached
     *
     * @return
     */
    public boolean isReachDbLimit() {
        return currentDbSize.get() >= dbLimitSize;
    }

    /**
     * Whether half of db cache limit is reached
     *
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
     * If the number of data written is greater than half of the total limit
     *
     * @return Whether half is reached
     */
    private boolean reachLogHalfLimit() {
        return logLimitCount > 0 && logCount.get() > logLimitCount / 2;
    }

    /**
     * If the number of data written is greater than half of the total limit
     *
     * @return
     */
    private boolean reachRumHalfLimit() {
        return rumLimitCount > 0 && rumCount.get() > rumLimitCount / 2;
    }

    /**
     * Execute log log database cache policy
     *
     * @return 0-means data can be inserted, n means old data needs to be deleted, -n means how much data to discard
     */
    public int optLogCachePolicy(int limit) {
        if (enableLimitWithDbSize) {
            if (isReachDbLimit()) {
                switch (dbCacheDiscard) {
                    case DISCARD:
                        return -limit;
                    case DISCARD_OLDEST:
                        return limit;
                }
            }
            return 0;
        }

        int status = 0;
        int currentLogCount = logCount.get();
        if (currentLogCount >= logLimitCount) {//When data volume is greater than the configured maximum database storage capacity, execute discard strategy
            if (logCacheDiscardStrategy == LogCacheDiscard.DISCARD) {//Directly discard data
                status = -limit;
            } else if (logCacheDiscardStrategy == LogCacheDiscard.DISCARD_OLDEST) {//Discard the first few pieces of data in the database
                FTDBManager.get().deleteOldestData(DataType.LOG, limit);
                logCount.set(FTDBManager.get().queryTotalCount(DataType.LOG));
                status = limit;
            }
        } else {
            if (currentLogCount + limit >= logLimitCount) {
                int needInsert = logLimitCount - currentLogCount;
                int needRemove = limit - needInsert;
                if (logCacheDiscardStrategy == LogCacheDiscard.DISCARD) {//Directly discard data
                    status = -needRemove;
                } else if (logCacheDiscardStrategy == LogCacheDiscard.DISCARD_OLDEST) {//Discard the first few pieces of data in the database
                    FTDBManager.get().deleteOldestData(DataType.LOG, needRemove);
                    logCount.set(FTDBManager.get().queryTotalCount(DataType.LOG));
                    status = needRemove;
                }
            }
        }
        return status;
    }

    /**
     * ExecuteRUM discard strategy
     *
     * @param limit
     * @return @return -1-means directly discard, 0-means data can be inserted, 1-means discard and delete old data
     */
    public int optRUMCachePolicy(int limit) {
        if (enableLimitWithDbSize) {
            if (isReachDbLimit()) {
                switch (dbCacheDiscard) {
                    case DISCARD:
                        return -1;
                    case DISCARD_OLDEST:
                        return 1;
                }
            }
            return 0;
        }

        int status = 0;
        int currentRUMCount = rumCount.get();
        if (currentRUMCount >= rumLimitCount) {//When data volume is greater than the configured maximum database storage capacity, execute discard strategy
            if (rumCacheDiscardStrategy == RUMCacheDiscard.DISCARD) {//Directly discard data
                status = -1;
            } else if (rumCacheDiscardStrategy == RUMCacheDiscard.DISCARD_OLDEST) {//Discard the first few pieces of data in the database
                FTDBManager.get().deleteOldestData(new DataType[]{
                        DataType.RUM_APP,
                        DataType.RUM_WEBVIEW,
                        DataType.RUM_APP_ERROR_SAMPLED,
                        DataType.RUM_WEBVIEW_ERROR_SAMPLED
                }, limit);
                rumCount.set(FTDBManager.get().queryTotalCount(new DataType[]{
                        DataType.RUM_APP,
                        DataType.RUM_WEBVIEW,
                        DataType.RUM_APP_ERROR_SAMPLED,
                        DataType.RUM_WEBVIEW_ERROR_SAMPLED
                }));
                status = 1;
            }
        } else {
            status = 0;
        }
        return status;
    }

    /**
     * Whether db limit is enabled
     *
     * @return
     */
    boolean isEnableLimitWithDbSize() {
        return enableLimitWithDbSize;
    }

    /**
     * db cache discard strategy
     *
     * @return
     */
    DBCacheDiscard getDbCacheDiscard() {
        return dbCacheDiscard;
    }
}
