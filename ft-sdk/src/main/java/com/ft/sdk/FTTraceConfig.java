package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Brandon
 */
public class FTTraceConfig {
    /**
     * Sampling rate, [0,1]
     */
    private float samplingRate = 1;
    /**
     * Trace type chain
     */
    private TraceType traceType = TraceType.DDTRACE;
    /**
     * Whether WebView enables chain tracing
     */
    private boolean enableWebTrace = false;
    /**
     * Whether to enable automatic HTTP
     */
    private boolean enableAutoTrace = false;
    /**
     * Whether to associate with RUM data
     */
    private boolean enableLinkRUMData = false;

    /**
     * Service name {@link Constants#KEY_SERVICE }, default is {@link Constants#DEFAULT_SERVICE_NAME}
     */
    String serviceName = Constants.DEFAULT_SERVICE_NAME;

    /**
     *
     */
    private FTTraceInterceptor.HeaderHandler headerHandler;


    public FTTraceInterceptor.HeaderHandler getOkHttpTraceHeaderHandler() {
        return headerHandler;
    }

    /**
     * ASM sets global {@link FTTraceInterceptor.HeaderHandler}, not set by default
     *
     * @param headerHandler
     * @return
     */
    public FTTraceConfig setOkHttpTraceHeaderHandler(FTTraceInterceptor.HeaderHandler headerHandler) {
        this.headerHandler = headerHandler;
        return this;
    }

    /**
     * Set global tag
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
     * <a href="https://docs.guance.com/real-user-monitoring/explorer/resource/">Explorer Resource</a> function, used to trace the Trace chain of problematic Resource data
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

    void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    //    /**
//     * Set supported collection types
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
     * Get link support
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

    @Deprecated
    public boolean isEnableWebTrace() {
        return enableWebTrace;
    }


    @Deprecated
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

    @Override
    public String toString() {
        return "FTTraceConfig{" +
                "samplingRate=" + samplingRate +
                ", traceType=" + traceType +
                ", enableWebTrace=" + enableWebTrace +
                ", enableAutoTrace=" + enableAutoTrace +
                ", enableLinkRUMData=" + enableLinkRUMData +
                ", serviceName='" + serviceName + '\'' +
                ", headerHandler=" + headerHandler +
                ", globalContext=" + globalContext +
                '}';
    }
}
