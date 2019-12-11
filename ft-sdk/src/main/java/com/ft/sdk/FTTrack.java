package com.ft.sdk;

import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

import org.json.JSONException;
import org.json.JSONObject;

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
     * @param event 埋点事件名称
     * @param tags 埋点数据
     * @param values 埋点数据
     */
    public void track(String event, JSONObject tags, JSONObject values) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, event, tags, values);
    }

    /**
     * 主动埋点
     * @param event 埋点事件名称
     * @param tags 埋点数据
     */
    public void trackTags(String event, JSONObject tags) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, event, tags, null);
    }

    /**
     * 主动埋点
     * @param event 埋点事件名称
     * @param values 埋点数据
     */
    public void trackValues(String event, JSONObject values) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, event, null, values);
    }

    private void track(OP op, long time, String field, JSONObject tags, JSONObject values) {
        final RecordData recordData = new RecordData();
        recordData.setOp(op.value);
        recordData.setTime(time);
        JSONObject opData = new JSONObject();
        try {
            if (field != null) {
                opData.put("field", field);
            }
            if (tags != null) {
                opData.put("tags", tags);
            }
            if (values != null) {
                opData.put("values", values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        recordData.setOpdata(opData.toString());
        ThreadPoolUtils.get().execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("FTTrack数据进数据库："+recordData.getJsonString());
                FTManager.getFTDBManager().insertFTOperation(recordData);
                FTManager.getSyncTaskManaget().executeSyncPoll();
            }
        });

    }

}
