package com.ft.sdk;

public enum TraceType {
    DDTRACE,
    ZIPKIN_MULTI_HEADER,
    ZIPKIN_SINGLE_HEADER,
    W3C_TRACEPARENT,
    SKYWALKING_V3,
    JAEGER;

    @Override
    public String toString() {
        switch (this) {
            case SKYWALKING_V3:
                return "skywalking";
            case ZIPKIN_SINGLE_HEADER:
            case ZIPKIN_MULTI_HEADER:
                return "zipkin";
            case W3C_TRACEPARENT:
                return "traceparent";
            default:
                return super.toString().toLowerCase();
        }

    }
}
