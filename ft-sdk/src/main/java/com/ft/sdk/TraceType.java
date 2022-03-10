package com.ft.sdk;

public enum TraceType {
    DDTRACE,
    ZIPKIN_MULTI_HEADER,
    ZIPKIN_SINGLE_HEADER,
    TRACEPARENT,
    SKYWALKING,
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
