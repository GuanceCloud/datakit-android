package com.ft.sdk.garble.bean;

import java.util.HashMap;
import java.util.List;

/**
 * Resource 请求参数
 */
public class ResourceParams {
    /**
     * http 请求地址
     */
    public String url = "";

    /**
     * http 请求头，如果为空则会读取 {@link #requestHeaderMap}
     */
    public String requestHeader = "";
    /**
     * http 返回头,如果为空则会读取 {@link #responseHeaderMap}
     */
    public String responseHeader = "";

    /**
     * http 请求头
     */
    public HashMap<String, List<String>> requestHeaderMap;


    /**
     * http 返回头
     */
    public HashMap<String, List<String>> responseHeaderMap;
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
     * response 内容大小
     */
    public long responseContentLength = 0;
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

    /**
     *  请求错误
     */
    public String requestErrorStack ="";

    /**
     * 附加属性参数
     */
    public HashMap<String, Object> property;
}
