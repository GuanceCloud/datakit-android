package com.ft.sdk.garble.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.utils.Utils;


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
    public void callEnd(@NonNull Call call) {
        super.callEnd(call);
        getNetStatusInfoWhenCallEnd(netStatusBean);
    }

    @Override
    public void callFailed(@NonNull Call call, @NonNull IOException ioe) {
        super.callFailed(call, ioe);
    }

    @Override
    public void callStart(@NonNull Call call) {
        super.callStart(call);
        netStatusBean.reset();
        netStatusBean.requestHost = call.request().url().host();
        netStatusBean.fetchStartTime = Utils.getCurrentNanoTime();

    }

    @Override
    public void responseHeadersStart(@NonNull Call call) {
        super.responseHeadersStart(call);
        netStatusBean.responseStartTime = Utils.getCurrentNanoTime();

    }

    @Override
    public void responseBodyEnd(@NonNull Call call, long byteCount) {
        super.responseBodyEnd(call, byteCount);
        netStatusBean.responseEndTime = Utils.getCurrentNanoTime();
    }

//    @Override
//    public void responseFailed(@NonNull Call call, @NonNull IOException ioe) {
//        super.responseFailed(call, ioe);
//    }

    @Override
    public void dnsEnd(@NonNull Call call, @NonNull String domainName, @NonNull List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        netStatusBean.dnsEndTime = Utils.getCurrentNanoTime();
    }

    @Override
    public void dnsStart(@NonNull Call call, @NonNull String domainName) {
        super.dnsStart(call, domainName);
        netStatusBean.dnsStartTime = Utils.getCurrentNanoTime();
    }

    @Override
    public void secureConnectEnd(@NonNull Call call, @NonNull Handshake handshake) {
        super.secureConnectEnd(call, handshake);
        netStatusBean.sslEndTime = Utils.getCurrentNanoTime();

    }

    @Override
    public void secureConnectStart(@NonNull Call call) {
        super.secureConnectStart(call);
        netStatusBean.sslStartTime = Utils.getCurrentNanoTime();
    }

    @Override
    public void connectStart(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy) {
        super.connectStart(call, inetSocketAddress, proxy);
        netStatusBean.tcpStartTime = Utils.getCurrentNanoTime();

    }

    @Override
    public void connectEnd(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy, @Nullable Protocol protocol) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol);
        netStatusBean.tcpEndTime = Utils.getCurrentNanoTime();
    }


}
