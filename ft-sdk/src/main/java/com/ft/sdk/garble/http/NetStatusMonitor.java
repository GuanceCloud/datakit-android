package com.ft.sdk.garble.http;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 */
public class NetStatusMonitor extends EventListener {

    private long tcpStartTime;
    private long tcpEndTime;
    private long dnsStartTime;
    private long dnsEndTime;
    private long responseStartTime;
    private long responseEndTime;
    private int requestCount;
    private int requestErrCount;
    private String requestHost;

    @Override
    public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
        super.requestHeadersEnd(call, request);
        StringBuilder sb = new StringBuilder();
        sb.append(request.headers().toString());
        LogUtils.d("request-header:\n" + sb.toString());
    }

    @Override
    public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
        super.responseHeadersEnd(call, response);
        StringBuilder sb = new StringBuilder();
        sb.append(response.headers().toString());
        if (response.body() != null) {
            try {
                sb.append(response.body().string()).append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogUtils.d("response-header:\n" + sb.toString());
    }

    @Override
    public void callEnd(@NotNull Call call) {
        super.callEnd(call);
        responseEndTime = System.currentTimeMillis();

        NetStatusBean netStatusBean = new NetStatusBean();
        netStatusBean.dnsStartTime = dnsStartTime;
        netStatusBean.dnsEndTime = dnsEndTime;
        netStatusBean.responseStartTime = responseStartTime;
        netStatusBean.responseEndTime = responseEndTime;
        netStatusBean.requestHost = requestHost;
        netStatusBean.requestCount = requestCount;
        netStatusBean.tcpStartTime = tcpStartTime;
        netStatusBean.tcpEndTime = tcpEndTime;
        netStatusBean.requestErrCount = requestErrCount;
        NetUtils.get().setLastMonitorStatus(netStatusBean);

    }

    @Override
    public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
        super.callFailed(call, ioe);
        requestErrCount += 1;
    }

    @Override
    public void callStart(@NotNull Call call) {
        super.callStart(call);
        requestHost = call.request().url().host();
        requestCount += 1;
        responseStartTime = System.currentTimeMillis();
    }

    @Override
    public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        dnsEndTime = System.currentTimeMillis();
    }

    @Override
    public void dnsStart(@NotNull Call call, @NotNull String domainName) {
        super.dnsStart(call, domainName);
        dnsStartTime = System.currentTimeMillis();
    }

    @Override
    public void secureConnectEnd(@NotNull Call call, @Nullable Handshake handshake) {
        super.secureConnectEnd(call, handshake);
    }

    @Override
    public void secureConnectStart(@NotNull Call call) {
        super.secureConnectStart(call);
    }

    @Override
    public void connectStart(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy) {
        super.connectStart(call, inetSocketAddress, proxy);
        tcpStartTime = System.currentTimeMillis();

    }

    @Override
    public void connectEnd(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol);
        tcpEndTime = System.currentTimeMillis();
    }


}
