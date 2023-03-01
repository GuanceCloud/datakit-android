package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;

/**
 * 链路类型
 *
 * 用于 {@link FTTraceConfig#setTraceType(TraceType)} 使用，用于设置 http 请求链路使用 Header propagation
 *
 * @author Brandon
 */
public enum TraceType {
    /**
     * datadog trace, 默认数值，{@link FTTraceConfig#traceType}
     * <p>
     * x-datadog-trace-id
     * x-datadog-parent-id
     * x-datadog-sampling-priority
     * x-datadog-origin
     */
    DDTRACE,

    /**
     * zipkin multi header
     * <p>
     * X-B3-TraceId
     * X-B3-SpanId
     * X-B3-Sampled
     *
     */
    ZIPKIN_MULTI_HEADER,

    /**
     * zipkin single header,b3
     */
    ZIPKIN_SINGLE_HEADER,

    /**
     * w3c, traceparent
     */
    TRACEPARENT,

    /**
     * skywalking 8.0+, sw-8
     */
    SKYWALKING,

    /**
     * jaeger, header uber-trace-id
     */
    JAEGER;

    /**
     * @return Trace 类型小写字符类型，例如 zipkin，ddtrace 等。
     * 用于行协议数据输出 {@link Constants#MEASUREMENT}使用
     */
    @Override
    public String toString() {
        switch (this) {
            case ZIPKIN_SINGLE_HEADER:
            case ZIPKIN_MULTI_HEADER:
                //zipkin 多头与单头参数输出同一个字符
                return "zipkin";
            default:
                return super.toString().toLowerCase();
        }

    }
}
