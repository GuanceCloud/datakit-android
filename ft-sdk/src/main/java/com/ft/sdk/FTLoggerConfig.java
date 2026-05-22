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
     * @return log sampling rate in the range [0, 1]
     */
    public float getSamplingRate() {
        return samplingRate;
    }

    /**
     * Set sampling rate
     *
     * @param samplingRate log sampling rate in the range [0, 1]
     * @return this config for chaining
     */
    public FTLoggerConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    /**
     * Whether to associate with RUM data
     *
     * @return true when logs should include RUM linkage data
     */
    public boolean isEnableLinkRumData() {
        return enableLinkRumData;
    }

    /**
     * Set whether to associate with RUM data
     *
     * @param enableLinkRumData true to attach RUM linkage data to logs
     * @return this config for chaining
     */
    public FTLoggerConfig setEnableLinkRumData(boolean enableLinkRumData) {
        this.enableLinkRumData = enableLinkRumData;
        return this;
    }

    /**
     * Whether to enable control log capture
     *
     * @return true when console log capture is enabled
     */
    public boolean isEnableConsoleLog() {
        return enableConsoleLog;
    }

    /**
     * Set whether to enable control log capture
     *
     * @param enableConsoleLog true to capture console logs
     * @return this config for chaining
     */
    public FTLoggerConfig setEnableConsoleLog(boolean enableConsoleLog) {
        this.enableConsoleLog = enableConsoleLog;
        return this;
    }

    /**
     * @param enableConsoleLog Whether to enable control log capture
     * @param prefix           Log filter prefix
     * @return this config for chaining
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

    /**
     * Sets whether custom logs submitted through {@link FTLogger#logBackground(String, Status)} are collected.
     *
     * @param enableCustomLog true to enable custom log collection
     * @return this config for chaining
     */
    public FTLoggerConfig setEnableCustomLog(boolean enableCustomLog) {
        this.enableCustomLog = enableCustomLog;
        return this;
    }

    /**
     * Get service name
     *
     * @return service name attached to log data
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
     * @return log cache discard strategy
     */
    public LogCacheDiscard getLogCacheDiscardStrategy() {
        return logCacheDiscardStrategy;
    }

    /**
     * Set database data storage strategy
     *
     * @param logCacheDiscardStrategy strategy used when the log cache limit is reached
     * @return this config for chaining
     */
    public FTLoggerConfig setLogCacheDiscardStrategy(LogCacheDiscard logCacheDiscardStrategy) {
        this.logCacheDiscardStrategy = logCacheDiscardStrategy;
        return this;
    }


    /**
     * Set log level filter
     *
     * @param logLevelFilters allowed log statuses
     * @return this config for chaining
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
     * @param logLevelFilters allowed custom log status strings
     * @return this config for chaining
     */
    public FTLoggerConfig setLogLevelFilters(String[] logLevelFilters) {
        this.logLevelFilters = Arrays.asList(logLevelFilters);
        return this;
    }

    /**
     * Whether to enable custom log printing
     *
     * @return true when custom logs should also be printed to console
     */
    public boolean isPrintCustomLogToConsole() {
        return !TrackLog.isSetInnerLogHandler() && printCustomLogToConsole;
    }

    /**
     * Set whether to print custom logs to console
     *
     * @param printCustomLogToConsole true to print custom logs to console
     */
    public FTLoggerConfig setPrintCustomLogToConsole(boolean printCustomLogToConsole) {
        this.printCustomLogToConsole = printCustomLogToConsole;
        return this;
    }

    /**
     * Set the maximum number of log entries, minimum not less than 1000
     *
     * @param count maximum log cache row count
     * @return this config for chaining
     */
    public FTLoggerConfig setLogCacheLimitCount(int count) {
        this.logCacheLimitCount = Math.max(Constants.MINI_DB_LOG_CACHE_NUM, count);
//        this.logCacheLimitCount = count;
        return this;
    }


    /**
     * Get the maximum number of log entries, minimum not less than 1000
     *
     * @return maximum log cache row count
     */
    public int getLogCacheLimitCount() {
        return logCacheLimitCount;
    }

    /**
     * Check if prefix is set
     *
     * @param message Console content
     * @return true when the message matches the configured prefix
     */
    public boolean checkPrefix(String message) {
        return logPrefix == null
                || message.startsWith(logPrefix);
    }


    /**
     * Check if log level is set
     *
     * @param status log status to check
     * @return Whether to set
     */
    public boolean checkLogLevel(String status) {
        return (logLevelFilters == null
                || logLevelFilters.isEmpty())
                || logLevelFilters.contains(status);
    }

    /**
     * Returns the configured log status filters, or null when all statuses are allowed.
     */
    public List<String> getLogLevelFilters() {
        return logLevelFilters;
    }

    /**
     * Adds a global attribute to every log item collected with this configuration.
     *
     * @param key   attribute key
     * @param value attribute value
     * @return this config for chaining
     */
    public FTLoggerConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    /**
     * Returns global log attributes configured on this object.
     */
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
