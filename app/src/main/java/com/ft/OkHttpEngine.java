package com.ft;

import com.ft.sdk.FTMonitor;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.INetEngine;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:51:48
 * description:
 */
public class OkHttpEngine implements INetEngine {
    private static OkHttpClient client;
    private Request request;

    @Override
    public void defaultConfig(HttpBuilder httpBuilder) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(httpBuilder.getSendOutTime(), TimeUnit.MILLISECONDS)
                    .readTimeout(httpBuilder.getReadOutTime(), TimeUnit.MILLISECONDS)
                    .eventListener(new EventListener() {
                        @Override
                        public void callEnd(@NotNull Call call) {
                            super.callEnd(call);
                            FTMonitor.get().setResponseEndTime();
                        }

                        @Override
                        public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
                            super.callFailed(call, ioe);
                            FTMonitor.get().setRequestErrCount();
                        }

                        @Override
                        public void callStart(@NotNull Call call) {
                            super.callStart(call);
                            FTMonitor.get().setRequestCount();
                            FTMonitor.get().setResponseStartTime();
                        }

                        @Override
                        public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
                            super.dnsEnd(call, domainName, inetAddressList);
                            FTMonitor.get().setDnsEndTime();
                        }

                        @Override
                        public void dnsStart(@NotNull Call call, @NotNull String domainName) {
                            super.dnsStart(call, domainName);
                            FTMonitor.get().setDnsStartTime();
                        }

                        @Override
                        public void secureConnectEnd(@NotNull Call call, @Nullable Handshake handshake) {
                            super.secureConnectEnd(call, handshake);
                            FTMonitor.get().setTcpEndTime();
                        }

                        @Override
                        public void secureConnectStart(@NotNull Call call) {
                            super.secureConnectStart(call);
                            FTMonitor.get().setTcpStartTime();
                        }
                    })
                    .build();
        }
    }

    @Override
    public void createRequest(HttpBuilder httpBuilder) {
        RequestBody requestBody = null;
        if (httpBuilder.getMethod() == RequestMethod.POST) {
            requestBody = RequestBody.create(null, httpBuilder.getBodyString());
        }
        Headers.Builder builder = new Headers.Builder();
        HashMap<String, String> hashMap = httpBuilder.getHeadParams();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        request = new Request.Builder()
                .url(httpBuilder.getUrl())
                .method(httpBuilder.getMethod().name(), requestBody)
                .headers(builder.build())
                .build();
    }

    @Override
    public ResponseData execute() {
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String string = "";
            if (responseBody != null) {
                string = responseBody.string();
            }
            return new ResponseData(response.code(), string);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
