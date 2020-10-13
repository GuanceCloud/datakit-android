package com.ft.sdk.garble.bean;

/**
 * BY huangDianHua
 * DATE:2019-12-02 14:00
 * Description:
 */
public enum OP {
    //页面事件
    LANC("lanc"),
    CLK("clk"),
    CSTM("cstm"),
//    FLOW_CHART("flow_chart"),
    OPEN("open"),
    OPEN_ACT("opn_act"),
    OPEN_FRA("open_fra"),
    CLS_ACT("cls_act"),
    CLS_FRA("cls_fra"),

    //错误事件
    BLOCK("block"),
    CRASH("crash"),
    ANR("anr"),


    //webview 事件
    WEBVIEW_LOADING("webview_loading"),
    WEBVIEW_LOAD_COMPLETED("webview_load_completed"),

    //客户端使用事件
    CLIENT_ACTIVATED_TIME("client_activated_time"),

    //网络情况事件
    HTTP_CLIENT("http_client"),
    HTTP_WEBVIEW("http_webview");

    public String value;

    OP(String value) {
        this.value = value;
    }
}
