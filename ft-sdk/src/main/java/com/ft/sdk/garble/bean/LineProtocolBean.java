package com.ft.sdk.garble.bean;

import java.util.HashMap;

/**
 * create by huangDianHua
 * time 2020/3/12 10:59:15
 */
public class LineProtocolBean {
    /**
     * Data index
     */
    private final String measurement;
    /**
     * Tag data
     */
    final HashMap<String, Object> tags;
    /**
     * Index data
     */
    final HashMap<String, Object> fields;
    /**
     * Generation time, unit nanosecond
     */
    final long timeNano;

    public LineProtocolBean(String measurement, HashMap<String, Object> tags, HashMap<String, Object> fields, long timeNano) {
        this.measurement = measurement;
        this.tags = tags;
        this.fields = fields;
        this.timeNano = timeNano;
    }

    public String getMeasurement() {
        return measurement;
    }

    public HashMap<String, Object> getTags() {
        return tags;
    }

    public HashMap<String, Object> getFields() {
        return fields;
    }

    public long getTimeNano() {
        return timeNano;
    }
}
