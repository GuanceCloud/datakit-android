package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * @author Brandon
 */
public class FTLoggerConfig {
    private float samplingRate = 1;
    private boolean enableLinkRumData = false;
    private boolean enableConsoleLog = false;
    private boolean enableCustomLog = false;

    /**
     * 服务名称 {@link Constants#KEY_SERVICE },默认为 {@link Constants#DEFAULT_LOG_SERVICE_NAME}
     */
    private String serviceName = Constants.DEFAULT_LOG_SERVICE_NAME;
    private String logPrefix = "";
    private List<Status> logLevelFilters;

    //设置全局 tag
    private final HashMap<String, Object> globalContext = new HashMap<>();

    //日志数据数据库存储策略
    private LogCacheDiscard logCacheDiscardStrategy = LogCacheDiscard.DISCARD;

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
     * 是否关联 RUM 数据
     *
     * @return
     */
    public boolean isEnableLinkRumData() {
        return enableLinkRumData;
    }

    /**
     * @param enableLinkRumData
     * @return
     */
    public FTLoggerConfig setEnableLinkRumData(boolean enableLinkRumData) {
        this.enableLinkRumData = enableLinkRumData;
        return this;
    }

    /**
     * @return
     */
    public boolean isEnableConsoleLog() {
        return enableConsoleLog;
    }

    /**
     * @param enableConsoleLog
     * @return
     */
    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog) {
        this.enableConsoleLog = enableConsoleLog;
        return this;
    }

    /**
     * @param enableConsoleLog
     * @param prefix           日志过滤前缀
     * @return
     */
    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog, String prefix) {
        this.enableConsoleLog = enableConsoleLog;
        this.logPrefix = prefix;
        return this;
    }

    /**
     * 判断是否已已经开启自定义
     *
     * @return
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

    public FTLoggerConfig setServiceName(String serviceName) {
        if (serviceName != null) {
            this.serviceName = serviceName;
        }
        return this;
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
     * 获取日志
     *
     * @return
     */
    public String getLogPrefix() {
        return logPrefix;
    }

    /**
     * 过去日志过滤规则
     *
     * @return
     */
    public List<Status> getLogLevelFilters() {
        return logLevelFilters;
    }

    public FTLoggerConfig setLogLevelFilters(Status[] logLevelFilters) {
        this.logLevelFilters = Arrays.asList(logLevelFilters);
        return this;
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

    /**
     * @return
     */
    public HashMap<String, Object> getGlobalContext() {
        return globalContext;
    }
}
