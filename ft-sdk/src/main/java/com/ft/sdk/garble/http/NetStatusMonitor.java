package com.ft.sdk.garble.http;

import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 */
public class NetStatusMonitor extends EventListener {
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
        NetUtils.get().responseEndTime = System.currentTimeMillis();
    }

    @Override
    public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
        super.callFailed(call, ioe);
        NetUtils.get().requestErrCount += 1;
    }

    @Override
    public void callStart(@NotNull Call call) {
        super.callStart(call);
        NetUtils.get().requestHost = call.request().url().host();
        NetUtils.get().requestCount += 1;
        NetUtils.get().responseStartTime = System.currentTimeMillis();
    }

    @Override
    public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        NetUtils.get().dnsEndTime = System.currentTimeMillis();
    }

    @Override
    public void dnsStart(@NotNull Call call, @NotNull String domainName) {
        super.dnsStart(call, domainName);
        NetUtils.get().dnsStartTime = System.currentTimeMillis();
    }

    @Override
    public void secureConnectEnd(@NotNull Call call, @Nullable Handshake handshake) {
        super.secureConnectEnd(call, handshake);
        NetUtils.get().tcpEndTime = System.currentTimeMillis();
    }

    @Override
    public void secureConnectStart(@NotNull Call call) {
        super.secureConnectStart(call);
        NetUtils.get().tcpStartTime = System.currentTimeMillis();
    }

}
