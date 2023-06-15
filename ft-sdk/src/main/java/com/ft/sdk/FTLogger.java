package com.ft.sdk;

import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.LogData;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * 打印输出观测云 Studio 可以查看的日志
 *
 * @author Brandon
 *
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
        logBackground(content, status, null);
    }

    /**
     * 将单条日志数据存入本地同步
     *
     * @param content 日志内容
     * @param status  日志等级
     */
    public void logBackground(String content, Status status, HashMap<String, Object> property) {

        if (!checkConfig()) return;
        if (!config.isEnableCustomLog()) {
            return;
        }
        LogBean logBean = new LogBean(Utils.translateFieldValue(content), Utils.getCurrentNanoTime());
        logBean.setServiceName(config.getServiceName());
        if (property != null) {
            logBean.getProperty().putAll(property);
        }
        logBean.setStatus(status);
        if (config.checkLogLevel(status)) {
            FTTrackInner.getInstance().logBackground(logBean);
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
        List<BaseContentBean> logBeans = new ArrayList<>();
        for (LogData logData : logDataList) {
            LogBean logBean = new LogBean(Utils.translateFieldValue(logData.getContent()),
                    Utils.getCurrentNanoTime());
            logBean.setServiceName(config.getServiceName());
            logBean.setStatus(logData.getStatus());
            if (config.checkLogLevel(logBean.getStatus())) {
                logBeans.add(logBean);
            }
        }
        FTTrackInner.getInstance().batchLogBeanBackground(logBeans);
    }

    /**
     * 检验 {@link FTLoggerConfig} 是否初始化
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
