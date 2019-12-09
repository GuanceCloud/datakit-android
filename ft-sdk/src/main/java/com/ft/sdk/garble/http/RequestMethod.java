package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-06 19:51
 * Description:
 */
public enum  RequestMethod {
    GET("GET"),POST("POST");
    String method;
    RequestMethod(String method){
        this.method = method;
    }
}
