package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

/**
 * 链路类型
 * <p>
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
     *
     * <a href="https://docs.datadoghq.com/synthetics/apm/">了解更多</a>
     */
    DDTRACE,

    /**
     * zipkin multi header
     * <p>
     * X-B3-TraceId
     * X-B3-SpanId
     * X-B3-Sampled
     *
     * <a href="https://github.com/openzipkin/b3-propagation">了解更多</a>
     */
    ZIPKIN_MULTI_HEADER,

    /**
     * zipkin single header,b3
     *
     * <a href="https://github.com/openzipkin/b3-propagation">了解更多</a>
     */
    ZIPKIN_SINGLE_HEADER,

    /**
     * w3c, traceparent
     *
     * <a href="https://www.w3.org/TR/trace-context/#traceparent-header-field-values">了解更多</a>
     */
    TRACEPARENT,

    /**
     * skywalking 8.0+, sw-8
     *
     * <a href="https://skywalking.apache.org/docs/main/next/en/api/x-process-propagation-headers-v3/#skywalking-cross-process-propagation-headers-protocol">了解更多</a>
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
    @NonNull
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

    public String value() {
        return super.toString().toLowerCase();
    }
}
