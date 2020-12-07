package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.FTHttpConfig;

public class NetStatusBean {

    public long fetchStartTime = -1;
    public long tcpStartTime = -1;
    public long tcpEndTime = -1;
    public long dnsStartTime = -1;
    public long dnsEndTime = -1;
    public long responseStartTime = -1;
    public long responseEndTime = -1;
    public long sslStartTime = -1;
    public long sslEndTime = -1;

    public String requestHost;

    /**
     * 判断是否是 SDK 内部发出的请求
     *
     * @return
     */
    public boolean isInnerRequest() {
        String innerUrl = FTHttpConfig.get().serverUrl;
        if (innerUrl != null && requestHost != null && innerUrl.contains(requestHost)) {
            return true;
        }
        return false;
    }

    public long getTcpTime() {
        if (tcpEndTime > tcpStartTime) {
            return tcpEndTime - tcpStartTime;
        }
        return 0;
    }

    public long getDNSTime() {
        if (dnsEndTime > dnsStartTime) {
            return dnsEndTime - dnsStartTime;
        }
        return 0;
    }

    public long getResponseTime() {
        if (responseEndTime > responseStartTime) {
            return responseEndTime - responseStartTime;
        }
        return 0;
    }

    public long getTTFB() {
        if (responseStartTime > fetchStartTime) {
            return responseStartTime - fetchStartTime;
        }
        return 0;
    }

    public long getHoleRequestTime() {
        if (responseEndTime > fetchStartTime) {
            return responseEndTime - fetchStartTime;
        }
        return 0;
    }

    public long getSSLTime() {
        if (sslEndTime > sslStartTime) {
            return sslEndTime - sslStartTime;
        }
        return 0;
    }

    public void reset() {
        fetchStartTime = -1;
        tcpStartTime = -1;
        tcpEndTime = -1;
        dnsStartTime = -1;
        dnsEndTime = -1;
        responseStartTime = -1;
        responseEndTime = -1;
        sslStartTime = -1;
        sslEndTime = -1;
        requestHost = null;
    }
}
