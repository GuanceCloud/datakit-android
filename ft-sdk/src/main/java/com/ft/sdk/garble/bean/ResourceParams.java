package com.ft.sdk.garble.bean;

import java.util.HashMap;
import java.util.List;

/**
 * Resource request parameters
 */
public class ResourceParams {
    /**
     * HTTP request address
     */
    public String url = "";

    /**
     * HTTP request header, if empty will read {@link #requestHeaderMap}
     */
    public String requestHeader = "";
    /**
     * HTTP response header, if empty will read {@link #responseHeaderMap}
     */
    public String responseHeader = "";

    /**
     * HTTP request header
     */
    public HashMap<String, List<String>> requestHeaderMap;


    /**
     * HTTP response header
     */
    public HashMap<String, List<String>> responseHeaderMap;
    /**
     * HTTP header Connection
     */
    public String responseConnection = "";
    /**
     * HTTP header Content-Type
     */
    public String responseContentType = "";
    /**
     * HTTP header Content-Encoding
     */
    public String responseContentEncoding = "";

    /**
     * Response content size
     */
    public long responseContentLength = 0;
    /**
     * HTTP request
     */
    public String resourceMethod = "";
    /**
     * HTTP request body
     */
    public String responseBody = "";

    /**
     * HTTP response code
     */
    public int resourceStatus = 0;

    /**
     *  Request error
     */
    public String requestErrorStack ="";
    /**
     * Error message
     */
    public String requestErrorMsg ="";

    /**
     * Additional property parameters
     */
    public HashMap<String, Object> property;
}
