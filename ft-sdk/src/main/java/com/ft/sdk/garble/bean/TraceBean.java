package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description: Log object (for SDK internal use)
 */
public class TraceBean extends BaseContentBean {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "TraceBean";
    /**
     * Used for trace logs, indicates the previous span ID of the current span
     */
    String parentID;
    /**
     * Used for trace logs, indicates the ID of the current span
     */
    String spanID;
    /**
     * Used for trace logs, indicates the ID of the current trace
     */
    String traceID;
    /**
     * String type, true means the response of this span's request is an error,
     * false or no such tag means the response of this span is a normal request
     */
    String status;
    /**
     * The type of span, currently supports 2 values: entry and local. 
     * entry span means this span is the entry point of the service,
     * i.e., the endpoint of the service that provides invocation requests to other services. 
     * Almost all services and message queue consumers are entry spans,
     * so only spans of entry type are considered independent requests. 
     * local span means this span has no relation to remote calls,
     * it's just an internal function call in the program, such as a regular Java method. 
     * Default value is entry.
     */
    String spanType;
    /**
     * Target address of the request. 
     * The network address used by the client to access the target service (not necessarily IP + port),
     *  e.g., 127.0.0.1:8080. Default: null
     */
    String endpoint;
    /**
     * Used for trace logs, indicates the operation name of the current span, can also be understood as the span name
     */
    String operationName;

    /**
     * Trace execution time
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
     * Get all metric data in the trace
     *
     * @return
     */
    public HashMap<String, Object> getAllFields() {
        super.getAllFields();
//        try {
        if (duration > 0) {
            fields.put("duration", duration);
        }
//        } catch (JSONException e) {
//            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
//        }
        return fields;
    }

    /**
     * Get all tag data in the trace
     *
     * @return
     */
    public HashMap<String, Object> getAllTags() {
        super.getAllTags();
//        try {
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

//        } catch (JSONException e) {
//            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
//
//        }

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
     * @param status String type, true means the response of this span's request is an error, 
     *              false or no such tag means the response of this span is a normal request
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
