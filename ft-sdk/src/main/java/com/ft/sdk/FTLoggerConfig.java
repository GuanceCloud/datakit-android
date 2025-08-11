package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.TrackLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Configure the parameters used for log output by {@link FTLogger}
 *
 * @author Brandon
 */
public class FTLoggerConfig {
    /**
     * Sampling rate, [0,1]
     */
    private float samplingRate = 1;
    /**
     * Whether to associate with RUM data
     */
    private boolean enableLinkRumData = false;
    /**
     * Whether to enable console log output
     */
    private boolean enableConsoleLog = false;
    /**
     * Whether to enable custom log
     */
    private boolean enableCustomLog = false;

    /**
     * Service name {@link Constants#KEY_SERVICE }, default is {@link Constants#DEFAULT_SERVICE_NAME}
     */
    private String serviceName = Constants.DEFAULT_SERVICE_NAME;
    /**
     * log filter prefix
     */
    private String logPrefix = "";

    private int logCacheLimitCount = Constants.DEFAULT_DB_LOG_CACHE_NUM;
    /**
     * log level filter
     */
    private List<String> logLevelFilters;

    /**
     * Set global tag
     */
    private final HashMap<String, Object> globalContext = new HashMap<>();

    /**
     * Log data database storage strategy
     */
    private LogCacheDiscard logCacheDiscardStrategy = LogCacheDiscard.DISCARD;

    /**
     * Custom log print configuration
     */
    private boolean printCustomLogToConsole = false;

    /**
     * Get sampling rate
     *
     * @return
     */
    public float getSamplingRate() {
        return samplingRate;
    }

    /**
     * Set sampling rate
     *
     * @param samplingRate
     * @return
     */
    public FTLoggerConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    /**
     * Whether to associate with RUM data
     *
     * @return
     */
    public boolean isEnableLinkRumData() {
        return enableLinkRumData;
    }

    /**
     * Set whether to associate with RUM data
     *
     * @param enableLinkRumData
     * @return
     */
    public FTLoggerConfig setEnableLinkRumData(boolean enableLinkRumData) {
        this.enableLinkRumData = enableLinkRumData;
        return this;
    }

    /**
     * Whether to enable control log capture
     *
     * @return
     */
    public boolean isEnableConsoleLog() {
        return enableConsoleLog;
    }

    /**
     * Set whether to enable control log capture
     *
     * @param enableConsoleLog
     * @return
     */
    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog) {
        this.enableConsoleLog = enableConsoleLog;
        return this;
    }

    /**
     * @param enableConsoleLog Whether to enable control log capture
     * @param prefix           Log filter prefix
     * @return
     */
    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog, String prefix) {
        this.enableConsoleLog = enableConsoleLog;
        this.logPrefix = prefix;
        return this;
    }

    /**
     * Determine if custom logging has been enabled
     *
     * @return true enabled, false not enabled
     */
    public boolean isEnableCustomLog() {
        return enableCustomLog;
    }

    public FTLoggerConfig setEnableCustomLog(boolean enableCustomLog) {
        this.enableCustomLog = enableCustomLog;
        return this;
    }

    /**
     * Get service name
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
     * Get log cache strategy
     *
     * @return
     */
    public LogCacheDiscard getLogCacheDiscardStrategy() {
        return logCacheDiscardStrategy;
    }

    /**
     * Set database data storage strategy
     *
     * @param logCacheDiscardStrategy
     * @return
     */
    public FTLoggerConfig setLogCacheDiscardStrategy(LogCacheDiscard logCacheDiscardStrategy) {
        this.logCacheDiscardStrategy = logCacheDiscardStrategy;
        return this;
    }


    /**
     * Set log level filter
     *
     * @param logLevelFilters
     * @return
     */
    public FTLoggerConfig setLogLevelFilters(Status[] logLevelFilters) {
        List<String> array = new ArrayList<>();
        for (Status s : logLevelFilters) {
            array.add(s.name);
        }
        this.logLevelFilters = array;
        return this;
    }

    /**
     * Set log level filter
     *
     * @param logLevelFilters
     * @return
     */
    public FTLoggerConfig setLogLevelFilters(String[] logLevelFilters) {
        this.logLevelFilters = Arrays.asList(logLevelFilters);
        return this;
    }

    /**
     * Whether to enable custom log printing
     *
     * @return
     */
    public boolean isPrintCustomLogToConsole() {
        return !TrackLog.isSetInnerLogHandler() && printCustomLogToConsole;
    }

    /**
     * Set whether to print custom logs to console
     *
     * @param printCustomLogToConsole
     */
    public FTLoggerConfig setPrintCustomLogToConsole(boolean printCustomLogToConsole) {
        this.printCustomLogToConsole = printCustomLogToConsole;
        return this;
    }

    /**
     * Set the maximum number of log entries, minimum not less than 1000
     *
     * @param count
     * @return
     */
    public FTLoggerConfig setLogCacheLimitCount(int count) {
        this.logCacheLimitCount = Math.max(Constants.MINI_DB_LOG_CACHE_NUM, count);
//        this.logCacheLimitCount = count;
        return this;
    }


    /**
     * Get the maximum number of log entries, minimum not less than 1000
     *
     * @return
     */
    public int getLogCacheLimitCount() {
        return logCacheLimitCount;
    }

    /**
     * Check if prefix is set
     *
     * @param message Console content
     * @return
     */
    public boolean checkPrefix(String message) {
        return logPrefix == null
                || message.startsWith(logPrefix);
    }


    /**
     * Check if log level is set
     *
     * @param status
     * @return Whether to set
     */
    public boolean checkLogLevel(String status) {
        return (logLevelFilters == null
                || logLevelFilters.isEmpty())
                || logLevelFilters.contains(status);
    }

    public List<String> getLogLevelFilters() {
        return logLevelFilters;
    }

    public FTLoggerConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    public HashMap<String, Object> getGlobalContext() {
        return globalContext;
    }

    @Override
    public String toString() {
        return "FTLoggerConfig{" +
                "samplingRate=" + samplingRate +
                ", enableLinkRumData=" + enableLinkRumData +
                ", enableConsoleLog=" + enableConsoleLog +
                ", enableCustomLog=" + enableCustomLog +
                ", serviceName='" + serviceName + '\'' +
                ", logPrefix='" + logPrefix + '\'' +
                ", logCacheLimitCount=" + logCacheLimitCount +
                ", logLevelFilters=" + logLevelFilters +
                ", globalContext=" + globalContext +
                ", logCacheDiscardStrategy=" + logCacheDiscardStrategy +
                ", printCustomLogToConsole=" + printCustomLogToConsole +
                '}';
    }
}
