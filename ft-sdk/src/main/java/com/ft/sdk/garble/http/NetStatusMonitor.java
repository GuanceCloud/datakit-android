package com.ft.sdk.garble.http;

import com.ft.sdk.garble.bean.NetStatusBean;

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

/**
 *
 */
public abstract class NetStatusMonitor extends EventListener {

    private final NetStatusBean netStatusBean = new NetStatusBean();

    protected abstract void getNetStatusInfoWhenCallEnd(NetStatusBean bean);

    @Override
    public void callEnd(@NotNull Call call) {
        super.callEnd(call);
        getNetStatusInfoWhenCallEnd(netStatusBean);
    }

    @Override
    public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
        super.callFailed(call, ioe);
    }

    @Override
    public void callStart(@NotNull Call call) {
        super.callStart(call);
        netStatusBean.reset();
        netStatusBean.requestHost = call.request().url().host();
        netStatusBean.fetchStartTime = System.currentTimeMillis();

    }

    @Override
    public void responseHeadersStart(@NotNull Call call) {
        super.responseHeadersStart(call);
        netStatusBean.responseStartTime = System.currentTimeMillis();

    }

    @Override
    public void responseBodyEnd(@NotNull Call call, long byteCount) {
        super.responseBodyEnd(call, byteCount);
        netStatusBean.responseEndTime = System.currentTimeMillis();
    }

    @Override
    public void responseFailed(@NotNull Call call, @NotNull IOException ioe) {
        super.responseFailed(call, ioe);
    }

    @Override
    public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        netStatusBean.dnsEndTime = System.currentTimeMillis();
    }

    @Override
    public void dnsStart(@NotNull Call call, @NotNull String domainName) {
        super.dnsStart(call, domainName);
        netStatusBean.dnsStartTime = System.currentTimeMillis();
    }

    @Override
    public void secureConnectEnd(@NotNull Call call, @Nullable Handshake handshake) {
        super.secureConnectEnd(call, handshake);
        netStatusBean.sslEndTime = System.currentTimeMillis();

    }

    @Override
    public void secureConnectStart(@NotNull Call call) {
        super.secureConnectStart(call);
        netStatusBean.sslStartTime = System.currentTimeMillis();
    }

    @Override
    public void connectStart(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy) {
        super.connectStart(call, inetSocketAddress, proxy);
        netStatusBean.tcpStartTime = System.currentTimeMillis();

    }

    @Override
    public void connectEnd(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol);
        netStatusBean.tcpEndTime = System.currentTimeMillis();
    }


}
