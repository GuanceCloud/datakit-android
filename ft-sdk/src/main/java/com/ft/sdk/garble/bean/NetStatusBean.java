package com.ft.sdk.garble.bean;


import java.util.HashMap;

/**
 * Network resource request, resource metric data
 */
public class NetStatusBean {

    /**
     * Request start time @deprecated Use {@link #callStartTime} instead.
     */
    @Deprecated
    public long fetchStartTime = -1;
    /**
     * request header start time @deprecated Use {@link #headerStartTime} instead.
     */
    @Deprecated
    public long requestStartTime = -1;
    /**
     * response start time  @deprecated Use {@link #bodyStartTime} instead.
     */
    @Deprecated
    public long responseStartTime = -1;
    /**
     * response end time {@link #bodyEndTime} instead.
     */
    @Deprecated
    public long responseEndTime = -1;

    /**
     * Request start time from call Start
     */
    public long callStartTime = -1;

    /**
     * Request response header start time
     */
    public long headerStartTime = -1;

    /**
     * Request response header end time
     */
    public long headerEndTime = -1;

    /**
     * tcp connection start time
     */
    public long tcpStartTime = -1;
    /**
     * tcp end time
     */
    public long tcpEndTime = -1;
    /**
     * dns resolution start time
     */
    public long dnsStartTime = -1;
    /**
     * dns resolution end time
     */
    public long dnsEndTime = -1;

    /**
     * response body start time
     */
    public long bodyStartTime = -1;

    /**
     * response body end time
     */
    public long bodyEndTime = -1;


    /**
     * ssl connection start time
     */
    public long sslStartTime = -1;

    /**
     * ssl connection end time
     */
    public long sslEndTime = -1;


    /**
     * host IP address
     */
    public String resourceHostIP = "";

    /**
     * Additional properties
     */
    public HashMap<String, Object> property;

    /**
     * Request host address
     */
    public String requestHost;

    /**
     * Get tcp connection duration
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
     *  Relative connect start time to {@link #callStartTime}
     *
     * @return
     */
    public long getConnectStartTime() {
        return tcpStartTime - callStartTime;
    }

    /**
     * Get dns resolution duration
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
     *  Relative dns start time to {@link #callStartTime}
     *
     * @return
     */
    public long getDNSStartTime() {
        return dnsStartTime - callStartTime;
    }


    /**
     *  body content load duration
     * @return
     */
    public long getDownloadTime() {
        if (bodyEndTime > bodyStartTime) {
            return bodyEndTime - bodyStartTime;
        }
        return 0;
    }

    /**
     *  Relative body download start time to {@link #callStartTime}
     * @return
     */
    public long getDownloadTimeStart() {
        return bodyStartTime - callStartTime;
    }

    /**
     * Get request response duration
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
     * Get ttfb duration
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
     * First byte duration
     *
     * @return
     */
    public long getFirstByteTime() {
        return getTTFB();
    }


    /**
     *  Relative first byte start time to {@link #callStartTime}
     * @return
     */
    public long getFirstByteStartTime() {
        return headerStartTime - callStartTime;
    }

    /**
     * Whole request duration
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
     * ssl connection duration
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
     *  Relative ssl start time to {@link #callStartTime}
     *
     * @return
     */
    public long getSslStartTime() {
        return sslStartTime - callStartTime;
    }
}
