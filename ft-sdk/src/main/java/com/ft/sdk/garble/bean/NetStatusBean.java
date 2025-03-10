package com.ft.sdk.garble.bean;


import java.util.HashMap;

/**
 * 网络资源请求，资源指标数据
 */
public class NetStatusBean {

    /**
     * 请求开始时间 @deprecated Use {@link #callStartTime} instead.
     */
    @Deprecated
    public long fetchStartTime = -1;
    /**
     * request header 开始时间 @deprecated Use {@link #headerStartTime} instead.
     */
    @Deprecated
    public long requestStartTime = -1;
    /**
     * response 开始时间  @deprecated Use {@link #bodyStartTime} instead.
     */
    @Deprecated
    public long responseStartTime = -1;
    /**
     * response 结束时间 {@link #bodyEndTime} instead.
     */
    @Deprecated
    public long responseEndTime = -1;

    /**
     * 请求从 call Start 处开始时间
     */
    public long callStartTime = -1;

    /**
     * 请求在 response header 开始时间
     */
    public long headerStartTime = -1;

    /**
     * 请求在 response header 结束时间
     */
    public long headerEndTime = -1;

    /**
     * tcp 连接时间
     */
    public long tcpStartTime = -1;
    /**
     * tcp 结束时间
     */
    public long tcpEndTime = -1;
    /**
     * dns 解析开始时间
     */
    public long dnsStartTime = -1;
    /**
     * dns 解析结束时间
     */
    public long dnsEndTime = -1;

    /**
     * response body 开始时间
     */
    public long bodyStartTime = -1;

    /**
     * response body 结束时间
     */
    public long bodyEndTime = -1;


    /**
     * ssl 连接开始时间
     */
    public long sslStartTime = -1;

    /**
     * ssl 连接结束时间
     */
    public long sslEndTime = -1;


    /**
     * host IP 地址
     */
    public String resourceHostIP = "";

    /**
     * 附加属性
     */
    public HashMap<String, Object> property;

    /**
     * 请求主机地址
     */
    public String requestHost;

    /**
     * 获取 tcp 连接时长
     *
     * @return
     */
    public long getTcpTime() {
        if (tcpEndTime > tcpStartTime) {
            return tcpEndTime - tcpStartTime;
        }
        return 0;
    }

    /**
     *  相对 {@link #callStartTime} 的 connect 开始时间
     *
     * @return
     */
    public long getConnectStartTime() {
        return tcpStartTime - callStartTime;
    }

    /**
     * 获取 dns 解析时长
     *
     * @return
     */
    public long getDNSTime() {
        if (dnsEndTime > dnsStartTime) {
            return dnsEndTime - dnsStartTime;
        }
        return 0;
    }

    /**
     *  相对 {@link #callStartTime} 的 dns 开始时间
     *
     * @return
     */
    public long getDNSStartTime() {
        return dnsStartTime - callStartTime;
    }


    /**
     *  body 内容加载耗时
     * @return
     */
    public long getDownloadTime() {
        if (bodyEndTime > bodyStartTime) {
            return bodyEndTime - bodyStartTime;
        }
        return 0;
    }

    /**
     * 相对 {@link #callStartTime} 的 body 下载开始时间
     * @return
     */
    public long getDownloadTimeStart() {
        return bodyStartTime - callStartTime;
    }

    /**
     * 获取请求返回时长
     *
     * @return
     */
    public long getResponseTime() {
        if (bodyEndTime > headerStartTime) {
            return bodyEndTime - headerStartTime;
        }
        return 0;
    }


    /**
     * 获取 ttfb 时时长
     *
     * @return
     */
    public long getTTFB() {
        if (headerEndTime > headerStartTime) {
            return headerEndTime - headerStartTime;
        }
        return 0;
    }

    /**
     * 首字节耗时
     *
     * @return
     */
    public long getFirstByteTime() {
        return getTTFB();
    }


    /**
     * 相对 {@link #callStartTime} 的首字节开始时间
     * @return
     */
    public long getFirstByteStartTime() {
        return headerStartTime - callStartTime;
    }

    /**
     * 整个请求请求时长
     *
     * @return
     */
    public long getHoleRequestTime() {
        if (bodyEndTime > callStartTime) {
            return bodyEndTime - callStartTime;
        }
        return 0;
    }

    /**
     * ssl 连接时长
     *
     * @return
     */

    public long getSSLTime() {
        if (sslEndTime > sslStartTime) {
            return sslEndTime - sslStartTime;
        }
        return 0;
    }

    /**
     * 相对 {@link #callStartTime} 的 ssl 开始时间
     *
     * @return
     */
    public long getSslStartTime() {
        return sslStartTime - callStartTime;
    }
}
