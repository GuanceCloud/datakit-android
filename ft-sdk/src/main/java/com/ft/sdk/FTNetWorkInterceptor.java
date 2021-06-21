package com.ft.sdk;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.http.HttpUrl;
import com.ft.sdk.garble.http.NetStatusMonitor;
import com.ft.sdk.garble.utils.LogUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;

/**
 * create: by huangDianHua
 * time: 2020/5/18 10:06:37
 * description:
 */
public class FTNetWorkInterceptor extends NetStatusMonitor implements Interceptor {

    private static final String TAG = "FTNetWorkTracerInterceptor";
    public static final String ZIPKIN_TRACE_ID = "X-B3-TraceId";
    public static final String ZIPKIN_SPAN_ID = "X-B3-SpanId";
    public static final String ZIPKIN_SAMPLED = "X-B3-Sampled";
    public static final String JAEGER_KEY = "uber-trace-id";
//    public static final String SKYWALKING_V3_SW_8 = "sw8";
//    public static final String SKYWALKING_V3_SW_6 = "sw6";

    private final boolean webViewTrace;

    private final FTNetworkRumHandler mNetworkRUMHandler = new FTNetworkRumHandler();

    public FTNetWorkInterceptor() {
        this(false);
    }


    public FTNetWorkInterceptor(boolean webViewTrace) {
        this.webViewTrace = webViewTrace;
    }


    private void uploadNetTrace(FTTraceHandler handler, String operationName, Request request, @Nullable Response response, String responseBody, String error) {
        try {

            JSONObject requestContent = buildRequestJsonContent(request);
            JSONObject responseContent = buildResponseJsonContent(response, responseBody, error);
            boolean isError = response == null || response.code() >= 400;

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestContent", requestContent);
            jsonObject.put("responseContent", responseContent);

            handler.traceDataUpload(jsonObject, operationName, isError);

        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }


    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {

        mNetworkRUMHandler.startResource();
        Request request = chain.request();
        Response response = null;
        Request.Builder requestBuilder = request.newBuilder();
        Exception exception = null;
        Request newRequest = null;
        String operationName = "";
        FTTraceHandler handler = new FTTraceHandler();
        try {
            okhttp3.HttpUrl url = request.url();
            HttpUrl httpUrl = new HttpUrl(url.host(), url.encodedPath(), url.port(), url.toString());
            operationName = request.method() + " " + httpUrl.getPath();
            HashMap<String, String> headers = handler.getTraceHeader(httpUrl);
            handler.setIsWebViewTrace(webViewTrace);
            for (String key : headers.keySet()) {
                requestBuilder.header(key, headers.get(key));//避免重试出现重复头
            }
            newRequest = requestBuilder.build();
            response = chain.proceed(requestBuilder.build());

        } catch (IOException e) {
            exception = e;
        }

        if (exception != null) {
            uploadNetTrace(handler, operationName, newRequest, null, "", exception.getMessage());
            mNetworkRUMHandler.setTransformContent(newRequest, null, "",
                    handler.getTraceID(), handler.getSpanID());

            throw new IOException(exception);
        } else {
            String responseBody = "";
            Response.Builder responseBuilder = response.newBuilder();
            Response clone = responseBuilder.build();
            ResponseBody responseBody1 = clone.body();
            if (HttpHeaders.promisesBody(clone)) {
                if (responseBody1 != null) {
                    if (isSupportFormat(responseBody1.contentType())) {
                        byte[] bytes = toByteArray(responseBody1.byteStream());
                        MediaType contentType = responseBody1.contentType();
                        responseBody = new String(bytes, getCharset(contentType));
                        responseBody1 = ResponseBody.create(responseBody1.contentType(), bytes);
                        response = response.newBuilder().body(responseBody1).build();
                        uploadNetTrace(handler, operationName, newRequest, response, responseBody, "");
                        mNetworkRUMHandler.setTransformContent(newRequest, response, responseBody,
                                handler.getTraceID(), handler.getSpanID());

                    }
                }
            }
        }
        mNetworkRUMHandler.stopResource();

        return response;
    }

    /**
     * @param request
     * @return
     * @throws IOException
     */
    private JSONObject buildRequestJsonContent(Request request) throws IOException {
        JSONObject json = new JSONObject();
        JSONObject headers = new JSONObject(request.headers().toMultimap());

        try {
            json.put("method", request.method());
            json.put("url", request.url());
            json.put("headers", headers);
//            if (request.body() != null) {
//                Buffer sink = new Buffer();
//                request.body().writeTo(sink);
//                String body = sink.readString(StandardCharsets.UTF_8);
//                json.put("body", body);
//            }
        } catch (JSONException e) {
            LogUtils.e(TAG, e.getMessage());
        }
        return json;
    }

    /**
     * @param response
     * @return
     */
    private JSONObject buildResponseJsonContent(@Nullable Response response, String body, String error) {
        JSONObject json = new JSONObject();
        JSONObject headers = response != null ? new JSONObject(response.headers().toMultimap()) : new JSONObject();

        try {
            int code = response != null ? response.code() : 0;
            json.put("code", code);
            json.put("headers", headers);
//            if (code > HttpURLConnection.HTTP_OK) {
//                JSONObject jbBody = null;
//                JSONArray jaBody = null;
//                try {
//                    jbBody = new JSONObject(body);
//                    json.put("body", jbBody);
//                } catch (JSONException e) {
//                }
//                if (jbBody == null) {
//                    try {
//                        jaBody = new JSONArray(body);
//                        json.put("body", jaBody);
//                    } catch (JSONException e) {
//                    }
//                }
//                if (jaBody == null && jbBody == null) {
//                    json.put("body", body);
//                }
//            }
            if (error != null && !error.isEmpty()) {
                json.put("error", error);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }


    private static Charset getCharset(MediaType contentType) {
        Charset charset = contentType != null ? contentType.charset(StandardCharsets.UTF_8) : StandardCharsets.UTF_8;
        if (charset == null) charset = StandardCharsets.UTF_8;
        return charset;
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        output.close();
        return output.toByteArray();
    }

    private static void write(InputStream inputStream, OutputStream outputStream) throws IOException {
        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, len);
    }

    /**
     * 支持内容抓取的
     *
     * @param mediaType
     * @return
     */
    private static boolean isSupportFormat(MediaType mediaType) {
        if (mediaType == null) return false;
        String contentType = mediaType.type() + "/" + mediaType.subtype();
        List<String> supportContentType = FTTraceConfigManager.get().traceContentType;
        if (supportContentType == null) {
            return false;
        }
        if (supportContentType.contains(contentType)) {
            return true;
        }
        return false;
    }

    @Override
    protected void getNetStatusInfoWhenCallEnd(NetStatusBean bean) {
        mNetworkRUMHandler.setTransformPerformance(bean);
        mNetworkRUMHandler.handleUpload();

    }
}
