package com.ft.sdk;

public enum TraceType {
    /**
     * datadog trace
     *
     * x-datadog-trace-id
     * x-datadog-parent-id
     * x-datadog-sampling-priority
     * x-datadog-origin
     */
    DDTRACE,

    /**
     * zipkin multi header
     *
     * X-B3-TraceId
     * X-B3-SpanId
     * X-B3-Sampled
     */
    ZIPKIN_MULTI_HEADER,

    /**
     * zipkin single header b3
     */
    ZIPKIN_SINGLE_HEADER,

    /**
     * w3c traceparent
     */
    TRACEPARENT,

    /**
     * skywalking 8.0+
     */
    SKYWALKING,

    /**
     * jaeger,header uber-trace-id
     */
    JAEGER;

    @Override
    public String toString() {
        switch (this) {
            case ZIPKIN_SINGLE_HEADER:
            case ZIPKIN_MULTI_HEADER:
                return "zipkin";
            default:
                return super.toString().toLowerCase();
        }

    }
}
