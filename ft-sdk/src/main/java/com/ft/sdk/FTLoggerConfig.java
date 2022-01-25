package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FTLoggerConfig {
    private float samplingRate = 1;
    private boolean enableLinkRumData = false;
    private boolean enableConsoleLog = false;
    private boolean enableCustomLog = false;
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

    public FTLoggerConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    public boolean isEnableLinkRumData() {
        return enableLinkRumData;
    }

    public FTLoggerConfig setEnableLinkRumData(boolean enableLinkRumData) {
        this.enableLinkRumData = enableLinkRumData;
        return this;
    }

    public boolean isEnableConsoleLog() {
        return enableConsoleLog;
    }

    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog) {
        this.enableConsoleLog = enableConsoleLog;
        return this;
    }

    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog, String prefix) {
        this.enableConsoleLog = enableConsoleLog;
        this.logPrefix = prefix;
        return this;
    }

    public boolean isEnableCustomLog() {
        return enableCustomLog;
    }

    public FTLoggerConfig setEnableCustomLog(boolean enableCustomLog) {
        this.enableCustomLog = enableCustomLog;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public FTLoggerConfig setServiceName(String serviceName) {
        if (serviceName != null) {
            this.serviceName = serviceName;
        }
        return this;
    }


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

    public String getLogPrefix() {
        return logPrefix;
    }

    public List<Status> getLogLevelFilters() {
        return logLevelFilters;
    }

    public FTLoggerConfig setLogLevelFilters(Status[] logLevelFilters) {
        this.logLevelFilters = Arrays.asList(logLevelFilters);
        return this;
    }

    public boolean checkPrefix(String message) {
        return logPrefix == null
                || message.startsWith(logPrefix);
    }


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
