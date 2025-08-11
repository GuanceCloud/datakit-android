package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-06 19:51
 * Description: Data request type, used internally in the project
 */
public enum  RequestMethod {
    /**
     * http get request
     */
    GET("GET"),

    /**
     * http post request
     */
    POST("POST");
    final String method;
    RequestMethod(String method){
        this.method = method;
    }
}
