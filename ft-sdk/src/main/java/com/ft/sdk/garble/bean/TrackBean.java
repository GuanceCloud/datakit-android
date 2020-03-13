package com.ft.sdk.garble.bean;

import org.json.JSONObject;

/**
 * create by huangDianHua
 * time 2020/3/12 10:59:15
 */
public class TrackBean {
    private String measurement;
    private JSONObject tags;
    private JSONObject fields;
    private long timeMillis;

    public TrackBean(String measurement, JSONObject tags, JSONObject fields, long timeMillis) {
        this.measurement = measurement;
        this.tags = tags;
        this.fields = fields;
        this.timeMillis = timeMillis;
    }
    public TrackBean(String measurement, JSONObject tags, JSONObject fields) {
        this.measurement = measurement;
        this.tags = tags;
        this.fields = fields;
        this.timeMillis = System.currentTimeMillis();
    }

    public TrackBean(String measurement, JSONObject fields) {
        this.measurement = measurement;
        this.tags = null;
        this.fields = fields;
        this.timeMillis = System.currentTimeMillis();
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

    public long getTimeMillis() {
        return timeMillis;
    }
}
