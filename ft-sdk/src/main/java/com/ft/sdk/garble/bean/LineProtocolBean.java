package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

/**
 * create by huangDianHua
 * time 2020/3/12 10:59:15
 */
public class LineProtocolBean {
    /**
     * 数据指标
     */
    private final String measurement;
    /**
     * 标签数据
     */
    private final JSONObject tags;
    /**
     * 指标数据
     */
    private final JSONObject fields;
    /**
     *  产生时间，单位纳秒
     */
    private final long timeNano;

    public LineProtocolBean(String measurement, JSONObject tags, JSONObject fields, long timeNano) {
        this.measurement = measurement;
        this.tags = tags;
        this.fields = fields;
        this.timeNano = timeNano;
    }

    public String getMeasurement() {
        return measurement;
    }

    public JSONObject getTags() {
        return tags;
    }

    public JSONObject getFields() {
        return fields;
    }

    public long getTimeNano() {
        return timeNano;
    }
}
