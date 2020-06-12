package com.ft.sdk.garble.http;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

/**
 * create: by huangDianHua
 * time: 2020/5/18 10:06:37
 * description:
 */
public class OKHttpEventListener extends EventListener {
    private String requestRaw;
    private String responseRaw;
    private long duration;
    private String operationName;

    @Override
    public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
        super.requestHeadersEnd(call, request);
        try {
            if(FTHttpConfig.get().metricsUrl.contains(request.url().host())){
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(request.method()).append("\n");
            sb.append(request.headers().toString()).append("\n\n");
            if(request.body() != null) {
                Buffer sink = new Buffer();
                request.body().writeTo(sink);
                String body = sink.readString(StandardCharsets.UTF_8);
                sb.append(body).append("\n");
            }
            requestRaw = sb.toString();
            String protocol = "Http";
            if(request.isHttps()){
                protocol = "Https";
            }
            operationName = request.method()+"/"+protocol;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
        super.responseHeadersEnd(call, response);
        try {
            if(FTHttpConfig.get().metricsUrl.contains(response.request().url().host())){
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(response.protocol().toString()).append("\n");
            sb.append(response.headers().toString());
            duration = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
            responseRaw = sb.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void callEnd(@NotNull Call call) {
        super.callEnd(call);
        NetUtils.get().responseEndTime = System.currentTimeMillis();
        if(FTHttpConfig.get().metricsUrl.contains(call.request().url().host())){
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestContent",requestRaw);
            jsonObject.put("responseContent",responseRaw);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogBean logBean = new LogBean(Constants.USER_AGENT,jsonObject.toString(),System.currentTimeMillis());
        logBean.setOperationName(operationName);
        logBean.setDuration(duration*1000);
        logBean.setSpanID(Utils.MD5(DeviceUtils.getUuid(FTApplication.getApplication())));
        logBean.setTraceID(UUID.randomUUID().toString());
        FTTrack.getInstance().logBackground(logBean);
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
