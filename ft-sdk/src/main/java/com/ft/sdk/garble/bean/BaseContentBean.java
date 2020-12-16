package com.ft.sdk.garble.bean;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description:日志对象(SDK内部使用)
 */
public class BaseContentBean {
    protected static final int LIMIT_SIZE = 30720;
    //指定当前日志的来源，比如如果来源于 Ngnix，可指定为 Nginx，
    // 同一应用产生的日志 source 应该一样，这样在 DataFlux 中方便针对该来源的日志配置同一的提取规则
    String measurement;
    String content;

    String serviceName;

    EnvType env;

    long time;

    JSONObject tags;
    JSONObject fields;

    /**
     * 是否超过 30KB
     *
     * @param content
     * @return
     */
    private static boolean isOverMaxLength(String content) {
        if (content == null) return false;
        return content.length() > LIMIT_SIZE;
    }

    public BaseContentBean(String measurement, String content, long time) {
        this.measurement = measurement;

        if (isOverMaxLength(content)) {
            this.content = content.substring(0, LIMIT_SIZE);
        } else {
            this.content = content;
        }
        this.time = time;
    }

    public BaseContentBean(String content, long time) {
        this(Constants.FT_LOG_DEFAULT_MEASUREMENT, content, time);
    }

    public BaseContentBean(String measurement, JSONObject json, long time) {
        this(measurement, json.toString(), time);
    }

    public BaseContentBean(JSONObject json, long time) {
        this(Constants.FT_LOG_DEFAULT_MEASUREMENT, json.toString(), time);
    }

    public JSONObject getAllFields() {
        if (fields == null) {
            fields = new JSONObject();
        }
        try {
            fields.put("__content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fields;
    }

    public JSONObject getAllTags() {
        if (tags == null) {
            tags = new JSONObject();
        }
        try {

            if (!tags.has("device_uuid")) {
                tags.put("device_uuid", DeviceUtils.getUuid(FTApplication.getApplication()));
            }

            if (!tags.has("application_uuid")) {
                tags.put("application_uuid", FTSdk.PACKAGE_UUID);
            }

            if (!tags.has("application_identifier")) {
                tags.put("application_identifier", DeviceUtils.getApplicationId(FTApplication.getApplication()));
            }
            tags.put("version", Utils.getAppVersionName());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tags;
    }


    public String getMeasurement() {
        return measurement;
    }

    public String getContent() {
        return content;
    }

    public long getTime() {
        return time;
    }


    public JSONObject getTags() {
        return tags;
    }

    public void setTags(JSONObject tags) {
        this.tags = tags;
    }

    public JSONObject getFields() {
        return fields;
    }

    public void setFields(JSONObject fields) {
        this.fields = fields;
    }

    public void setEnv(EnvType env) {
        this.env = env;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
