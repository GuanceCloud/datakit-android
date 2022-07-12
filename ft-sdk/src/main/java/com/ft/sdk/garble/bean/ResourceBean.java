package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

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
    public String resourceUrlQuery = "";
    public String traceId = "";
    public String spanId = "";
    public String errorStack = "";

    public int resourceStatus = -1;

    public long resourceSize = -1;
    public long resourceLoad = -1;
    public long resourceDNS = -1;
    public long resourceTCP = -1;
    public long resourceSSL = -1;
    public long resourceTTFB = -1;
    public long resourceTrans = -1;
    public long resourceFirstByte = -1;

    public long startTime = Utils.getCurrentNanoTime();
    public long endTime = -1;

    public String sessionId;
    public String viewId;
    public String viewName;
    public String viewReferrer;
    public String actionId;
    public String actionName;

    public boolean netStateSet = false;
    public boolean contentSet = false;


}
