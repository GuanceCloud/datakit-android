package com.ft.sdk.garble.bean;


import java.util.HashMap;

/**
 * 网络资源请求，资源指标数据
 */
public class NetStatusBean {

    /**
     * 请求开始时间
     */
    public long fetchStartTime = -1;


    /**
     * 请求开始时间
     */
    public long requestStartTime = -1;


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
     * 请求返回内容加载开始时间
     */
    public long responseStartTime = -1;
    /**
     * 请求返回内容加载结束时间
     */
    public long responseEndTime = -1;

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
     * 附加属性，
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
     * 获取请求返回时长
     *
     * @return
     */
    public long getResponseTime() {
        if (responseEndTime > responseStartTime) {
            return responseEndTime - responseStartTime;
        }
        return 0;
    }


    /**
     * 获取 ttfb 时时长
     *
     * @return
     */
    public long getTTFB() {
        if (responseStartTime > requestStartTime) {
            return responseStartTime - requestStartTime;
        }
        return 0;
    }

    /**
     * 首字节时间
     *
     * @return
     */
    public long getFirstByteTime() {
        if (responseStartTime > dnsStartTime) {
            if (dnsStartTime > 0) {
                return responseStartTime - dnsStartTime;
            } else {
                return responseStartTime - requestStartTime;
            }
        }
        return 0;
    }

    /**
     * 整个请求请求时长
     *
     * @return
     */
    public long getHoleRequestTime() {
        if (responseEndTime > fetchStartTime) {
            return responseEndTime - fetchStartTime;
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
//
//    public void reset() {
//        fetchStartTime = -1;
//        tcpStartTime = -1;
//        tcpEndTime = -1;
//        dnsStartTime = -1;
//        dnsEndTime = -1;
//        responseStartTime = -1;
//        responseEndTime = -1;
//        sslStartTime = -1;
//        sslEndTime = -1;
//        requestHost = null;
//    }
}
