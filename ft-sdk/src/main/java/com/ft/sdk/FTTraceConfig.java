package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;

import java.util.List;

public class FTTraceConfig {
    private float samplingRate = 1;
    private String serviceName = Constants.DEFAULT_LOG_SERVICE_NAME;
    private TraceType traceType = TraceType.ZIPKIN;
    private boolean enableLinkRUMData = false;
    private List<String> traceContentType;
    private boolean isNetworkTrace = true;

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
        this.traceContentType = traceContentType;
        return this;
    }


    public List<String> getTraceContentType() {
        return traceContentType;
    }

    public boolean isNetworkTrace() {
        return isNetworkTrace;
    }

    public FTTraceConfig setNetworkTrace(boolean networkTrace) {
        isNetworkTrace = networkTrace;
        return this;
    }
}
