package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.LogCacheDiscard;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;

/**
 * author: huangDianHua
 * time: 2020/8/4 15:41:54
 * description: 日志数据数据库缓存丢弃策略
 */
public class FTDBCachePolicy {
    private volatile static FTDBCachePolicy instance;
    private volatile int count = 0;
    private LogCacheDiscard logCacheDiscardStrategy = LogCacheDiscard.DISCARD;


    private FTDBCachePolicy(){
        count = FTDBManager.get().queryTotalCount(OP.LOG);//初始化时从数据库中获取日志数据
    }
    public synchronized static FTDBCachePolicy get(){
        if(instance == null){
            instance = new FTDBCachePolicy();
        }
        return instance;
    }

    public void initParam(FTSDKConfig ftsdkConfig){
        this.logCacheDiscardStrategy = ftsdkConfig.getLogCacheDiscardStrategy();
    }

    /**
     * 操作 Log 日志计数
     * @param optCount
     */
    public synchronized void optCount(int optCount) {
        count += optCount;
    }

    /**
     * 执行 log 日志数据库缓存策略
     * @return true-需要继续执行数据库数据插入；false-不需要再执行数据库数据插入
     */
    public boolean optLogCachePolicy(int limit){
        if(count >= Constants.MAX_DB_CACHE_NUM) {//当数据量大于配置的数据库最大存储量时，执行丢弃策略
            if(logCacheDiscardStrategy == LogCacheDiscard.DISCARD){//直接丢弃数据
                return false;
            }else if(logCacheDiscardStrategy == LogCacheDiscard.DISCARD_OLDEST){//丢弃数据库中的前几条数据
                FTDBManager.get().deleteOldestData(OP.LOG,limit);
                count = FTDBManager.get().queryTotalCount(OP.LOG);
                return true;
            }
        }else{
            optCount(limit);
        }
        return true;
    }
}
