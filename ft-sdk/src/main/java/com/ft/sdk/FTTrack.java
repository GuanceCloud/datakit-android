package com.ft.sdk;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.TokenCheck;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.KeyEventBean;
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
        track(OP.CSTM, time, measurement, tags, fields);
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
        track(OP.CSTM, Collections.singletonList(trackBean), callback);
    }

    /**
     * 主动埋点多条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param trackBeans 多条埋点数据
     * @param callback   上传结果回调
     */
    public void trackImmediate(List<TrackBean> trackBeans, SyncCallback callback) {
        track(OP.CSTM, trackBeans, callback);
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
        track(OP.LOG, logBean.getTime(), logBean.getMeasurement(), logBean.getAllTags(), logBean.getAllFields());
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
        trackAsync(OP.LOG, trackBeans);
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
     * 将单条日志数据及时上传并回调结果
     *
     * @param logBean
     */
    public void logImmediate(LogBean logBean, SyncCallback syncCallback) {
        if (logBean == null) {
            return;
        }
        TrackBean trackBean = new TrackBean(logBean.getMeasurement(), logBean.getAllTags(), logBean.getAllFields(), logBean.getTime());
        track(OP.LOG, Collections.singletonList(trackBean), syncCallback);
    }

    /**
     * 将多条日志数据存入本地同步
     *
     * @param logBeans
     */
    public void logImmediate(List<LogBean> logBeans, SyncCallback syncCallback) {
        if (logBeans == null) {
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (LogBean logBean : logBeans) {
            trackBeans.add(new TrackBean(logBean.getMeasurement(), logBean.getAllTags(), logBean.getAllFields(), logBean.getTime()));
        }
        track(OP.LOG, trackBeans, syncCallback);
    }

    /**
     * 将单条事件数据存入本地同步
     *
     * @param keyEventBean
     */
    public void keyEventBackground(KeyEventBean keyEventBean) {
        if (keyEventBean == null) {
            return;
        }
        track(OP.KEYEVENT, keyEventBean.getTime(), keyEventBean.getMeasurement(), keyEventBean.getAllTags(), keyEventBean.getAllFields());
    }

    /**
     * 将多条事件数据存入本地同步
     *
     * @param keyEventBeans
     */
    public void keyEventBackground(List<KeyEventBean> keyEventBeans) {
        if (keyEventBeans == null) {
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (KeyEventBean keyEventBean : keyEventBeans) {
            trackBeans.add(new TrackBean(keyEventBean.getMeasurement(), keyEventBean.getAllTags(), keyEventBean.getAllFields(), keyEventBean.getTime()));
        }
        trackAsync(OP.KEYEVENT, trackBeans);
    }

    /**
     * 将单条事件数据及时上传并回调结果
     *
     * @param keyEventBean
     */
    public void keyEventImmediate(KeyEventBean keyEventBean, SyncCallback syncCallback) {
        if (keyEventBean == null) {
            return;
        }
        TrackBean trackBean = new TrackBean(keyEventBean.getMeasurement(), keyEventBean.getAllTags(), keyEventBean.getAllFields(), keyEventBean.getTime());
        track(OP.KEYEVENT, Collections.singletonList(trackBean), syncCallback);
    }

    /**
     * 将多条事件数据存入本地同步
     *
     * @param keyEventBeans
     */
    public void keyEventImmediate(List<KeyEventBean> keyEventBeans, SyncCallback syncCallback) {
        if (keyEventBeans == null) {
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (KeyEventBean keyEventBean : keyEventBeans) {
            trackBeans.add(new TrackBean(keyEventBean.getMeasurement(), keyEventBean.getAllTags(), keyEventBean.getAllFields(), keyEventBean.getTime()));
        }
        track(OP.KEYEVENT, trackBeans, syncCallback);
    }

    /**
     * 将多条对象数据直接同步
     *
     * @param objectBeans
     */
    public void objectImmediate(List<ObjectBean> objectBeans, SyncCallback syncCallback) {
        if (objectBeans == null) {
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (ObjectBean objectBean : objectBeans) {
            trackBeans.add(new TrackBean("", null, objectBean.getJSONData(), 0));
        }
        track(OP.OBJECT, trackBeans, syncCallback);
    }

    /**
     * 将单条对象数据直接同步
     *
     * @param objectBean
     */
    public void objectImmediate(ObjectBean objectBean, SyncCallback syncCallback) {
        if (objectBean == null) {
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        trackBeans.add(new TrackBean("", null, objectBean.getJSONData(), 0));
        track(OP.OBJECT, trackBeans, syncCallback);
    }

    /**
     * 将多条对象数据直接同步
     *
     * @param objectBeans
     */
    public void objectBackground(List<ObjectBean> objectBeans) {
        if (objectBeans == null) {
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (ObjectBean objectBean : objectBeans) {
            trackBeans.add(new TrackBean("", null, objectBean.getJSONData(), 0));
        }
        trackAsync(OP.OBJECT, trackBeans);
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
     * 将埋点数据存入本地后通知同步
     *
     * @param op
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    private void track(OP op, long time, String measurement, final JSONObject tags, JSONObject fields) {
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
                    LogUtils.d("FTTrack数据进数据库：" + recordData.getJsonString());
                    FTManager.getFTDBManager().insertFTOperation(recordData);
                    FTManager.getSyncTaskManager().executeSyncPoll();
                } catch (Exception e) {
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接将埋点数据存入本地等待同步
     *
     * @param trackBeans
     */
    private void trackAsync(OP op, List<TrackBean> trackBeans) {
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
     * 直接将埋点数据存入本地等待同步
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
            FTManager.getFTDBManager().insertFtOptList(recordDataList);
            FTManager.getSyncTaskManager().executeSyncPoll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接将埋点数据同步
     *
     * @param trackBeans
     * @param callback
     */
    private void track(OP op, List<TrackBean> trackBeans, SyncCallback callback) {
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
                LogUtils.e("指标集 measurement 不能为空");
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
                LogUtils.e("指标 fields 不能为空");
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
    boolean isLegalValues(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            LogUtils.e("参数 fields 不能为空");
            return false;
        }
        if (jsonObject.keys().hasNext()) {
            return true;
        } else {
            LogUtils.e("参数 fields 不能为空");
            return false;
        }
    }
}
