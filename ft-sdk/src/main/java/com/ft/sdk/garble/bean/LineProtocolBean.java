package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

/**
 * create by huangDianHua
 * time 2020/3/12 10:59:15
 */
public class LineProtocolBean {
    private String measurement;
    private JSONObject tags;
    private JSONObject fields;
    private long timeNano;

    public LineProtocolBean(String measurement, JSONObject tags, JSONObject fields, long timeNano) {
        this.measurement = measurement;
        this.tags = tags;
        this.fields = fields;
        this.timeNano = timeNano;
    }
    public LineProtocolBean(String measurement, JSONObject tags, JSONObject fields) {
        this.measurement = measurement;
        this.tags = tags;
        this.fields = fields;
        this.timeNano = Utils.getCurrentNanoTime();
    }

    public LineProtocolBean(String measurement, JSONObject fields) {
        this.measurement = measurement;
        this.tags = null;
        this.fields = fields;
        this.timeNano = Utils.getCurrentNanoTime();
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
