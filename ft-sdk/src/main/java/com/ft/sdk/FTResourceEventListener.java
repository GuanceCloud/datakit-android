package com.ft.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 配合 OKHttp {@link EventListener} 获取网络请求 dns、ssl、tcp 等时间点指标
 *
 * @author Brandon
 */
public class FTResourceEventListener extends EventListener {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTResourceEventListener";

    private String requestHost = null;
    private long callStartTime = -1;
    private long headerEndTime = -1;
    private long headerStartTime = -1;
    private long bodyStartTime = -1;
    private long bodyEndTime = -1;
    private long dnsEndTime = -1;
    private long dnsStartTime = -1;
    private long sslEndTime = -1;
    private long sslStartTime = -1;
    private long tcpStartTime = -1;
    private long tcpEndTime = -1;
    private String resourceHostIP;

    private final String resourceId;

    private final boolean enableResourceHostIP;
    private final EventListener originEventListener;

    public FTResourceEventListener(String resourceId, Boolean enableResourceHostIP, EventListener originEventListener) {
        LogUtils.d(TAG, "FTFactory create:" + resourceId);

        this.enableResourceHostIP = enableResourceHostIP;
        this.resourceId = resourceId;
        this.originEventListener = originEventListener;
    }

    @Override
    public void callEnd(@NonNull Call call) {
        super.callEnd(call);
        originEventListener.callEnd(call);
        setNetworkMetricsTimeline();
    }

    @Override
    public void callFailed(@NonNull Call call, @NonNull IOException ioe) {
        super.callFailed(call, ioe);
        setNetworkMetricsTimeline();
    }

    @Override
    public void callStart(@NonNull Call call) {
        super.callStart(call);
        originEventListener.callStart(call);
        requestHost = call.request().url().host();
        callStartTime = System.nanoTime();
    }

    @Override
    public void responseHeadersStart(@NonNull Call call) {
        super.responseHeadersStart(call);
        originEventListener.responseHeadersStart(call);
        headerStartTime = System.nanoTime();
    }

    @Override
    public void responseBodyStart(Call call) {
        super.responseBodyStart(call);
        bodyStartTime = System.nanoTime();
    }

    @Override
    public void requestHeadersStart(@NonNull Call call) {
        super.requestHeadersStart(call);
        originEventListener.requestHeadersStart(call);
    }

    @Override
    public void responseBodyEnd(@NonNull Call call, long byteCount) {
        super.responseBodyEnd(call, byteCount);
        originEventListener.responseBodyEnd(call, byteCount);
        bodyEndTime = System.nanoTime();
//        LogUtils.d(TAG, "responseBodyEnd:" + resourceId);
    }

//    @Override
//    public void responseFailed(@NonNull Call call, @NonNull IOException ioe) {
//        super.responseFailed(call, ioe);
//    }

    @Override
    public void dnsEnd(@NonNull Call call, @NonNull String domainName, @NonNull List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        originEventListener.dnsEnd(call, domainName, inetAddressList);
        dnsEndTime = System.nanoTime();
    }

    @Override
    public void dnsStart(@NonNull Call call, @NonNull String domainName) {
        super.dnsStart(call, domainName);
        originEventListener.dnsStart(call, domainName);
        dnsStartTime = System.nanoTime();
    }

    @Override
    public void secureConnectEnd(@NonNull Call call, @NonNull Handshake handshake) {
        super.secureConnectEnd(call, handshake);
        originEventListener.secureConnectEnd(call, handshake);
        sslEndTime = System.nanoTime();
//        LogUtils.d(TAG, "secureConnectEnd:" + resourceId);
    }

    @Override
    public void secureConnectStart(@NonNull Call call) {
        super.secureConnectStart(call);
        originEventListener.secureConnectStart(call);
        sslStartTime = System.nanoTime();
//        LogUtils.d(TAG, "secureConnectStart:" + resourceId);
    }

    @Override
    public void connectStart(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy) {
        super.connectStart(call, inetSocketAddress, proxy);
        originEventListener.connectStart(call, inetSocketAddress, proxy);
        tcpStartTime = System.nanoTime();
        if (enableResourceHostIP) {
            resourceHostIP = inetSocketAddress.getAddress().getHostAddress();
        }
//        LogUtils.d(TAG, "connectStart:" + resourceId + ",hostAddr:" + resourceHostIP);
    }

    @Override
    public void connectEnd(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy, @Nullable Protocol protocol) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol);
        originEventListener.connectEnd(call, inetSocketAddress, proxy, protocol);
        tcpEndTime = System.nanoTime();
//        LogUtils.d(TAG, "connectEnd:" + resourceId);
    }

    @Override
    public void canceled(@NotNull Call call) {
        super.canceled(call);
        originEventListener.canceled(call);
    }

    @Override
    public void connectFailed(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol, @NotNull IOException ioe) {
        super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
        originEventListener.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
    }

    @Override
    public void connectionAcquired(@NotNull Call call, @NotNull Connection connection) {
        super.connectionAcquired(call, connection);
        originEventListener.connectionAcquired(call, connection);
    }

    @Override
    public void connectionReleased(@NotNull Call call, @NotNull Connection connection) {
        super.connectionReleased(call, connection);
        originEventListener.connectionReleased(call, connection);
    }

    @Override
    public void proxySelectEnd(@NotNull Call call, @NotNull HttpUrl url, @NotNull List<Proxy> proxies) {
        super.proxySelectEnd(call, url, proxies);
        originEventListener.proxySelectEnd(call, url, proxies);
    }

    @Override
    public void proxySelectStart(@NotNull Call call, @NotNull HttpUrl url) {
        super.proxySelectStart(call, url);
        originEventListener.proxySelectStart(call, url);
    }

    @Override
    public void requestBodyEnd(@NotNull Call call, long byteCount) {
        super.requestBodyEnd(call, byteCount);
        originEventListener.requestBodyEnd(call, byteCount);
    }

    @Override
    public void requestBodyStart(@NotNull Call call) {
        super.requestBodyStart(call);
        originEventListener.requestBodyStart(call);
    }

    @Override
    public void requestFailed(@NotNull Call call, @NotNull IOException ioe) {
        super.requestFailed(call, ioe);
        originEventListener.requestFailed(call, ioe);
    }

    @Override
    public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
        super.requestHeadersEnd(call, request);
        originEventListener.requestHeadersEnd(call, request);
    }

    @Override
    public void responseFailed(@NotNull Call call, @NotNull IOException ioe) {
        super.responseFailed(call, ioe);
        originEventListener.responseFailed(call, ioe);
    }

    @Override
    public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
        super.responseHeadersEnd(call, response);
        originEventListener.responseHeadersEnd(call, response);
        headerEndTime = System.nanoTime();
    }

    /**
     * 将指标数据写入到对应 {@link this#resourceId} 的 Resource 数据里
     */
    private void setNetworkMetricsTimeline() {
        NetStatusBean netStatusBean = new NetStatusBean();
        netStatusBean.requestHost = requestHost;
        netStatusBean.callStartTime = callStartTime;
        netStatusBean.headerEndTime = headerEndTime;
        netStatusBean.headerStartTime = headerStartTime;
        netStatusBean.bodyEndTime = bodyEndTime;
        netStatusBean.bodyStartTime = bodyStartTime;
        netStatusBean.dnsEndTime = dnsEndTime;
        netStatusBean.dnsStartTime = dnsStartTime;
        netStatusBean.sslEndTime = sslEndTime;
        netStatusBean.sslStartTime = sslStartTime;
        netStatusBean.tcpStartTime = tcpStartTime;
        netStatusBean.tcpEndTime = tcpEndTime;
        netStatusBean.resourceHostIP = resourceHostIP;
        FTRUMInnerManager.get().setNetState(this.resourceId, netStatusBean);
    }

    /**
     * 创建 {@link  FTResourceEventListener} 对应 {@link  EventListener.Factory} 对象
     */
    public static class FTFactory implements EventListener.Factory {

        /**
         * 开启 resource host ip 采集
         */
        private final boolean enableResourceHostIP;

        /**
         * Resource 过滤规则
         */
        private final FTInTakeUrlHandler handler;

        private EventListener originEventLister = new NoOPEventListener();
        private final EventListener.Factory originFactory;

        public FTFactory() {
            this.enableResourceHostIP = false;
            this.handler = null;
            this.originFactory = null;
        }

        public FTFactory(boolean enableResourceHostIP) {
            this.enableResourceHostIP = enableResourceHostIP;
            this.handler = null;
            this.originFactory = null;
        }

        public FTFactory(boolean enableResourceHostIP, FTInTakeUrlHandler handler, EventListener.Factory originFactory) {
            this.enableResourceHostIP = enableResourceHostIP;
            this.handler = handler;
            this.originFactory = originFactory;
        }


        /**
         * @param call
         * @return
         */
        @NonNull
        @Override
        public EventListener create(@NonNull Call call) {
            if (originFactory != null) {
                originEventLister = originFactory.create(call);
            }
            String url = call.request().url().toString();
            if (handler != null && handler.isInTakeUrl(url)) {
                return originEventLister;
            }
            String resourceId = Utils.identifyRequest(call.request());
            //自动计算 resourceId
            return new FTResourceEventListener(resourceId,
                    this.enableResourceHostIP, originEventLister);
        }
    }

    /**
     * 无操作，不进行监听
     */
    public static class NoOPEventListener extends EventListener {

    }


}
