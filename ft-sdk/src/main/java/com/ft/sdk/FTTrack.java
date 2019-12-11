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

    public void track(String field, JSONObject tags, JSONObject values) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, field, tags, values);
    }

    public void trackTags(String field, JSONObject tags) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, field, tags, null);
    }

    public void trackValues(String field, JSONObject values) {
        long time = System.currentTimeMillis();
        track(OP.CSTM, time, field, null, values);
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
