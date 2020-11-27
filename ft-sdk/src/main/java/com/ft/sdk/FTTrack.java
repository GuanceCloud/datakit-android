package com.ft.sdk;

import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.TrackBean;
import com.ft.sdk.garble.manager.AsyncCallback;

import org.json.JSONObject;

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
        FTTrackInner.getInstance().trackBackground(OP.CSTM, time, measurement, tags, fields);
    }

    /**
     * 主动埋点一条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param measurement 埋点事件名称
     * @param tags        埋点数据
     * @param fields      埋点数据
     * @param callback    上传结果回调
     */
    public void trackImmediate(String measurement, JSONObject tags, JSONObject fields, AsyncCallback callback) {
        long time = System.currentTimeMillis();
        TrackBean trackBean = new TrackBean(measurement, tags, fields, time);
        FTTrackInner.getInstance().trackAsync(OP.CSTM, Collections.singletonList(trackBean), callback);
    }

    /**
     * 主动埋点多条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param trackBeans 多条埋点数据
     * @param callback   上传结果回调
     */
    public void trackImmediate(List<TrackBean> trackBeans, AsyncCallback callback) {
        FTTrackInner.getInstance().trackAsync(OP.CSTM, trackBeans, callback);
    }


}
