package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Resource 指标数据，{@link ResourceParams},{@link  NetStatusBean}
 *
 * @author Brandon
 */
public class ResourceBean {

    /**
     * 请求资源地址
     */
    public String url = "";

    /**
     * 请求主机地址
     */
    public String urlHost = "";

    /**
     * url path
     */
    public String urlPath = "";

    /**
     * 请求 Http 头 Content-Type
     */
    public String resourceType = "";
    /**
     * 请求 Http 头
     */
    public String requestHeader = "";
    /**
     * 请求返回头
     */
    public String responseHeader = "";
    /**
     * Http 返回头 Connection
     */
    public String responseConnection = "";

    /**
     * Http 返回 Content Type
     */
    public String responseContentType = "";

    /**
     * Http 返回 Content-Encoding
     */
    public String responseContentEncoding = "";

    /**
     * Http 请求方法 GET POST 等
     */
    public String resourceMethod = "";

    /**
     * Http url query
     */
    public String resourceUrlQuery = "";

    /**
     * 链路 ID {@link TraceBean#traceID}
     */
    public String traceId = "";

    /**
     * 链路 Span ID {@link TraceBean#spanID}
     */
    public String spanId = "";

    /**
     * 请求错误日志堆栈
     */
    public String errorStack = "";

    /**
     * 请求返回 code {@link  HttpsURLConnection#HTTP_OK,HttpsURLConnection#HTTP_UNAUTHORIZED} 等等
     */
    public int resourceStatus = -1;

    /**
     * 资源请求大小，单位 byte
     */
    public long resourceSize = -1;

    /**
     * 资源加载时间 单位
     */
    public long resourceLoad = -1;

    /**
     * dns 解析时长， {@link NetStatusBean#getDNSTime()}
     */
    public long resourceDNS = -1;

    /**
     * tcp 连接时长，{@link NetStatusBean#getTcpTime()} }
     */
    public long resourceTCP = -1;

    /**
     * ssl 连接时长，{@link NetStatusBean#getSSLTime()}  }
     */
    public long resourceSSL = -1;

    /**
     * TTFB 时长, {@link NetStatusBean#getTTFB()}}
     */
    public long resourceTTFB = -1;

    /**
     * 请求返回耗时, {@link NetStatusBean#getResponseTime()}
     */
    public long resourceTrans = -1;

    /**
     * 首字节时长, {@link NetStatusBean#getFirstByteTime()}
     */
    public long resourceFirstByte = -1;

    /**
     * 资源请求开始时间
     */
    public long startTime = Utils.getCurrentNanoTime();

    /**
     * 资源请求结束时间
     */
    public long endTime = -1;

    /**
     * 会话 ID ,{@link  com.ft.sdk.FTRUMGlobalManager#sessionId}
     */
    public String sessionId;

    /**
     * 页面 ID，{@link ViewBean#id}
     */
    public String viewId;

    /**
     * 页面名称，{@link ViewBean#viewName}
     */
    public String viewName;

    /**
     * 上级页面，{@link  ViewBean#viewReferrer}
     */
    public String viewReferrer;

    /**
     * Action Id，{@link  ActionBean#id}
     */
    public String actionId;

    /**
     * Action 名称，{@link  ActionBean#actionName}
     */
    public String actionName;
    public HashMap<String, Object> property = new HashMap<>();

    /**
     *
     */
    public boolean netStateSet = false;

    /**
     *
     */
    public boolean contentSet = false;


}
