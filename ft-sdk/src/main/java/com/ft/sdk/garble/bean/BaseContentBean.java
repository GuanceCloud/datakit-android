package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
    final HashMap<String, Object> tags = new HashMap<>();
    final HashMap<String, Object> fields = new HashMap<>();

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
    public HashMap<String, Object> getAllFields() {
        fields.put(Constants.KEY_MESSAGE, content);

        return fields;
    }

    /**
     * 获取所有标签数据
     *
     * @return
     */
    public HashMap<String,Object> getAllTags() {
        if (!Utils.isNullOrEmpty(serviceName)) {
            tags.put(Constants.KEY_SERVICE, serviceName);
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

    public void appendTags(HashMap<String, Object> tags) {
        this.tags.putAll(tags);
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
