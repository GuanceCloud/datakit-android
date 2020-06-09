package com.ft.sdk.garble.bean;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * create: by huangDianHua
 * time: 2020/6/5 15:08:45
 * description:日志对象
 */
public class LogBean {
    String measurement;
    String clazz;
    String source;
    String serviceName;
    String env;
    Status status = Status.INFO;
    String parentID;
    String operationName;
    String spanID;
    String traceID;
    String errorCode;
    String content;
    String duration;
    long time;
    JSONObject tags;
    JSONObject fields;
    public LogBean(String measurement,String content,long time){
        this.measurement = measurement;
        this.content = content;
        this.time = time;
    }

    public JSONObject getAllFields(){
        if(fields == null){
            fields = new JSONObject();
        }
        try {
            fields.put("__content",content);
            if(!Utils.isNullOrEmpty(duration)){
                fields.put("__duration",duration);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fields;
    }

    public JSONObject getAllTags(){
        if(tags == null){
            tags = new JSONObject();
        }
        try {
            if(!Utils.isNullOrEmpty(clazz)) {//目前只支持tracing
                tags.put("__class", "tracing");
            }
            if(!Utils.isNullOrEmpty(source)) {
                tags.put("__source",source);
            }
            if(!Utils.isNullOrEmpty(serviceName)){
                tags.put("__serviceName",serviceName);
            }
            if(!Utils.isNullOrEmpty(env)){
                tags.put("__env",env);
            }
            tags.put("__status",status.name);

            if(!Utils.isNullOrEmpty(parentID)){
                tags.put("__parentID",parentID);
            }
            if(!Utils.isNullOrEmpty(operationName)){
                tags.put("__operationName",operationName);
            }
            if(!Utils.isNullOrEmpty(spanID)){
                tags.put("__spanID",spanID);
            }
            if(!Utils.isNullOrEmpty(traceID)){
                tags.put("__traceID",traceID);
            }
            if(!Utils.isNullOrEmpty(errorCode)){
                tags.put("__errorCode",errorCode);
            }
            if(!tags.has("device_uuid")){
                tags.put("device_uuid", DeviceUtils.getUuid(FTApplication.getApplication()));
            }
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

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getSpanID() {
        return spanID;
    }

    public void setSpanID(String spanID) {
        this.spanID = spanID;
    }

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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
}
