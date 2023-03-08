package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-06 19:51
 * Description:数据请求类型，用于项目内部
 */
public enum  RequestMethod {
    /**
     * http get 请求
     */
    GET("GET"),

    /**
     * http post 请求
     */
    POST("POST");
    final String method;
    RequestMethod(String method){
        this.method = method;
    }
}
