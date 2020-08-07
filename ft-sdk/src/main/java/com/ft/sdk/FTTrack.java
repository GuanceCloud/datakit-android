package com.ft.sdk;

import com.ft.sdk.garble.FTExceptionHandler;
import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.LogData;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.bean.TrackBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-10 20:18
 * Description:
 */
public class FTTrack {
    private static FTTrack instance;

    private FTTrack() {
    }

    public static FTTrack getInstance() {
        if (instance == null) {
            synchronized (FTTrack.class) {
                if (instance == null) {
                    instance = new FTTrack();
                }
            }
        }
        return instance;
    }

    /**
     * 主动埋点
     *
     * @param measurement 埋点事件名称
     * @param tags        埋点数据
     * @param fields      埋点数据
     */
    public void trackBackground(String measurement, JSONObject tags, JSONObject fields) {
        long time = System.currentTimeMillis();
        FTTrackInner.getInstance().track(OP.CSTM, time, measurement, tags, fields);
    }

    /**
     * 主动埋点一条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param measurement 埋点事件名称
     * @param tags        埋点数据
     * @param fields      埋点数据
     * @param callback    上传结果回调
     */
    public void trackImmediate(String measurement, JSONObject tags, JSONObject fields, SyncCallback callback) {
        long time = System.currentTimeMillis();
        TrackBean trackBean = new TrackBean(measurement, tags, fields, time);
        FTTrackInner.getInstance().track(OP.CSTM, Collections.singletonList(trackBean), callback);
    }

    /**
     * 主动埋点多条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param trackBeans 多条埋点数据
     * @param callback   上传结果回调
     */
    public void trackImmediate(List<TrackBean> trackBeans, SyncCallback callback) {
        FTTrackInner.getInstance().track(OP.CSTM, trackBeans, callback);
    }


    /**
     * 将单条日志数据存入本地同步
     *
     * @param content
     * @param status
     */
    public void logBackground(String content, Status status) {
        LogBean logBean = new LogBean(Constants.USER_AGENT, Utils.translateFieldValue(content), System.currentTimeMillis());
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
            LogBean logBean = new LogBean(Constants.USER_AGENT, Utils.translateFieldValue(logData.getContent()), System.currentTimeMillis());
            logBean.setServiceName(FTExceptionHandler.get().getTrackServiceName());
            logBean.setStatus(logData.getStatus());
            logBean.setEnv(FTExceptionHandler.get().getEnv());
            logBeans.add(logBean);
        }
        FTTrackInner.getInstance().logBackground(logBeans);
    }
}
