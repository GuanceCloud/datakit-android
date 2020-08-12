package com.ft.sdk.garble;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.ObjectBean;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.bean.TrackBean;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.manager.SyncDataManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
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
     * 将单条日志数据存入本地同步
     *
     * @param logBean
     */
    public void logBackground(LogBean logBean) {
        if (logBean == null) {
            return;
        }
        FTTrackInner.getInstance().track(OP.LOG, logBean.getTime(), logBean.getMeasurement(), logBean.getAllTags(), logBean.getAllFields());
    }

    /**
     * 将多条日志数据存入本地同步(异步)
     *
     * @param logBeans
     */
    public void logBackground(List<LogBean> logBeans) {
        if (logBeans == null) {
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (LogBean logBean : logBeans) {
            trackBeans.add(new TrackBean(logBean.getMeasurement(), logBean.getAllTags(), logBean.getAllFields(), logBean.getTime()));
        }
        FTTrackInner.getInstance().trackAsync(OP.LOG, trackBeans);
    }

    /**
     * 将多条日志数据存入本地同步（同步）
     *
     * @param logBeans
     */
    public void logBackgroundSync(List<LogBean> logBeans) {
        if (logBeans == null) {
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (LogBean logBean : logBeans) {
            trackBeans.add(new TrackBean(logBean.getMeasurement(), logBean.getAllTags(), logBean.getAllFields(), logBean.getTime()));
        }
        track(OP.LOG, trackBeans);
    }

    /**
     * 将单条对象数据直接同步
     *
     * @param objectBean
     */
    public void objectBackground(ObjectBean objectBean) {
        List<TrackBean> trackBeans = new ArrayList<>();
        trackBeans.add(new TrackBean("", null, objectBean.getJSONData(), 0));
        trackAsync(OP.OBJECT, trackBeans);
    }

    /**
     * 在子线程中将埋点数据同步（经过数据库）
     *
     * @param op
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    public void track(OP op, long time, String measurement, final JSONObject tags, JSONObject fields) {
        try {
            if (!isLegalValues(fields)) {
                return;
            }
            ThreadPoolUtils.get().execute(() -> {
                try {
                    RecordData recordData = transTrackBeanToRecordData(op, time, measurement, tags, fields, null);
                    if (recordData == null) {
                        return;
                    }
                    LogUtils.d(TAG,"FTTrack数据进数据库：" + recordData.printFormatRecordData());
                    judgeNeedOptCachePolicy(op,null,recordData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在子线程中将埋点数据同步（经过数据库）
     *
     * @param trackBeans
     */
    public void trackAsync(OP op, List<TrackBean> trackBeans) {
        try {
            if (trackBeans == null) {
                return;
            }
            ThreadPoolUtils.get().execute(() -> {
                track(op, trackBeans);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在当前线程中将埋点数据同步（经过数据库）
     *
     * @param trackBeans
     */
    private void track(OP op, List<TrackBean> trackBeans) {
        if (trackBeans == null) {
            return;
        }
        try {
            List<RecordData> recordDataList = new ArrayList<>();
            for (TrackBean t : trackBeans) {
                if (op != OP.OBJECT && !isLegalValues(t.getFields())) {
                    continue;
                }
                RecordData recordData = transTrackBeanToRecordData(op, t.getTimeMillis(), t.getMeasurement(), t.getTags(), t.getFields(), null);
                if (recordData != null) {
                    recordDataList.add(recordData);
                }
            }
            judgeNeedOptCachePolicy(op,recordDataList,null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否需要执行同步策略
     * @param op
     * @param recordDataList
     * @param recordData
     */
    private void judgeNeedOptCachePolicy(OP op,List<RecordData> recordDataList,RecordData recordData){
        //如果 OP 类型不等于 LOG 则直接进行数据库操作；否则执行同步策略，根据同步策略返回结果判断是否需要执行数据库操作
        if(op != OP.LOG || FTDBCachePolicy.get().optLogCachePolicy(recordDataList==null?1:recordDataList.size())){//执行同步策略
            if(recordDataList != null) {
                FTManager.getFTDBManager().insertFtOptList(recordDataList);
            }
            if(recordData != null){
                FTManager.getFTDBManager().insertFTOperation(recordData);
            }
            FTManager.getSyncTaskManager().executeSyncPoll();
        }
    }

    /**
     * 在子线程中将埋点数据同步(不经过数据库)
     *
     * @param trackBeans
     * @param callback
     */
    public void track(OP op, List<TrackBean> trackBeans, SyncCallback callback) {
        try {
            if (trackBeans == null) {
                return;
            }

            ThreadPoolUtils.get().execute(() -> {
                try {
                    List<RecordData> recordDataList = new ArrayList<>();
                    for (TrackBean t : trackBeans) {
                        if (op != OP.OBJECT && !isLegalValues(t.getFields())) {
                            if (callback != null) {
                                callback.onResponse(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, "参数 fields 不能为空");
                            }
                            continue;
                        }
                        RecordData recordData = transTrackBeanToRecordData(op, t.getTimeMillis(), t.getMeasurement(), t.getTags(), t.getFields(), callback);
                        if (recordData != null) {
                            recordDataList.add(recordData);
                        }
                    }
                    DataType dataType;
                    switch (op) {
                        case LOG:
                            dataType = DataType.LOG;
                            break;
                        case KEYEVENT:
                            dataType = DataType.KEY_EVENT;
                            break;
                        case OBJECT:
                            dataType = DataType.OBJECT;
                            break;
                        default:
                            dataType = DataType.TRACK;
                    }
                    updateRecordData(dataType, recordDataList, callback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将用户埋点的数据转换成RecordData
     *
     * @param op
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     * @param callback
     * @return
     */
    private RecordData transTrackBeanToRecordData(OP op, long time, String measurement, final JSONObject tags, JSONObject fields, SyncCallback callback) {
        JSONObject tagsTemp = tags;
        RecordData recordData = new RecordData();
        try {
            recordData.setOp(op.value);
            recordData.setTime(time);
            JSONObject opData = new JSONObject();

            if (measurement != null) {
                opData.put(Constants.MEASUREMENT, measurement);
            } else {
                LogUtils.e(TAG,"指标集 measurement 不能为空");
                if (callback != null) {
                    callback.onResponse(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, "指标集 measurement 不能为空");
                }
                return null;
            }
            if (tagsTemp == null) {
                tagsTemp = new JSONObject();
            }
            opData.put(Constants.TAGS, tagsTemp);
            if (fields != null) {
                opData.put(Constants.FIELDS, fields);
            } else {
                LogUtils.e(TAG,"指标 fields 不能为空");
                if (callback != null) {
                    callback.onResponse(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, "指标集 measurement 不能为空");
                }
                return null;
            }
            if (op == OP.CSTM) {
                SyncDataManager.addMonitorData(tagsTemp, fields);
            }
            recordData.setOpdata(opData.toString());
            String sessionId = FTUserConfig.get().getSessionId();
            if (!Utils.isNullOrEmpty(sessionId)) {
                recordData.setSessionid(sessionId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recordData;
    }

    /**
     * 异步上传用户埋点信息，并返回上传结果
     *
     * @param recordDataList
     * @param callback
     */
    private void updateRecordData(DataType dataType, List<RecordData> recordDataList, SyncCallback callback) {
        if (!TokenCheck.get().checkToken()) {
            if (callback != null) {
                callback.onResponse(HttpURLConnection.HTTP_OK, TokenCheck.get().message);
                return;
            }
        }
        if (recordDataList == null || recordDataList.isEmpty()) {
            return;
        }
        SyncDataManager syncDataManager = new SyncDataManager();
        String body = syncDataManager.getBodyContent(dataType, recordDataList);
        SyncDataManager.printUpdateData(dataType == DataType.OBJECT, body);
        body = body.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION).replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK);
        String model = Constants.URL_MODEL_TRACK;
        switch (dataType) {
            case KEY_EVENT:
                model = Constants.URL_MODEL_KEY_EVENT;
                break;
            case LOG:
                model = Constants.URL_MODEL_LOG;
                break;
            case OBJECT:
                model = Constants.URL_MODEL_OBJECT;
                break;
            case TRACK:
                model = Constants.URL_MODEL_TRACK;
        }
        String content_type = "text/plain";
        if (DataType.OBJECT == dataType) {
            content_type = "application/json";
        }
        ResponseData result = HttpBuilder.Builder()
                .setModel(model)
                .addHeadParam("Content-Type", content_type)
                .setMethod(RequestMethod.POST)
                .setBodyString(body).executeSync(ResponseData.class);
        callback.onResponse(result.getHttpCode(), result.getData());
    }

    /**
     * 判断是否是合法的Values
     *
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    public boolean isLegalValues(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            LogUtils.e(TAG,"参数 fields 不能为空");
            return false;
        }
        if (jsonObject.keys().hasNext()) {
            return true;
        } else {
            LogUtils.e(TAG,"参数 fields 不能为空");
            return false;
        }
    }
}
