package com.ft.sdk;

public enum TraceType {
    DDTRACE,
    ZIPKIN,
    JAEGER;


    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
