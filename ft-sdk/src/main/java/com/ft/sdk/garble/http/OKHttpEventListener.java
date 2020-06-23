package com.ft.sdk.garble.http;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;

/**
 * create: by huangDianHua
 * time: 2020/5/18 10:06:37
 * description:
 */
public class OKHttpEventListener extends EventListener implements Interceptor {
    private String requestRaw;
    private String responseRaw;
    private long duration;
    private String operationName;
    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    @Override
    public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
        super.requestHeadersEnd(call, request);
        try {
            if (!FTHttpConfig.get().networkTrace || FTHttpConfig.get().metricsUrl.contains(request.url().host())) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(request.method()).append("\n");
            sb.append(request.headers().toString()).append("\n\n");
            if (request.body() != null) {
                Buffer sink = new Buffer();
                request.body().writeTo(sink);
                String body = sink.readString(StandardCharsets.UTF_8);
                sb.append(body).append("\n");
            }
            requestRaw = sb.toString();
            operationName = request.method() + "/http";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
        super.responseHeadersEnd(call, response);
        try {
            if (!FTHttpConfig.get().networkTrace || FTHttpConfig.get().metricsUrl.contains(response.request().url().host())) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(response.protocol().toString()).append("\n");
            sb.append(response.headers().toString()).append("\n");
            duration = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
            responseRaw = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadNetTrace(Call call,String isError){
        try {
            NetUtils.get().responseEndTime = System.currentTimeMillis();
            if (!FTHttpConfig.get().networkTrace || FTHttpConfig.get().metricsUrl.contains(call.request().url().host())) {
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestContent",requestRaw);
            jsonObject.put("responseContent",responseRaw + queue.poll());
            LogBean logBean = new LogBean(Constants.USER_AGENT, jsonObject, System.currentTimeMillis());
            logBean.setOperationName(operationName);
            logBean.setDuration(duration * 1000);
            logBean.setClazz("tracing");
            logBean.setIsError(isError);
            logBean.setServiceName(Constants.DEFAULT_LOG_SERVICE_NAME);
            logBean.setSpanID(Utils.MD5(DeviceUtils.getUuid(FTApplication.getApplication())));
            logBean.setTraceID(UUID.randomUUID().toString());
            FTTrack.getInstance().logBackground(logBean);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void callEnd(@NotNull Call call) {
        super.callEnd(call);
        uploadNetTrace(call,"false");
    }

    @Override
    public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
        super.callFailed(call, ioe);
        NetUtils.get().requestErrCount += 1;
        uploadNetTrace(call,"true");
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

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        if (!FTHttpConfig.get().networkTrace || FTHttpConfig.get().metricsUrl.contains(request.url().host())) {
            return chain.proceed(request);
        }
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            ResponseBody responseBody = ResponseBody.create(MediaType.parse("text/plain;charset=utf-8"), "" + e.getMessage());
            response = new Response.Builder()
                    .code(404)
                    .message("" + e.getMessage())
                    .request(request)
                    .body(responseBody)
                    .protocol(Protocol.HTTP_1_1)
                    .build();
        }
        return bodyForResponse(response);
    }


    private Response bodyForResponse(Response response) {
        try {
            Response.Builder builder = response.newBuilder();
            Response clone = builder.build();
            ResponseBody responseBody1 = clone.body();
            if (HttpHeaders.hasBody(clone)) {
                if (responseBody1 == null) return response;
                if (isPlaintext(responseBody1.contentType())) {
                    byte[] bytes = toByteArray(responseBody1.byteStream());
                    MediaType contentType = responseBody1.contentType();
                    String body = new String(bytes, getCharset(contentType));
                    if (queue.size() > 10) {
                        queue.clear();
                    }
                    queue.add(body);
                    responseBody1 = ResponseBody.create(responseBody1.contentType(), bytes);
                    return response.newBuilder().body(responseBody1).build();
                }
            }

        } catch (Exception e) {
        }
        return response;
    }

    private static Charset getCharset(MediaType contentType) {
        Charset charset = contentType != null ? contentType.charset(StandardCharsets.UTF_8) : StandardCharsets.UTF_8;
        if (charset == null) charset = StandardCharsets.UTF_8;
        return charset;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        output.close();
        return output.toByteArray();
    }

    public static void write(InputStream inputStream, OutputStream outputStream) throws IOException {
        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, len);
    }

    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype != null) {
            subtype = subtype.toLowerCase();
            if (subtype.contains("x-www-form-urlencoded") || subtype.contains("json") || subtype.contains("xml") || subtype.contains("html")) //
                return true;
        }
        return false;
    }
}
