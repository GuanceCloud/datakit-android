package com.ft.sdk;

import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.utils.Utils;

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
        long time = Utils.getCurrentNanoTime();
        FTTrackInner.getInstance().trackBackground(time, measurement, tags, fields);
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
        long time = Utils.getCurrentNanoTime();
        LineProtocolBean trackBean = new LineProtocolBean(measurement, tags, fields, time);
        FTTrackInner.getInstance().trackAsync(Collections.singletonList(trackBean), callback);
    }

    /**
     * 主动埋点多条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param trackBeans 多条埋点数据
     * @param callback   上传结果回调
     */
    public void trackImmediate(List<LineProtocolBean> trackBeans, AsyncCallback callback) {
        FTTrackInner.getInstance().trackAsync(trackBeans, callback);
    }


}
