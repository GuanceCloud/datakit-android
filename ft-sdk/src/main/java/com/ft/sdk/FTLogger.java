package com.ft.sdk;

import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.LogData;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;
import java.util.List;

/**
 * Print logs that can be viewed in Guanceyun Studio
 *
 * @author Brandon
 */
public class FTLogger {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTLogger";

    private FTLogger() {

    }

    private static class SingletonHolder {
        private static final FTLogger INSTANCE = new FTLogger();
    }

    public static FTLogger getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private FTLoggerConfig config;

    /**
     * @param config {@link  FTLoggerConfig} initialization config
     */
    void init(FTLoggerConfig config) {
        this.config = config;
    }

    /**
     * Store a single log entry locally and synchronize
     *
     * @param content Log content, maximum limit {@link com.ft.sdk.garble.bean.BaseContentBean#LIMIT_SIZE}
     * @param status  Log level
     */
    public void logBackground(String content, Status status) {
        logBackground(content, status.name, null, false);

    }

    /**
     * Store a single log entry locally and synchronize
     *
     * @param content   Log content, maximum limit {@link com.ft.sdk.garble.bean.BaseContentBean#LIMIT_SIZE}
     * @param status    Log level
     * @param isSilence Whether silent
     */
    public void logBackground(String content, Status status, boolean isSilence) {
        logBackground(content, status.name, null, isSilence);
    }

    /**
     * Store a single log entry locally and synchronize
     *
     * @param content  Log content, maximum limit {@link com.ft.sdk.garble.bean.BaseContentBean#LIMIT_SIZE}
     * @param status   Log level
     * @param property Additional properties
     */
    public void logBackground(String content, Status status, HashMap<String, Object> property) {
        logBackground(content, status.name, property, false);
    }

    /**
     * Store a single log entry locally and synchronize
     *
     * @param content   Log content, maximum limit {@link com.ft.sdk.garble.bean.BaseContentBean#LIMIT_SIZE}
     * @param status    Log level
     * @param property  Additional properties
     * @param isSilence Whether silent
     */
    public void logBackground(String content, Status status, HashMap<String, Object> property, boolean isSilence) {
        logBackground(content, status.name, property, isSilence);
    }

    public void logBackground(String content, String status) {
        logBackground(content, status, null);
    }

    /**
     * Store a single log entry locally and synchronize
     *
     * @param content Log content, maximum limit {@link com.ft.sdk.garble.bean.BaseContentBean#LIMIT_SIZE}
     * @param status   Log level
     */
    public void logBackground(String content, String status, boolean isSilence) {
        logBackground(content, status, null, isSilence);
    }

    public void logBackground(String content, String status, HashMap<String, Object> property) {
        logBackground(content, status, property, false);
    }

    /**
     * Store a single log entry locally and synchronize
     *
     * @param content   Log content, maximum limit {@link com.ft.sdk.garble.bean.BaseContentBean#LIMIT_SIZE}
     * @param status    Custom status
     * @param property  Additional properties
     * @param isSilence Whether silent
     */
    public void logBackground(String content, String status, HashMap<String, Object> property, boolean isSilence) {

        if (!checkConfig()) return;
        if (!config.isEnableCustomLog()) {
            return;
        }
        if (config.isPrintCustomLogToConsole()) {
            String propertyString = property == null ? "" : "," + property;
            String message = "[" + status.toUpperCase() + "]" + content + propertyString;

            boolean contain = false;
            for (Status s : Status.values()) {
                if (s.name.equals(status.toLowerCase())) {
                    contain = true;
                    break;
                }
            }

            if (contain) {
                switch (Status.valueOf(status.toUpperCase())) {
                    case INFO:
                        LogUtils.i(Constants.LOG_TAG_PREFIX, message, true);
                        break;
                    case WARNING:
                        LogUtils.w(Constants.LOG_TAG_PREFIX, message, true);
                        break;
                    case ERROR:
                    case CRITICAL:
                        LogUtils.e(Constants.LOG_TAG_PREFIX, message, true);
                        break;
                    case OK:
                        LogUtils.v(Constants.LOG_TAG_PREFIX, message, true);
                        break;
                }
            } else {
                LogUtils.i(Constants.LOG_TAG_PREFIX, message, true);
            }


        }

        LogBean logBean = new LogBean(content, Utils.getCurrentNanoTime());
        logBean.setServiceName(config.getServiceName());
        if (property != null) {
            logBean.getProperty().putAll(property);
        }
        logBean.setStatus(status);
        if (config.checkLogLevel(status)) {
            FTTrackInner.getInstance().logBackground(logBean, isSilence);
        }

    }

    /**
     * Store multiple log entries locally and synchronize (asynchronously)
     *
     * @param logDataList {@link LogData} list
     */
    public void logBackground(List<LogData> logDataList) {
        if (!checkConfig()) return;
        if (logDataList == null || (!config.isEnableCustomLog())) {
            return;
        }
        for (LogData logData : logDataList) {
            LogBean logBean = new LogBean(logData.getContent(),
                    Utils.getCurrentNanoTime());
            logBean.setServiceName(config.getServiceName());
            logBean.setStatus(logData.getStatus());
            if (config.checkLogLevel(logBean.getStatus())) {
                TrackLogManager.get().trackLog(logBean, false);
            }
        }
    }

    /**
     * Check whether {@link FTLoggerConfig} is initialized
     *
     * @return true if initialized, false otherwise
     */
    private boolean checkConfig() {
        if (config == null) {
            LogUtils.e(TAG, "Use FTLogger, need to initialize FTSdk.initLogWithConfig(FTLoggerConfig ftSdkConfig))");
            return false;
        }
        return true;
    }


}
