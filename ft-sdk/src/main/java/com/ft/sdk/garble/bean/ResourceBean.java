package com.ft.sdk.garble.bean;

public class ResourceBean {
    public String url = "";
    public String urlHost = "";
    public String urlPath = "";
    public String resourceType = "";
    public String requestHeader = "";
    public String responseHeader = "";
    public String responseConnection = "";
    public String responseContentType = "";
    public String responseContentEncoding = "";
    public String resourceMethod = "";
    public String resourceUrlQuery ="";

    public int resourceStatus = -1;

    public long resourceSize = -1;
    public long resourceLoad = -1;
    public long resourceDNS = -1;
    public long resourceTCP = -1;
    public long resourceSSL = -1;
    public long resourceTTFB = -1;
    public long resourceTrans = -1;
    public long resourceFirstByte =-1;

    public String sessionId;
    public String viewId;
    public String viewName;
    public String viewReferrer;
    public String actionId;
    public String actionName;

    public void reset() {
        url = "";
        urlHost = "";
        urlPath = "";
        resourceType = "";
        requestHeader = "";
        responseHeader = "";
        responseConnection = "";
        responseContentType = "";
        responseContentEncoding = "";
        resourceMethod = "";
        resourceUrlQuery = "";

        resourceStatus = -1;

        resourceSize = -1;
        resourceLoad = -1;
        resourceDNS = -1;
        resourceTCP = -1;
        resourceSSL = -1;
        resourceTTFB = -1;
        resourceTrans = -1;
        resourceFirstByte = -1;

        sessionId = "";
        viewId = "";
        viewName = "";
        viewReferrer = "";
        actionId = "";
        actionName = "";
    }

}
