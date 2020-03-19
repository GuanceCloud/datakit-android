package com.ft.sdk;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.bean.OP;
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
        track(Collections.singletonList(trackBean), callback);
    }

    /**
     * 主动埋点多条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param trackBeans 多条埋点数据
     * @param callback   上传结果回调
     */
    public void trackImmediate(List<TrackBean> trackBeans, SyncCallback callback) {
        track(trackBeans, callback);
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
        track(OP.CSTM, time, "$flow_" + product, tags, fields);
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
     * 直接将埋点数据同步
     *
     * @param trackBeans
     * @param callback
     */
    private void track(List<TrackBean> trackBeans, SyncCallback callback) {
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
                        RecordData recordData = transTrackBeanToRecordData(OP.CSTM, t.getTimeMillis(), t.getMeasurement(), t.getTags(), t.getFields(), callback);
                        if (recordData != null) {
                            recordDataList.add(recordData);
                        }
                    }
                    updateRecordData(recordDataList, callback);
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
            SyncDataManager.addMonitorData(tagsTemp);
            opData.put("tags", tagsTemp);
            if (fields != null) {
                opData.put(Constants.FIELDS, fields);
            } else {
                LogUtils.e("指标 fields 不能为空");
                if (callback != null) {
                    callback.onResponse(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, "指标集 measurement 不能为空");
                }
                return null;
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
    private void updateRecordData(List<RecordData> recordDataList, SyncCallback callback) {
        if (recordDataList == null || recordDataList.isEmpty()) {
            return;
        }
        SyncDataManager syncDataManager = new SyncDataManager();
        String body = syncDataManager.getBodyContent(recordDataList);
        SyncDataManager.printUpdateData(body);
        ResponseData result = HttpBuilder.Builder()
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
