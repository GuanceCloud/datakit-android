package com.ft.sdk;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.manager.SyncDataManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public void track(String event, JSONObject tags, JSONObject values) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, event, tags, values);
    }

    /**
     * 主动埋点
     *
     * @param event  埋点事件名称
     * @param values 埋点数据
     */
    public void trackValues(String event, JSONObject values) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, event, null, values);
    }

    private void track(OP op, long time, String field, JSONObject tags, JSONObject values) {
        try {
            if (!isLegalValues(values)) {
                return;
            }
            final RecordData recordData = new RecordData();
            recordData.setOp(op.value);
            recordData.setTime(time);
            JSONObject opData = new JSONObject();

            if (field != null) {
                opData.put("field", field);
            }
            if (tags == null) {
                tags = new JSONObject();
            }
            SyncDataManager.addMonitorData(tags);
            opData.put("tags", tags);
            if (values != null) {
                opData.put("values", values);
            }
            recordData.setOpdata(opData.toString());
            String sessionId = FTUserConfig.get().getSessionId();
            if(!Utils.isNullOrEmpty(sessionId)){
                recordData.setSessionid(sessionId);
            }
            ThreadPoolUtils.get().execute(() -> {
                LogUtils.d("FTTrack数据进数据库：" + recordData.getJsonString());
                FTManager.getFTDBManager().insertFTOperation(recordData);
                FTManager.getSyncTaskManager().executeSyncPoll();
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否是合法的Values
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    public boolean isLegalValues(JSONObject jsonObject) throws JSONException{
        if(jsonObject == null){
            LogUtils.e("参数 Values 不能为空");
            return false;
        }
        if(jsonObject.keys().hasNext()){
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()){
                String key = iterator.next();
                Object obj = jsonObject.get(key);
                if(obj instanceof JSONObject || obj instanceof JSONArray){
                    LogUtils.e("参数 Values 中含有非法数据类型");
                    return false;
                }
            }
            return true;
        }else{
            LogUtils.e("参数 Values 不能为空");
            return false;
        }
    }
}
