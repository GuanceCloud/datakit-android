package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.FTHttpConfig;

public class NetStatusBean {
    public long tcpStartTime;
    public long tcpEndTime;
    public long dnsStartTime;
    public long dnsEndTime;
    public long responseStartTime;
    public long responseEndTime;
    public int requestCount;
    public int requestErrCount;
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
        if (tcpEndTime >= tcpStartTime) {
            long time = tcpEndTime - tcpStartTime;
            if (time > 10 * 1000 || time >= getResponseTime()) {
                return 0;
            }
            return time;
        }
        return 0;
    }

    public long getDNSTime() {
        if (dnsEndTime >= dnsStartTime) {
            long time = dnsEndTime - dnsStartTime;
            if (time > 10 * 1000 || time >= getResponseTime()) {
                return 0;
            }
            return time;
        }
        return 0;
    }

    public long getResponseTime() {
        if (responseEndTime >= responseStartTime) {
            long time = responseEndTime - responseStartTime;
            if (time > 10 * 1000) {
                return 0;
            }
            return time;
        }
        return 0;
    }

    public double getErrorRate() {
        if (requestCount > 0) {
            double rate = requestErrCount * 1.0 / requestCount;
            return Math.floor(rate * 100) / 100.0;
        }
        return 0;
    }
}
