package com.ft.sdk.garble.bean;

/**
 * Resource 请求参数
 */
public class ResourceParams {
    /**
     * http 请求地址
     */
    public String url = "";
    /**
     * http 请求头
     */
    public String requestHeader = "";
    /**
     * http 返回头
     */
    public String responseHeader = "";
    /**
     * http 头 Connection
     */
    public String responseConnection = "";
    /**
     * http 头 Content-Type
     */
    public String responseContentType = "";
    /**
     * http 头 Content-Encoding
     */
    public String responseContentEncoding = "";
    /**
     * http 请求
     */
    public String resourceMethod = "";
    /**
     * http 请求 body
     */
    public String responseBody = "";

    /**
     * http 返回 code
     */
    public int resourceStatus = 0;
}
