package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description:日志对象(SDK内部使用)
 */
public class BaseContentBean extends LineProtocolBean {
    /**
     * 内容大小限制 30 KB
     */
    protected static final int LIMIT_SIZE = 30720;

    /**
     * 日志内容
     */
    String content;

    /**
     * 运行服务名称
     */
    String serviceName;


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
        super(measurement, new HashMap<>(), new HashMap<>(), time);
        if (isOverMaxLength(content)) {
            this.content = content.substring(0, LIMIT_SIZE);
        } else {
            this.content = content;
        }
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
    public HashMap<String, Object> getAllTags() {
        if (!Utils.isNullOrEmpty(serviceName)) {
            tags.put(Constants.KEY_SERVICE, serviceName);
        }
        return tags;
    }

    public String getContent() {
        return content;
    }

    public void appendTags(HashMap<String, Object> tags) {
        this.tags.putAll(tags);
    }

    public void appendFields(HashMap<String, Object> fields) {
        this.fields.putAll(fields);
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
