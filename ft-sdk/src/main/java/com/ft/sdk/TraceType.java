package com.ft.sdk;

public enum TraceType {
    DDTRACE,
    ZIPKIN,
    SKYWALKING_V3,
    JAEGER;

    @Override
    public String toString() {
        if (this.equals(SKYWALKING_V3)) {
            return "skywalking";
        }
        return super.toString().toLowerCase();
    }
}
