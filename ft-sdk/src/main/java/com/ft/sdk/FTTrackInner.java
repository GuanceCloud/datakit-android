package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.manager.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * author: huangDianHua
 * time: 2020/8/7 16:37:09
 * description:内部使用的 Track 方法
 */
public class FTTrackInner {
    private final static String TAG = "FTTrackInner";
    private static FTTrackInner instance;

    private FTTrackInner() {
    }

    public static FTTrackInner getInstance() {
        if (instance == null) {
            synchronized (FTTrackInner.class) {
                if (instance == null) {
                    instance = new FTTrackInner();
                }
            }
        }
        return instance;
    }

    /**
     * rum 事件数据
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void rum(long time, String measurement, final JSONObject tags, JSONObject fields) {
        String sessionId = tags.optString(Constants.KEY_RUM_SESSION_ID);
        if (FTRUMGlobalManager.get().checkSessionWillCollect(sessionId)) {
            syncDataBackground(DataType.RUM_APP, time, measurement, tags, fields);
        }
    }

    /**
     * rum 同步 web 数据
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void rumWebView(long time, String measurement, final JSONObject tags, JSONObject fields) {
        syncDataBackground(DataType.RUM_WEBVIEW, time, measurement, tags, fields);

    }

    /**
     * 在子线程中将埋点数据同步（经过数据库）
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void trackBackground(long time, String measurement, final JSONObject tags, JSONObject fields) {
        SyncDataHelper.addMonitorData(tags, fields);
        syncDataBackground(DataType.TRACK, time, measurement, tags, fields);
    }

    private void syncDataBackground(DataType dataType, long time,
                                    String measurement, final JSONObject tags, JSONObject fields) {
        DataUploaderThreadPool.get().execute(() -> {
            try {

                SyncJsonData recordData = SyncJsonData.getSyncJsonData(dataType,
                        new LineProtocolBean(measurement, tags, fields, time));
                boolean result = FTDBManager.get().insertFtOperation(recordData);
                LogUtils.d(TAG, "trackBackground:" + dataType.toString() + ",insert-result=" + result);
                SyncTaskManager.get().executeSyncPoll();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.getMessage());
            }
        });
    }


    /**
     * 在子线程中将埋点数据同步(不经过数据库)
     *
     * @param trackBeans
     * @param callback
     */
    void trackAsync(@NonNull List<LineProtocolBean> trackBeans, AsyncCallback callback) {
        DataUploaderThreadPool.get().execute(() -> {
            List<SyncJsonData> recordDataList = new ArrayList<>();
            for (LineProtocolBean t : trackBeans) {
                try {
                    SyncJsonData recordData = SyncJsonData.getSyncJsonData(DataType.TRACK,
                            new LineProtocolBean(t.getMeasurement(), t.getTags(),
                                    t.getFields(), t.getTimeNano()));
                    recordDataList.add(recordData);
                    uploadTrackOPData(recordDataList, callback);
                } catch (Exception e) {
                    if (callback != null) {
                        if (e instanceof InvalidParameterException) {
                            callback.onResponse(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, e.getMessage());
                        } else {
                            callback.onResponse(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getMessage());
                        }
                    }
                    LogUtils.e(TAG, e.getMessage());
                }
            }
        });
    }


    /**
     * 同步上传用户埋点信息，并返回上传结果
     *
     * @param recordDataList
     * @param callback
     */
    private void uploadTrackOPData(List<SyncJsonData> recordDataList, AsyncCallback callback) {
        if (recordDataList == null || recordDataList.isEmpty()) {
            return;
        }
        SyncDataHelper syncDataManager = new SyncDataHelper();
        String body = syncDataManager.getBodyContent(DataType.TRACK, recordDataList);
        String model = Constants.URL_MODEL_TRACK_INFLUX;
        String content_type = "text/plain";
        ResponseData result = HttpBuilder.Builder()
                .setModel(model)
                .addHeadParam("Content-Type", content_type)
                .setMethod(RequestMethod.POST)
                .setBodyString(body).executeSync(ResponseData.class);
        if (callback != null) {
            callback.onResponse(result.getHttpCode(), result.getData());
        }
    }


    /**
     * 将单条日志数据存入本地同步
     *
     * @param logBean
     */
    void traceBackground(@NonNull BaseContentBean logBean) {
        ArrayList<BaseContentBean> list = new ArrayList<>();
        list.add(logBean);
        batchTraceBeanBackground(list);
    }


    /**
     * 将单条日志数据存入本地同步
     *
     * @param logBean
     */
    void logBackground(@NonNull LogBean logBean) {
        ArrayList<BaseContentBean> list = new ArrayList<>();
        list.add(logBean);
        batchLogBeanBackground(list);
    }

    /**
     * 将多条日志数据存入本地同步(异步)
     *
     * @param logBeans
     */
    void batchLogBeanBackground(@NonNull List<BaseContentBean> logBeans) {
        try {
            FTLoggerConfig config = FTLoggerConfigManager.get().getConfig();
            JSONObject rumTags = null;
            if (config.isEnableLinkRumData()) {
                rumTags = FTAutoTrack.getRUMPublicTags();
                FTRUMGlobalManager.get().attachRUMRelative(rumTags, false);
            }

            ArrayList<SyncJsonData> datas = new ArrayList<>();
            for (BaseContentBean logBean : logBeans) {
                try {
                    if (Utils.enableTraceSamplingRate(config.getSamplingRate())) {
                        if (rumTags != null) {
                            logBean.setTags(rumTags);
                        }
                        datas.add(SyncJsonData.getFromLogBean(logBean, DataType.LOG));
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, e.getMessage());
                }

            }
            judgeLogCachePolicy(datas);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }

    }

    void batchTraceBeanBackground(@NonNull List<BaseContentBean> logBeans) {
        ArrayList<SyncJsonData> datas = new ArrayList<>();
        for (BaseContentBean logBean : logBeans) {
            try {
                datas.add(SyncJsonData.getFromLogBean(logBean, DataType.TRACE));
            } catch (Exception e) {
                LogUtils.e(TAG, e.getMessage());
            }

        }
        FTDBManager.get().insertFtOptList(datas);
    }


    /**
     * 判断是否需要执行同步策略
     *
     * @param recordDataList
     */
    private void judgeLogCachePolicy(@NonNull List<SyncJsonData> recordDataList) {
        //如果 OP 类型不等于 LOG 则直接进行数据库操作；否则执行同步策略，根据同步策略返回结果判断是否需要执行数据库操作
        int length = recordDataList.size();
        int policyStatus = FTDBCachePolicy.get().optLogCachePolicy(length);
        if (policyStatus >= 0) {//执行同步策略
            if (policyStatus > 0) {
                for (int i = 0; i < policyStatus && i < length; i++) {
                    recordDataList.remove(0);
                }
            }
            boolean result = FTDBManager.get().insertFtOptList(recordDataList);
            LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result);
            SyncTaskManager.get().executeSyncPoll();
        }
    }


}
