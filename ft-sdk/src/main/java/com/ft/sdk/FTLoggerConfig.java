package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.TrackLog;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * 配置日志 {@link FTLogger} 输出使用的参数配置
 *
 * @author Brandon
 */
public class FTLoggerConfig {
    /**
     * 采样率，[0,1]
     */
    private float samplingRate = 1;
    /**
     * 是否与 RUM 数据关联
     */
    private boolean enableLinkRumData = false;
    /**
     * 是否开启控制台日志输出
     */
    private boolean enableConsoleLog = false;
    /**
     * 是否开启自定义日志
     */
    private boolean enableCustomLog = false;

    /**
     * 服务名称 {@link Constants#KEY_SERVICE },默认为 {@link Constants#DEFAULT_SERVICE_NAME}
     */
    private String serviceName = Constants.DEFAULT_SERVICE_NAME;
    /**
     * log 过滤前缀
     */
    private String logPrefix = "";

    private int logCacheLimitCount = Constants.DEFAULT_DB_LOG_CACHE_NUM;
    /**
     * log 日志等级过滤
     */
    private List<Status> logLevelFilters;

    /**
     * 设置全局 tag
     */
    private final HashMap<String, Object> globalContext = new HashMap<>();

    /**
     * 日志数据数据库存储策略
     */
    private LogCacheDiscard logCacheDiscardStrategy = LogCacheDiscard.DISCARD;

    /**
     * 自定义日志打印配置
     */
    private boolean printCustomLogToConsole = false;

    /**
     * 获取采样率
     *
     * @return
     */
    public float getSamplingRate() {
        return samplingRate;
    }

    /**
     * 设置采样率
     *
     * @param samplingRate
     * @return
     */
    public FTLoggerConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    /**
     * 是否与 RUM 数据关联
     *
     * @return
     */
    public boolean isEnableLinkRumData() {
        return enableLinkRumData;
    }

    /**
     * 设置是否与 RUM 数据关联
     *
     * @param enableLinkRumData
     * @return
     */
    public FTLoggerConfig setEnableLinkRumData(boolean enableLinkRumData) {
        this.enableLinkRumData = enableLinkRumData;
        return this;
    }

    /**
     * 是否开启控制日志抓取
     *
     * @return
     */
    public boolean isEnableConsoleLog() {
        return  enableConsoleLog;
    }

    /**
     * 设置是否开启控制日志抓取
     *
     * @param enableConsoleLog
     * @return
     */
    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog) {
        this.enableConsoleLog = enableConsoleLog;
        return this;
    }

    /**
     * @param enableConsoleLog 是否开启控制日志抓取
     * @param prefix           日志过滤前缀
     * @return
     */
    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog, String prefix) {
        this.enableConsoleLog = enableConsoleLog;
        this.logPrefix = prefix;
        return this;
    }

    /**
     * 判断是否已经开启自定义日志
     *
     * @return true 开启，false 未开启
     */
    public boolean isEnableCustomLog() {
        return enableCustomLog;
    }

    public FTLoggerConfig setEnableCustomLog(boolean enableCustomLog) {
        this.enableCustomLog = enableCustomLog;
        return this;
    }

    /**
     * 获取服务名称
     *
     * @return
     */
    public String getServiceName() {
        return serviceName;
    }

    void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    /**
     * 获取日志缓存策略
     *
     * @return
     */
    public LogCacheDiscard getLogCacheDiscardStrategy() {
        return logCacheDiscardStrategy;
    }

    /**
     * 设置数据库数据存储策略
     *
     * @param logCacheDiscardStrategy
     * @return
     */
    public FTLoggerConfig setLogCacheDiscardStrategy(LogCacheDiscard logCacheDiscardStrategy) {
        this.logCacheDiscardStrategy = logCacheDiscardStrategy;
        return this;
    }


    /**
     * 设置日志等级过滤
     *
     * @param logLevelFilters
     * @return
     */
    public FTLoggerConfig setLogLevelFilters(Status[] logLevelFilters) {
        this.logLevelFilters = Arrays.asList(logLevelFilters);
        return this;
    }

    /**
     * 是否开启自定义日志打印
     *
     * @return
     */
    public boolean isPrintCustomLogToConsole() {
        return !TrackLog.isSetInnerLogHandler() && printCustomLogToConsole;
    }

    /**
     * 设置是否将自定义日志打印至 console
     *
     * @param printCustomLogToConsole
     */
    public FTLoggerConfig setPrintCustomLogToConsole(boolean printCustomLogToConsole) {
        this.printCustomLogToConsole = printCustomLogToConsole;
        return this;
    }

    /**
     * 设置日志最大条目数量,最小不小于 1000
     *
     * @param count
     * @return
     */
    public FTLoggerConfig setLogCacheLimitCount(int count) {
        this.logCacheLimitCount = Math.max(1000, count);
        return this;
    }


    /**
     * 获取最大日志条目数量限制
     * @return
     */
    public int getLogCacheLimitCount() {
        return logCacheLimitCount;
    }

    /**
     * 检验是否包含 prefix 设置
     *
     * @param message 控制台内容
     * @return
     */
    public boolean checkPrefix(String message) {
        return logPrefix == null
                || message.startsWith(logPrefix);
    }


    /**
     * 检验是否包含日志等级设置
     *
     * @param status
     * @return 是否设置
     */
    public boolean checkLogLevel(Status status) {
        return (logLevelFilters == null
                || logLevelFilters.size() == 0)
                || logLevelFilters.contains(status);
    }

    public FTLoggerConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    public HashMap<String, Object> getGlobalContext() {
        return globalContext;
    }
}
