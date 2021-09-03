package com.ft.sdk;

import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.LogData;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Utils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class FTLogger {

    private static final String TAG = "FTLogger";

    private FTLogger() {

    }

    private static class SingletonHolder {
        private static final FTLogger INSTANCE = new FTLogger();
    }

    public static FTLogger getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private FTLoggerConfig config;

    void init(FTLoggerConfig config) {
        this.config = config;
    }


    /**
     * 将单条日志数据存入本地同步
     *
     * @param content
     * @param status
     */
    public void logBackground(String content, Status status) {

        checkConfig();

        if (!config.isEnableCustomLog()) {
            return;
        }
        LogBean logBean = new LogBean(Utils.translateFieldValue(content), Utils.getCurrentNanoTime());
        logBean.setServiceName(config.getServiceName());
        logBean.setStatus(status);
        logBean.setEnv(FTSdk.get().getBaseConfig().getEnv());
        if (config.checkLogBeanWillPrint(logBean)) {
            FTTrackInner.getInstance().logBackground(logBean);
        }

    }

    /**
     * 将多条日志数据存入本地同步(异步)
     *
     * @param logDataList
     */
    public void logBackground(List<LogData> logDataList) {
        checkConfig();
        if (logDataList == null || (!config.isEnableCustomLog())) {
            return;
        }
        List<BaseContentBean> logBeans = new ArrayList<>();
        for (LogData logData : logDataList) {
            LogBean logBean = new LogBean(Utils.translateFieldValue(logData.getContent()),
                    Utils.getCurrentNanoTime());
            logBean.setServiceName(config.getServiceName());
            logBean.setStatus(logData.getStatus());
            logBean.setEnv(FTSdk.get().getBaseConfig().getEnv());
            if (config.checkLogBeanWillPrint(logBean)) {
                logBeans.add(logBean);
            }
        }
        FTTrackInner.getInstance().batchLogBeanBackground(logBeans);
    }

    void checkConfig() {
        if (config == null) {
            throw new InvalidParameterException("使用 FTLogger，需要初始化 FTLoggerConfigManager.get().initWithConfig(FTLoggerConfig ftSdkConfig))");
        }
    }


}
