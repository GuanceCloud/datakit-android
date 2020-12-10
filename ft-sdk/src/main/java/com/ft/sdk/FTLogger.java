package com.ft.sdk;

import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.LogData;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.manager.FTExceptionHandler;
import com.ft.sdk.garble.utils.Utils;

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


    /**
     * 将单条日志数据存入本地同步
     *
     * @param content
     * @param status
     */
    public void logBackground(String content, Status status) {
        LogBean logBean = new LogBean(Utils.translateFieldValue(content), Utils.getCurrentNanoTime());
        logBean.setServiceName(FTExceptionHandler.get().getTrackServiceName());
        logBean.setStatus(status);
        logBean.setEnv(FTExceptionHandler.get().getEnv());
        FTTrackInner.getInstance().logBackground(logBean);
    }

    /**
     * 将多条日志数据存入本地同步(异步)
     *
     * @param logDataList
     */
    public void logBackground(List<LogData> logDataList) {
        if (logDataList == null) {
            return;
        }
        List<LogBean> logBeans = new ArrayList<>();
        for (LogData logData : logDataList) {
            LogBean logBean = new LogBean(Utils.translateFieldValue(logData.getContent()), Utils.getCurrentNanoTime());
            logBean.setServiceName(FTExceptionHandler.get().getTrackServiceName());
            logBean.setStatus(logData.getStatus());
            logBean.setEnv(FTExceptionHandler.get().getEnv());
            logBeans.add(logBean);
        }
        FTTrackInner.getInstance().logBackground(logBeans);
    }


}
