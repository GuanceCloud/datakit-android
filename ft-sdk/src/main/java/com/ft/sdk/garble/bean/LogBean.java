package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description:日志对象(SDK内部使用)
 */
public class LogBean extends BaseContentBean {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "LogBean";
    /**
     * 指定当前日志的来源，比如如果来源于 Ngnix，可指定为 Nginx，
     * 同一应用产生的日志 source 应该一样，这样在观测云中方便针对该来源的日志配置同一的提取规则
     * 日志等级
     */
    String status = Status.INFO.name;

    /**
     * 用于链路日志，当前链路的请求响应时间，微秒为单位
     */
    long duration;

    /**
     * 扩展属性
     */
    HashMap<String, Object> property = new HashMap<>();

    public HashMap<String, Object> getProperty() {
        return property;
    }

    public LogBean(String content, long time) {
        super(content, time);
    }

    /**
     * 获取所有日志中指标数据
     *
     * @return
     */
    public HashMap<String, Object> getAllFields() {
        super.getAllFields();
//        try {
        if (duration > 0) {
            fields.put(Constants.KEY_TIME_COST_DURATION, duration);
        }

        if (!property.isEmpty()) {
            for (Map.Entry<String, Object> entry : property.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                fields.put(key, value);
            }
        }
//        } catch (JSONException e) {
//            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
//
//        }
        return fields;
    }

    /**
     * 获取所有日志中标签数据
     *
     * @return
     */
    public HashMap<String, Object> getAllTags() {
        super.getAllTags();
//        try {
        tags.put(Constants.KEY_STATUS, status);
//        } catch (JSONException e) {
//            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
//
//        }

        return tags;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
