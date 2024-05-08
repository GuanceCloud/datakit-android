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
 * 打印输出观测云 Studio 可以查看的日志
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
     * @param config {@link  FTLoggerConfig} 初始化 config
     */
    void init(FTLoggerConfig config) {
        this.config = config;
    }

    /**
     * 将单条日志数据存入本地同步
     *
     * @param content 日志内容
     * @param status  日志等级
     */
    public void logBackground(String content, Status status) {
        logBackground(content, status.name, null, false);

    }

    /**
     * 将单条日志数据存入本地同步
     *
     * @param content   日志内容
     * @param status    日志等级
     * @param isSilence 是否静默
     */
    public void logBackground(String content, Status status, boolean isSilence) {
        logBackground(content, status.name, null, isSilence);
    }

    /**
     * 将单条日志数据存入本地同步
     *
     * @param content  日志内容
     * @param status   日志等级
     * @param property 附加属性
     */
    public void logBackground(String content, Status status, HashMap<String, Object> property) {
        logBackground(content, status.name, property, false);
    }

    /**
     * 将单条日志数据存入本地同步
     *
     * @param content   日志内容
     * @param status    日志等级
     * @param property  附加属性
     * @param isSilence 是否静默
     */
    public void logBackground(String content, Status status, HashMap<String, Object> property, boolean isSilence) {
        logBackground(content, status.name, property, isSilence);
    }

    public void logBackground(String content, String status) {
        logBackground(content, status, null);
    }

    /**
     * 将单条日志数据存入本地同步
     *
     * @param content 日志内容
     * @param status  日志等级
     */
    public void logBackground(String content, String status, boolean isSilence) {
        logBackground(content, status, null, isSilence);
    }

    public void logBackground(String content, String status, HashMap<String, Object> property) {
        logBackground(content, status, property, false);
    }

    /**
     * 将单条日志数据存入本地同步
     *
     * @param content   日志内容
     * @param status    自定义状态
     * @param property  附加属性
     * @param isSilence 是否静默
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
     * 将多条日志数据存入本地同步(异步)
     *
     * @param logDataList {@link LogData} 列表
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
     * 检验 {@link FTLoggerConfig} 是否初始化
     *
     * @return true 为已初始化，反之为 false
     */
    private boolean checkConfig() {
        if (config == null) {
            LogUtils.e(TAG, "使用 FTLogger，需要初始化 FTSdk.initLogWithConfig(FTLoggerConfig ftSdkConfig))");
            return false;
        }
        return true;
    }


}
