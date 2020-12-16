package com.ft.sdk;

public enum TraceType {

    ZIPKIN,
    JAEGER;
//    public static int SKYWALKING_V3 = 2;
//    public static int SKYWALKING_V2 = 3;


    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
