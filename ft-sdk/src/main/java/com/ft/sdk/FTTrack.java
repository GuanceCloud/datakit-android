package com.ft.sdk;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.SyncCallback;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
        track(OP.CSTM,Collections.singletonList(trackBean), callback);
    }

    /**
     * 主动埋点多条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param trackBeans 多条埋点数据
     * @param callback   上传结果回调
     */
    public void trackImmediate(List<TrackBean> trackBeans, SyncCallback callback) {
        track(OP.CSTM,trackBeans, callback);
    }

    /**
     * 流程图数据上报
     *
     * @param product  指标集，流程图以该值进行分类
     * @param traceId  标示一个流程图的全程唯一ID
     * @param name     流程节点名称
     * @param parent   流程图当前流程节点的上一个流程节点名称，如果是第一个节点，该值应填null
     * @param duration 流程图在该节点所耗费或持续时间，单位为毫秒
     * @param tags     其他标签值（该值中不能含 traceId，name，parent 字段）
     * @param fields   其他指标（该值中不能含 duration 字段）
     */
    public void trackFlowChart(String product, String traceId, String name, String parent, long duration, JSONObject tags, JSONObject fields) {
        if (Utils.isNullOrEmpty(product)) {
            throw new InvalidParameterException("参数 product 不能为空");
        }

        if (!Utils.isLegalProduct(product)) {
            throw new InvalidParameterException("参数 product 不合法，只能包含英文字母、数字、中划线和下划线，最长 40 个字符，区分大小写");
        }

        if (Utils.isNullOrEmpty(traceId)) {
            throw new InvalidParameterException("参数 traceId 不能为空");
        }

        if (Utils.isNullOrEmpty(name)) {
            throw new InvalidParameterException("参数 name 不能为空");
        }

        long time = System.currentTimeMillis();
        if (tags == null) {
            tags = new JSONObject();
        }
        if (fields == null) {
            fields = new JSONObject();
        }
        Iterator<String> iteTag = tags.keys();
        while (iteTag.hasNext()) {
            if (iteTag.next().contains("$")) {
                throw new InvalidParameterException("参数 tags 中不能使用保留字段符号 $");
            }
        }

        Iterator<String> iteValue = fields.keys();
        while (iteValue.hasNext()) {
            if (iteValue.next().contains("$")) {
                throw new InvalidParameterException("参数 values 中不能使用保留字段符号 $");
            }
        }

        if (tags.has("traceId")) {
            throw new InvalidParameterException("参数 tags 中不能包含保留字段 traceId");
        }
        if (tags.has("name")) {
            throw new InvalidParameterException("参数 tags 中不能包含保留字段 name");
        }
        if (tags.has("parent")) {
            throw new InvalidParameterException("参数 tags 中不能包含保留字段 parent");
        }

        if (tags.has("duration")) {
            throw new InvalidParameterException("参数 values 中不能包含保留字段 duration");
        }
        try {
            tags.put("$traceId", traceId);
            tags.put("$name", name);
            if (!Utils.isNullOrEmpty(parent)) {
                tags.put("$parent", parent);
            }
            fields.put("$duration", duration);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        track(OP.FLOW_CHAT, time, "$flow_" + product, tags, fields);
    }

    /**
     * 将单条日志数据存入本地同步
     * @param logBean
     */
    public void logBackground(LogBean logBean) {
        if(logBean == null){
            return;
        }
        track(OP.LOG,logBean.getTime(),logBean.getMeasurement(),logBean.getAllTags(),logBean.getAllFields());
    }

    /**
     * 将多条日志数据存入本地同步
     * @param logBeans
     */
    public void logBackground(List<LogBean> logBeans) {
        if(logBeans == null){
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (LogBean logBean : logBeans) {
            trackBeans.add(new TrackBean(logBean.getMeasurement(),logBean.getAllTags(),logBean.getAllFields(),logBean.getTime()));
        }
        track(OP.LOG,trackBeans);
    }

    /**
     * 将单条日志数据及时上传并回调结果
     * @param logBean
     */
    public void logImmediate(LogBean logBean,SyncCallback syncCallback){
        if(logBean == null){
            return;
        }
        TrackBean trackBean = new TrackBean(logBean.getMeasurement(),logBean.getAllTags(),logBean.getAllFields(),logBean.getTime());
        track(OP.LOG,Collections.singletonList(trackBean),syncCallback);
    }

    /**
     * 将多条日志数据存入本地同步
     * @param logBeans
     */
    public void logImmediate(List<LogBean> logBeans,SyncCallback syncCallback) {
        if(logBeans == null){
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (LogBean logBean : logBeans) {
            trackBeans.add(new TrackBean(logBean.getMeasurement(),logBean.getAllTags(),logBean.getAllFields(),logBean.getTime()));
        }
        track(OP.LOG,trackBeans,syncCallback);
    }

    /**
     * 将单条事件数据存入本地同步
     * @param keyEventBean
     */
    public void keyEventBackground(KeyEventBean keyEventBean) {
        if(keyEventBean == null){
            return;
        }
        track(OP.KEYEVENT,keyEventBean.getTime(),keyEventBean.getMeasurement(),keyEventBean.getAllTags(),keyEventBean.getAllFields());
    }

    /**
     * 将多条事件数据存入本地同步
     * @param keyEventBeans
     */
    public void keyEventBackground(List<KeyEventBean> keyEventBeans) {
        if(keyEventBeans == null){
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (KeyEventBean keyEventBean : keyEventBeans) {
            trackBeans.add(new TrackBean(keyEventBean.getMeasurement(),keyEventBean.getAllTags(),keyEventBean.getAllFields(),keyEventBean.getTime()));
        }
        track(OP.KEYEVENT,trackBeans);
    }

    /**
     * 将单条事件数据及时上传并回调结果
     * @param keyEventBean
     */
    public void keyEventImmediate(KeyEventBean keyEventBean,SyncCallback syncCallback){
        if(keyEventBean == null){
            return;
        }
        TrackBean trackBean = new TrackBean(keyEventBean.getMeasurement(),keyEventBean.getAllTags(),keyEventBean.getAllFields(),keyEventBean.getTime());
        track(OP.KEYEVENT,Collections.singletonList(trackBean),syncCallback);
    }

    /**
     * 将多条事件数据存入本地同步
     * @param keyEventBeans
     */
    public void keyEventImmediate(List<KeyEventBean> keyEventBeans,SyncCallback syncCallback) {
        if(keyEventBeans == null){
            return;
        }
        List<TrackBean> trackBeans = new ArrayList<>();
        for (KeyEventBean keyEventBean : keyEventBeans) {
            trackBeans.add(new TrackBean(keyEventBean.getMeasurement(),keyEventBean.getAllTags(),keyEventBean.getAllFields(),keyEventBean.getTime()));
        }
        track(OP.KEYEVENT,trackBeans,syncCallback);
    }

    /**
     * 将多条对象数据直接同步
     * @param objectBeans
     */
    public void objectImmediate(List<ObjectBean> objectBeans, SyncCallback syncCallback) {
        if(objectBeans == null){
            return;
        }
        JSONArray jsonArray = new JSONArray();
        for (ObjectBean objectBean : objectBeans) {
            jsonArray.put(objectBean.getJSONData());
        }

        ThreadPoolUtils.get().execute(() -> {
            try {
                ResponseData result = HttpBuilder.Builder()
                        .setModel(Constants.URL_MODEL_OBJECT)
                        .addHeadParam("Content-Type","application/json")
                        .setMethod(RequestMethod.POST)
                        .setBodyString(jsonArray.toString()).executeSync(ResponseData.class);
                syncCallback.onResponse(result.getHttpCode(), result.getData());
            } catch (Exception e) {
            }
        });
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
    private void track(OP op,List<TrackBean> trackBeans) {
        try {
            if (trackBeans == null) {
                return;
            }

            ThreadPoolUtils.get().execute(() -> {
                try {
                    List<RecordData> recordDataList = new ArrayList<>();
                    for (TrackBean t : trackBeans) {
                        if (!isLegalValues(t.getFields())) {
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
            });
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
    private void track(OP op,List<TrackBean> trackBeans, SyncCallback callback) {
        try {
            if (trackBeans == null) {
                return;
            }

            ThreadPoolUtils.get().execute(() -> {
                try {
                    List<RecordData> recordDataList = new ArrayList<>();
                    for (TrackBean t : trackBeans) {
                        if (!isLegalValues(t.getFields())) {
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
                        default:
                            dataType = DataType.TRACK;
                    }
                    updateRecordData(dataType,recordDataList, callback);
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
            if(op == OP.CSTM) {
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
    private void updateRecordData(DataType dataType,List<RecordData> recordDataList, SyncCallback callback) {
        if (recordDataList == null || recordDataList.isEmpty()) {
            return;
        }
        SyncDataManager syncDataManager = new SyncDataManager();
        String body = syncDataManager.getBodyContent(dataType,recordDataList);
        SyncDataManager.printUpdateData(body);
        body = body.replaceAll(Constants.SEPARATION_PRINT,Constants.SEPARATION).replaceAll(Constants.SEPARATION_LINE_BREAK,Constants.SEPARATION_REALLY_LINE_BREAK);
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
        ResponseData result = HttpBuilder.Builder()
                .setModel(model)
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
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object obj = jsonObject.get(key);
                if (obj instanceof JSONObject || obj instanceof JSONArray) {
                    LogUtils.e("参数 fields 中含有非法数据类型");
                    return false;
                }
            }
            return true;
        } else {
            LogUtils.e("参数 fields 不能为空");
            return false;
        }
    }
}
