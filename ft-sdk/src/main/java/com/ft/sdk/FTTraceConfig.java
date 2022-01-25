package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FTTraceConfig {
    private float samplingRate = 1;
    private String serviceName = Constants.DEFAULT_LOG_SERVICE_NAME;
    private TraceType traceType = TraceType.DDTRACE;
    private boolean enableWebTrace = false;
    private boolean enableAutoTrace = false;
    private boolean enableLinkRUMData = false;

    //设置全局 tag
    private final HashMap<String, Object> globalContext = new HashMap<>();


    public List<String> traceContentType = Arrays.asList("application/json",
            "application/javascript", "application/xml", "application/x-www-form-urlencoded",
            "text/html", "text/xml", "text/plain",
            "multipart/form-data"
    );

    public float getSamplingRate() {
        return samplingRate;
    }

    public FTTraceConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public FTTraceConfig setServiceName(String serviceName) {
        if (serviceName != null) {
            this.serviceName = serviceName;
        }
        return this;
    }

    public TraceType getTraceType() {
        return traceType;
    }

    public FTTraceConfig setTraceType(TraceType traceType) {
        this.traceType = traceType;
        return this;
    }

    public boolean isEnableLinkRUMData() {
        return enableLinkRUMData;
    }

    public FTTraceConfig setEnableLinkRUMData(boolean enableLinkRUMData) {
        this.enableLinkRUMData = enableLinkRUMData;
        return this;
    }

    /**
     * 设置支持的采集类型
     *
     * @param traceContentType
     * @return
     */
    public FTTraceConfig setTraceContentType(List<String> traceContentType) {
        if (traceContentType != null) {
            this.traceContentType = traceContentType;
        }
        return this;
    }


    public List<String> getTraceContentType() {
        return traceContentType;
    }

    public boolean isEnableAutoTrace() {
        return enableAutoTrace;
    }

    public boolean isEnableWebTrace() {
        return enableWebTrace;
    }

    public FTTraceConfig setEnableWebTrace(boolean enableWebTrace) {
        this.enableWebTrace = enableWebTrace;
        return this;
    }


    public FTTraceConfig setEnableAutoTrace(boolean enableAutoTrace) {
        this.enableAutoTrace = enableAutoTrace;
        return this;
    }

    public FTTraceConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    public HashMap<String, Object> getGlobalContext() {
        return globalContext;
    }
}
