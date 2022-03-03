package com.ft.sdk;

public enum TraceType {
    DDTRACE,
    ZIPKIN,
    SKYWALKING_V3,
    JAEGER;


    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
