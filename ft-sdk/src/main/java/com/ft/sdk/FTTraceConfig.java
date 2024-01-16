package com.ft.sdk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Brandon
 */
public class FTTraceConfig {
    /**
     * 采样率，[0,1]
     */
    private float samplingRate = 1;
    /**
     * 追踪类型链路
     */
    private TraceType traceType = TraceType.DDTRACE;
    /**
     * webview 是否开启链路追踪
     */
    private boolean enableWebTrace = false;
    /**
     * 是否开启自动 http
     */
    private boolean enableAutoTrace = false;
    /**
     * 是否与 RUM 数据关联
     */
    private boolean enableLinkRUMData = false;

    /**
     * 设置全局 tag
     */
    private final HashMap<String, Object> globalContext = new HashMap<>();


    private final List<String> traceContentType = Arrays.asList("application/json",
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

    public TraceType getTraceType() {
        return traceType;
    }

    /**
     * <a href="https://docs.guance.com/real-user-monitoring/explorer/resource/">查看器 Resource</a>功能，
     * 用于追查一些有问题 Resource 数据的 Trace 链路追查
     *
     * @param traceType
     * @return
     */

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

//    /**
//     * 设置支持的采集类型
//     *
//     * @param traceContentType
//     * @return
//     */
//    public FTTraceConfig setTraceContentType(List<String> traceContentType) {
//        if (traceContentType != null) {
//            this.traceContentType = traceContentType;
//        }
//        return this;
//    }


    /**
     * 获取链路支持
     *
     * @return
     */
    public List<String> getTraceContentType() {
        return traceContentType;
    }

    /**
     * {@link #enableAutoTrace}
     */
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


    /**
     * {@link #enableAutoTrace}
     *
     * @param enableAutoTrace
     * @return
     */
    public FTTraceConfig setEnableAutoTrace(boolean enableAutoTrace) {
        this.enableAutoTrace = enableAutoTrace;
        return this;
    }


    public HashMap<String, Object> getGlobalContext() {
        return globalContext;
    }
}
