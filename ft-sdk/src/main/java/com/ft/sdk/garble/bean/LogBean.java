package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description:日志对象(SDK内部使用)
 */
public class LogBean extends BaseContentBean {
    protected static final int LIMIT_SIZE = 30720;
    //指定当前日志的来源，比如如果来源于 Ngnix，可指定为 Nginx，
    // 同一应用产生的日志 source 应该一样，这样在 DataFlux 中方便针对该来源的日志配置同一的提取规则
    //日志等级
    Status status = Status.INFO;

    //用于链路日志，表示当前 span 操作名，也可理解为 span 名称
    String operationName;
    //用于链路日志，当前链路的请求响应时间，微秒为单位
    long duration;

    public LogBean(String measurement, String content, long time) {
        super(measurement, content, time);
    }

    public LogBean(String content, long time) {
        super(content, time);
    }

    public LogBean(String measurement, JSONObject json, long time) {
        super(measurement, json, time);
    }

    public LogBean(JSONObject json, long time, String measurement) {
        super(json, time);
        this.measurement = measurement;
    }

    public JSONObject getAllFields() {
        super.getAllFields();
        try {
            if (duration > 0) {
                fields.put("duration", duration);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fields;
    }

    public JSONObject getAllTags() {
        super.getAllTags();
        try {
            tags.put("status", status.name);

            if (!Utils.isNullOrEmpty(operationName)) {
                tags.put("operation", operationName);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tags;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


}
