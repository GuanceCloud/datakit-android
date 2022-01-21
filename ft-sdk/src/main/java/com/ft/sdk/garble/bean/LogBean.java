package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description:日志对象(SDK内部使用)
 */
public class LogBean extends BaseContentBean {
    //指定当前日志的来源，比如如果来源于 Ngnix，可指定为 Nginx，
    // 同一应用产生的日志 source 应该一样，这样在 DataFlux 中方便针对该来源的日志配置同一的提取规则
    //日志等级
    Status status = Status.INFO;

    //用于链路日志，当前链路的请求响应时间，微秒为单位
    long duration;

    public LogBean(String content, long time) {
        super(content, time);
    }

    public JSONObject getAllFields() {
        super.getAllFields();
        try {
            if (duration > 0) {
                fields.put(Constants.KEY_TIME_COST_DURATION, duration);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fields;
    }

    public JSONObject getAllTags() {
        super.getAllTags();
        try {
            tags.put(Constants.KEY_STATUS, status.name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tags;
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
