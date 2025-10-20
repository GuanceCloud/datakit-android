package com.ft.sdk.garble.bean;

import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Resource metric data, {@link ResourceParams},{@link  NetStatusBean}
 *
 * @author Brandon
 */
public class ResourceBean {

    /**
     * request uuid
     */
    public String id = "";
    /**
     * Request resource address
     */
    public String url = "";

    /**
     * Request host address
     */
    public String urlHost = "";

    /**
     * url path
     */
    public String urlPath = "";

    /**
     * Request HTTP header Content-Type
     */
    public String resourceType = "";
    /**
     * Request HTTP header
     */
    public String requestHeader = "";
    /**
     * Request response header
     */
    public String responseHeader = "";
    /**
     * HTTP response header Connection
     */
    public String responseConnection = "";

    /**
     * HTTP response Content Type
     */
    public String responseContentType = "";

    /**
     * HTTP response Content-Encoding
     */
    public String responseContentEncoding = "";

    /**
     * HTTP request method GET POST etc.
     */
    public String resourceMethod = "";

    /**
     * HTTP url query
     */
    public String resourceUrlQuery = "";

    /**
     * Trace ID {@link TraceBean#traceID}
     */
    public String traceId = null;

    /**
     * Trace Span ID {@link TraceBean#spanID}
     */
    public String spanId = null;

    /**
     * Request error log stack
     */
    public String errorStack = "";

    /**
     * Error message
     */
    public String errorMsg = "";

    /**
     * Request response code {@link  HttpsURLConnection#HTTP_OK,HttpsURLConnection#HTTP_UNAUTHORIZED} etc.
     */
    public int resourceStatus = 0;

    /**
     * Resource request size, unit byte
     */
    public long resourceSize = -1;

    /**
     * Resource load time unit
     */
    public long resourceLoad = -1;

    /**
     * DNS resolution duration, {@link NetStatusBean#getDNSTime()}
     */
    public long resourceDNS = -1;

    /**
     * DNS resolution start time, {@link NetStatusBean#getDNSStartTime()}
     */
    public long resourceDNSStart = -1;

    /**
     * TCP connection duration, {@link NetStatusBean#getTcpTime()} }
     */
    public long resourceTCP = -1;

    /**
     * TCP connection start time {@link  NetStatusBean#getConnectStartTime()}
     */
    public long resourceTCPStart = -1;

    /**
     * SSL connection duration, {@link NetStatusBean#getSSLTime()}  }
     */
    public long resourceSSL = -1;

    /**
     * SSL connection start time, {@link NetStatusBean#getSslStartTime()}
     */
    public long resourceSSLStart = -1;

    /**
     * TTFB duration, {@link NetStatusBean#getTTFB()}}
     */
    public long resourceTTFB = -1;

    /**
     * Request response time, {@link NetStatusBean#getResponseTime()}
     */
    public long resourceTrans = -1;

    /**
     * First byte duration, {@link NetStatusBean#getFirstByteTime()}
     */
    public long resourceFirstByte = -1;
    /**
     * First byte start time, {@link NetStatusBean#getFirstByteStartTime()}
     */
    public long resourceFirstByteStart = -1;

    /**
     * Resource download time, {@link NetStatusBean#getDownloadTime()}
     */
    public long resourceDownloadTime = -1;
    /**
     * Resource download start time, {@link NetStatusBean#getDownloadTimeStart()}
     */
    public long resourceDownloadTimeStart = -1;


    /**
     * Host IP address
     */
    public String resourceHostIP = "";


    /**
     * Resource request start time
     */
    public long startTime = Utils.getCurrentNanoTime();

    /**
     * Resource request end time
     */
    public long endTime = -1;

    /**
     * Session ID, {@link  FTRUMInnerManager#sessionId}
     */
    public String sessionId;

    /**
     * Page ID, {@link ViewBean#id}
     */
    public String viewId;

    /**
     * Page name, {@link ViewBean#viewName}
     */
    public String viewName;

    /**
     * Parent page, {@link  ViewBean#viewReferrer}
     */
    public String viewReferrer;

    /**
     * Action ID, {@link  ActionBean#id}
     */
    public String actionId;

    /**
     * Action name, {@link  ActionBean#actionName}
     */
    public String actionName;
    public HashMap<String, Object> property = new HashMap<>();

    /**
     * Whether resource metrics are set
     */
    public boolean netStateSet = false;

    /**
     * Whether resource content related is set
     */
    public boolean contentSet = false;

    /**
     * Whether there is session replay
     */
    public boolean hasSessionReplay = false;

}
