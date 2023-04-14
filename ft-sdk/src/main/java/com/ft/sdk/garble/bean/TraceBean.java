package com.ft.sdk.garble.bean;

import android.util.Log;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description:日志对象(SDK内部使用)
 */
public class TraceBean extends BaseContentBean {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "TraceBean";
    /**
     * 用于链路日志，表示当前 span 的上一个 span的 ID
     */
    String parentID;
    /**
     * 用于链路日志，表示当前 span 的 ID
     */
    String spanID;
    /**
     * 用于链路日志，表示当前链路的 ID
     */
    String traceID;
    /**
     * 字符串类型，true 表示该 span 的请求响应是错误,false 或者无该标签，表示该 span 的响应是正常的请求
     */
    String status;
    /**
     * span 的类型，目前支持 2 个值：entry 和 local，entry span 表示该 span 的调用的是服务的入口，
     * 即该服务的对其他服务提供调用请求的端点，几乎所有服务和消息队列消费者都是 entry span，
     * 因此只有 span 是 entry 类型的调用才是一个独立的请求。 local span 表示该 span 和远程调用没有任何关系，
     * 只是程序内部的函数调用，例如一个普通的 Java 方法，默认值 entry
     */
    String spanType;
    /**
     * 请求的目标地址，客户端用于访问目标服务的网络地址(但不一定是 IP + 端口)，例如 127.0.0.1:8080 ,默认：null
     */
    String endpoint;
    /**
     * 用于链路日志，表示当前 span 操作名，也可理解为 span 名称
     */
    String operationName;

    /**
     * trace 执行时间
     */
    long duration;


    public TraceBean(String measurement, String content, long time) {
        super(measurement, content, time);
    }

    public TraceBean(String content, long time) {
        super(content, time);
    }

    public TraceBean(String measurement, JSONObject json, long time) {
        super(measurement, json, time);
    }

    public TraceBean(JSONObject json, long time) {
        super(json, time);
    }


    /**
     * 获取所有链路中指标数据
     *
     * @return
     */
    public JSONObject getAllFields() {
        super.getAllFields();
        try {
            if (duration > 0) {
                fields.put("duration", duration);
            }
        } catch (JSONException e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        }
        return fields;
    }

    /**
     * 获取所有链路中标签数据
     *
     * @return
     */
    public JSONObject getAllTags() {
        super.getAllTags();
        try {
            tags.put("type", "custom");

            if (!Utils.isNullOrEmpty(parentID)) {
                tags.put("parent_id", parentID);
            }
            if (!Utils.isNullOrEmpty(operationName)) {
                tags.put("operation", operationName);
                tags.put("resource", operationName);
            }
            if (!Utils.isNullOrEmpty(spanID)) {
                tags.put("span_id", spanID);
            }
            if (!Utils.isNullOrEmpty(traceID)) {
                tags.put("trace_id", traceID);
            }
            if (!Utils.isNullOrEmpty(status)) {
                tags.put("status", status);
            }
            if (!Utils.isNullOrEmpty(spanType)) {
                tags.put("span_type", spanType);
            }
            if (!Utils.isNullOrEmpty(endpoint)) {
                tags.put("endpoint", endpoint);
            }

        } catch (JSONException e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }

        return tags;
    }


    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    public void setParentID(String parentID) {
        this.parentID = parentID;
    }


    public void setSpanID(String spanID) {
        this.spanID = spanID;
    }


    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }


    /**
     * @param status 字符串类型，true 表示该 span 的请求响应是错误,false 或者无该标签，表示该 span 的响应是正常的请求
     */
    public void setStatus(String status) {
        this.status = status;
    }


    public void setSpanType(String spanType) {
        this.spanType = spanType;
    }


    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
