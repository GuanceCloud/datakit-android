package com.ft.sdk;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.manager.SyncDataManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Iterator;

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
     * @param event  埋点事件名称
     * @param tags   埋点数据
     * @param values 埋点数据
     */
    public void trackBackground(String event, JSONObject tags, JSONObject values) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, event, tags, values);
    }

    /**
     * 主动埋点，异步上传用户埋点数据并返回上传结果
     *
     * @param event  埋点事件名称
     * @param tags   埋点数据
     * @param values 埋点数据
     * @param callback 上传结果回调
     */
    public void trackImmediate(String event, JSONObject tags, JSONObject values,SyncCallback callback) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, event, tags, values,false,callback);
    }

    /**
     * 流程图数据上报
     *
     * @param product 指标集，流程图以该值进行分类
     * @param traceId 标示一个流程图的全程唯一ID
     * @param name 流程节点名称
     * @param parent 流程图当前流程节点的上一个流程节点名称，如果是第一个节点，该值应填null
     * @param duration 流程图在该节点所耗费或持续时间，单位为毫秒
     * @param tags 其他标签值（该值中不能含 traceId，name，parent 字段）
     * @param values 其他指标（该值中不能含 duration 字段）
     */
    public void trackFlowChart(String product, String traceId, String name, String parent, long duration,JSONObject tags,JSONObject values) {
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
        if(tags == null) {
            tags = new JSONObject();
        }
        if(values == null) {
            values = new JSONObject();
        }
        Iterator<String> iteTag = tags.keys();
        while (iteTag.hasNext()){
            if(iteTag.next().contains("$")){
                throw new InvalidParameterException("参数 tags 中不能使用保留字段符号 $");
            }
        }

        Iterator<String> iteValue = values.keys();
        while (iteValue.hasNext()){
            if(iteValue.next().contains("$")){
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
            values.put("$duration", duration);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        track(OP.CSTM, time, "$flow_" + product, tags, values);
    }

    private void track(OP op, long time, String field, final JSONObject tags, JSONObject values) {
        track(op,time,field,tags,values,true, null);
    }
    private void track(OP op, long time, String field, final JSONObject tags, JSONObject values, boolean insertDB, SyncCallback callback) {
        try {
            if (!isLegalValues(values)) {
                return;
            }
            ThreadPoolUtils.get().execute(() -> {
                JSONObject tagsTemp = tags;
                try {
                    final RecordData recordData = new RecordData();
                    recordData.setOp(op.value);
                    recordData.setTime(time);
                    JSONObject opData = new JSONObject();

                    if (field != null) {
                        opData.put("field", field);
                    }
                    if (tagsTemp == null) {
                        tagsTemp = new JSONObject();
                    }
                    //SyncDataManager.addMonitorData(tagsTemp);
                    opData.put("tags", tagsTemp);
                    if (values != null) {
                        opData.put("values", values);
                    }
                    recordData.setOpdata(opData.toString());
                    String sessionId = FTUserConfig.get().getSessionId();
                    if (!Utils.isNullOrEmpty(sessionId)) {
                        recordData.setSessionid(sessionId);
                    }

                    if(insertDB) {
                        LogUtils.d("FTTrack数据进数据库：" + recordData.getJsonString());
                        FTManager.getFTDBManager().insertFTOperation(recordData);
                        FTManager.getSyncTaskManager().executeSyncPoll();
                    }else{
                        updateRecordData(recordData,callback);
                    }
                } catch (Exception e) {
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步上传用户埋点信息，并返回上传结果
     * @param recordData
     * @param callback
     */
    private void updateRecordData(RecordData recordData,SyncCallback callback){
        SyncDataManager syncDataManager = new SyncDataManager();
        String body = syncDataManager.getBodyContent(Collections.singletonList(recordData));
        FTResponseData result = HttpBuilder.Builder()
                .setMethod(RequestMethod.POST)
                .setBodyString(body).executeSync(FTResponseData.class);

        try {
            callback.isSuccess(result.getCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            callback.isSuccess(false);
        }
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
            LogUtils.e("参数 Values 不能为空");
            return false;
        }
        if (jsonObject.keys().hasNext()) {
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object obj = jsonObject.get(key);
                if (obj instanceof JSONObject || obj instanceof JSONArray) {
                    LogUtils.e("参数 Values 中含有非法数据类型");
                    return false;
                }
            }
            return true;
        } else {
            LogUtils.e("参数 Values 不能为空");
            return false;
        }
    }
}
