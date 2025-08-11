package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

/**
 * Link type
 * <p>
 * Used for {@link FTTraceConfig#setTraceType(TraceType)}, used to set http request link using Header propagation
 *
 * @author Brandon
 */
public enum TraceType {
    /**
     * datadog trace, default value, {@link FTTraceConfig#traceType}
     * <p>
     * x-datadog-trace-id
     * x-datadog-parent-id
     * x-datadog-sampling-priority
     * x-datadog-origin 
     *
     * <a href="https://docs.datadoghq.com/synthetics/apm/">Learn more</a>
     */
    DDTRACE,

    /**
     * zipkin multi header
     * <p>
     * X-B3-TraceId
     * X-B3-SpanId
     * X-B3-Sampled
     *
     * <a href="https://github.com/openzipkin/b3-propagation">Learn more</a>
     */
    ZIPKIN_MULTI_HEADER,

    /**
     * zipkin single header,b3
     *
     * <a href="https://github.com/openzipkin/b3-propagation">Learn more</a>
     */
    ZIPKIN_SINGLE_HEADER,

    /**
     * w3c, traceparent
     *
     * <a href="https://www.w3.org/TR/trace-context/#traceparent-header-field-values">Learn more</a>
     */
    TRACEPARENT,

    /** 
     * skywalking 8.0+, sw-8
     *
     * <a href="https://skywalking.apache.org/docs/main/next/en/api/x-process-propagation-headers-v3/#skywalking-cross-process-propagation-headers-protocol">Learn more</a>
     */
    SKYWALKING,

    /**
     * jaeger, header uber-trace-id
     */
    JAEGER;

    /**
     * @return Trace type lowercase character type, such as zipkin, ddtrace, etc.
     * Used for line protocol data output {@link Constants#MEASUREMENT}
     */
    @NonNull
    @Override
    public String toString() {
        switch (this) {
            case ZIPKIN_SINGLE_HEADER:
            case ZIPKIN_MULTI_HEADER:
                //zipkin multi header and single header output the same character
                return "zipkin";
            default:
                return super.toString().toLowerCase();
        }

    }

    public String value() {
        return super.toString().toLowerCase();
    }
}
