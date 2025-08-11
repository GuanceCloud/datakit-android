package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Constants;

import java.util.Arrays;

/**
 * BY huangDianHua
 * DATE:2019-12-02 14:00
 * Description:
 */
public enum OP {
    /**
     * Page click event
     */
    CLK("clk");
    //    FLOW_CHART("flow_chart"),
//    OPEN("open"),
//    OPEN_ACT("opn_act"),
//    OPEN_FRA("open_fra"),
//    CLS_ACT("cls_act"),
//    CLS_FRA("cls_fra");

//    //webview event
//    WEBVIEW_LOADING("webview_loading"),
//    WEBVIEW_LOAD_COMPLETED("webview_load_completed"),
//
//    //client usage event
//    CLIENT_ACTIVATED_TIME("client_activated_time"),
//
//    //network status event
//    HTTP_CLIENT("http_client"),
//    HTTP_WEBVIEW("http_webview"),
//
//    RUM_APP_STARTUP("rum_app_startup"),
//    RUM_APP_VIEW("rum_app_view"),
//    RUM_APP_RESOURCE_PERFORMANCE("rum_app_resource_performance"),
//    RUM_APP_FREEZE("rum_app_freeze");


    public String value;

    OP(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return value;
    }

}
