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

    /**
     * Returns the custom OkHttp trace header handler, or null when the default handler is used.
     */
    public FTTraceInterceptor.HeaderHandler getOkHttpTraceHeaderHandler() {
        return headerHandler;
    }

    /**
     * Sets custom trace header handler for OkHttp. Prefer overriding
     * {@link FTTraceInterceptor.HeaderHandler#getTraceContext(okhttp3.Request)}
     * for cleaner integration; legacy {@link FTTraceInterceptor.HeaderHandler#getTraceHeader(okhttp3.Request)}
     * + getTraceID/getSpanID is still supported.
     * ASM may set global HeaderHandler, not set by default.
     *
     * @param headerHandler custom handler, or null to use default
     * @return this config for chaining
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

    /**
     * Returns the trace sampling rate.
     */
    public float getSamplingRate() {
        return samplingRate;
    }

    /**
     * Sets the trace sampling rate.
     *
     * @param samplingRate sampling rate in the range [0, 1]
     * @return this config for chaining
     */
    public FTTraceConfig setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    /**
     * Returns the configured trace header propagation type.
     */
    public TraceType getTraceType() {
        return traceType;
    }

    /**
     * <a href="https://docs.guance.com/real-user-monitoring/explorer/resource/">Explorer Resource</a> function, used to trace the Trace chain of problematic Resource data
     *
     * @param traceType trace header propagation type
     * @return this config for chaining
     */

    public FTTraceConfig setTraceType(TraceType traceType) {
        this.traceType = traceType;
        return this;
    }

    /**
     * Returns whether trace ids are linked to RUM resource data.
     */
    public boolean isEnableLinkRUMData() {
        return enableLinkRUMData;
    }

    /**
     * Sets whether trace ids should be linked to RUM resource data.
     *
     * @param enableLinkRUMData true to attach trace metadata to RUM resources
     * @return this config for chaining
     */
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
//     * @param traceContentType supported response content types
//     * @return this config for chaining
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
     * @return content types supported for trace response body handling
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

    /**
     * Returns whether legacy WebView trace injection is enabled.
     *
     * @deprecated Use automatic trace configuration instead.
     */
    @Deprecated
    public boolean isEnableWebTrace() {
        return enableWebTrace;
    }

    /**
     * Sets whether legacy WebView trace injection is enabled.
     *
     * @param enableWebTrace true to enable legacy WebView trace injection
     * @return this config for chaining
     * @deprecated Use automatic trace configuration instead.
     */
    @Deprecated
    public FTTraceConfig setEnableWebTrace(boolean enableWebTrace) {
        this.enableWebTrace = enableWebTrace;
        return this;
    }


    /**
     * {@link #enableAutoTrace}
     *
     * @param enableAutoTrace true to automatically inject trace headers for supported requests
     * @return this config for chaining
     */
    public FTTraceConfig setEnableAutoTrace(boolean enableAutoTrace) {
        this.enableAutoTrace = enableAutoTrace;
        return this;
    }

    /**
     * Returns global trace attributes configured on this object.
     */
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
