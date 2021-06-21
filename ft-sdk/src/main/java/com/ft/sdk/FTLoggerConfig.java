package com.ft.sdk;

public class FTLoggerConfig {
    private float samplingRate = 1;
    private boolean enableLinkRumData = false;
    private boolean enableConsoleLog = false;
    private boolean enableCustomLog = false;
    private String serviceName = "";

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
        this.serviceName = serviceName;
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
}
