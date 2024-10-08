package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description:日志对象(SDK内部使用)
 */
public class BaseContentBean {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "BaseContentBean";
    /**
     * 内容大小限制 30 KB
     */
    protected static final int LIMIT_SIZE = 30720;
    /**
     * 指定当前日志的来源，比如如果来源于 Ngnix，可指定为 Nginx，
     * 同一应用产生的日志 source 应该一样，这样在观测云中方便针对该来源的日志配置同一的提取规则
     */
    String measurement;

    /**
     * 日志内容
     */
    String content;

    /**
     * 运行服务名称
     */
    String serviceName;

    /**
     * 日志生成时间
     */
    long time;
    final JSONObject tags = new JSONObject();
    final JSONObject fields = new JSONObject();

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

    /**
     * 获取所有指标数据
     *
     * @return
     */
    public JSONObject getAllFields() {
        try {
            fields.put(Constants.KEY_MESSAGE, content);
        } catch (JSONException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return fields;
    }

    /**
     * 获取所有标签数据
     *
     * @return
     */
    public JSONObject getAllTags() {
        try {
            if (!Utils.isNullOrEmpty(serviceName)) {
                tags.put(Constants.KEY_SERVICE, serviceName);
            }
        } catch (JSONException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
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

    public void appendTags(JSONObject tags) {
        Iterator<String> it = tags.keys();
        while (it.hasNext()) {
            String key = it.next();
            try {
                this.tags.put(key, tags.get(key));
            } catch (JSONException e) {
                LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            }

        }
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
