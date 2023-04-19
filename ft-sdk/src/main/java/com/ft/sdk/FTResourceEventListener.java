package com.ft.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.utils.Utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Protocol;

/**
 * 配合 OKHttp {@link EventListener} 获取网络请求 dns、ssl、tcp 等时间点指标
 *
 * @author Brandon
 */
public class FTResourceEventListener extends EventListener {

    private String requestHost = null;
    private long fetchStartTime = -1;
    private long responseStartTime = -1;
    private long responseEndTime = -1;
    private long dnsEndTime = -1;
    private long dnsStartTime = -1;
    private long sslEndTime = -1;
    private long sslStartTime = -1;
    private long tcpStartTime = -1;
    private long tcpEndTime = -1;

    private final String resourceId;

    public FTResourceEventListener(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public void callEnd(@NonNull Call call) {
        super.callEnd(call);
        NetStatusBean netStatusBean = new NetStatusBean();
        netStatusBean.requestHost = requestHost;
        netStatusBean.fetchStartTime = fetchStartTime;
        netStatusBean.responseStartTime = responseStartTime;
        netStatusBean.responseEndTime = responseEndTime;
        netStatusBean.dnsEndTime = dnsEndTime;
        netStatusBean.dnsStartTime = dnsStartTime;
        netStatusBean.sslEndTime = sslEndTime;
        netStatusBean.sslStartTime = sslStartTime;
        netStatusBean.tcpStartTime = tcpStartTime;
        netStatusBean.tcpEndTime = tcpEndTime;
        FTRUMGlobalManager.get().setNetState(this.resourceId, netStatusBean);
    }

    @Override
    public void callStart(@NonNull Call call) {
        super.callStart(call);

        requestHost = call.request().url().host();
        fetchStartTime = Utils.getCurrentNanoTime();

    }

    @Override
    public void responseHeadersStart(@NonNull Call call) {
        super.responseHeadersStart(call);
        responseStartTime = Utils.getCurrentNanoTime();

    }

    @Override
    public void responseBodyEnd(@NonNull Call call, long byteCount) {
        super.responseBodyEnd(call, byteCount);
        responseEndTime = Utils.getCurrentNanoTime();
    }

//    @Override
//    public void responseFailed(@NonNull Call call, @NonNull IOException ioe) {
//        super.responseFailed(call, ioe);
//    }

    @Override
    public void dnsEnd(@NonNull Call call, @NonNull String domainName, @NonNull List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        dnsEndTime = Utils.getCurrentNanoTime();
    }

    @Override
    public void dnsStart(@NonNull Call call, @NonNull String domainName) {
        super.dnsStart(call, domainName);
        dnsStartTime = Utils.getCurrentNanoTime();
    }

    @Override
    public void secureConnectEnd(@NonNull Call call, @NonNull Handshake handshake) {
        super.secureConnectEnd(call, handshake);
        sslEndTime = Utils.getCurrentNanoTime();

    }

    @Override
    public void secureConnectStart(@NonNull Call call) {
        super.secureConnectStart(call);
        sslStartTime = Utils.getCurrentNanoTime();
    }

    @Override
    public void connectStart(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy) {
        super.connectStart(call, inetSocketAddress, proxy);
        tcpStartTime = Utils.getCurrentNanoTime();

    }

    @Override
    public void connectEnd(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy, @Nullable Protocol protocol) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol);
        tcpEndTime = Utils.getCurrentNanoTime();
    }

    public static class FTFactory implements EventListener.Factory {
        /**
         *
         * @param call
         * @return
         */
        @NonNull
        @Override
        public EventListener create(@NonNull Call call) {
            return new FTResourceEventListener(Utils.identifyRequest(call.request()));
        }
    }


}
