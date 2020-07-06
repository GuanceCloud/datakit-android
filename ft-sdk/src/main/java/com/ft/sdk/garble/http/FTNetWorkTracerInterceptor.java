package com.ft.sdk.garble.http;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTTrack;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.MediaType;
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
public class FTNetWorkTracerInterceptor implements Interceptor {
    public static final String ZIPKIN_TRACE_ID = "X-B3-TraceId";
    public static final String ZIPKIN_SPAN_ID = "X-B3-SpanId";
    public static final String ZIPKIN_SAMPLED = "X-B3-Sampled";
    public static final String JAEGER_KEY = "uber-trace-id";

    private void uploadNetTrace(Request request, @Nullable Response response, String traceID,
                                String spanID, String responseBody, String error, long duration) {
        try {
            if (!FTHttpConfig.get().networkTrace) {
                return;
            }

            String operationName = request.method() + "/http";

            JSONObject requestContent = buildRequestJsonContent(request);
            JSONObject responseContent = buildResponseJsonContent(response, responseBody, error);
            boolean isError = response == null || response.code() != HttpURLConnection.HTTP_OK;

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestContent", requestContent);
            jsonObject.put("responseContent", responseContent);
            LogBean logBean = new LogBean(Constants.USER_AGENT, jsonObject, System.currentTimeMillis());
            logBean.setOperationName(operationName);
            logBean.setDuration(duration * 1000);
            logBean.setClazz("tracing");
            logBean.setIsError(String.valueOf(isError));
            logBean.setServiceName(Constants.DEFAULT_LOG_SERVICE_NAME);
            logBean.setSpanID(spanID);
            logBean.setTraceID(traceID);
            FTTrack.getInstance().logBackground(logBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();

        if (!FTHttpConfig.get().networkTrace) {
            return chain.proceed(request);
        }
        Response response = null;
        Request.Builder requestBuilder = request.newBuilder();
        String traceID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        String spanID = Utils.MD5_16(DeviceUtils.getUuid(FTApplication.getApplication())).toLowerCase();
        Exception exception = null;

        //请求开始时间
        long requestTime = System.currentTimeMillis();

        try {
            //抓取数据内容
            String sampled = "1";
            String parentSpanID = "0000000000000000";

            //在数据中添加标记
            if (FTHttpConfig.get().traceType == TraceType.ZIPKIN) {
                requestBuilder.addHeader(ZIPKIN_SPAN_ID, spanID);
                requestBuilder.addHeader(ZIPKIN_TRACE_ID, traceID);
                requestBuilder.addHeader(ZIPKIN_SAMPLED, sampled);
            } else if (FTHttpConfig.get().traceType == TraceType.JAEGER) {
                requestBuilder.addHeader(JAEGER_KEY, traceID + ":" + spanID + ":" + parentSpanID + ":" + sampled);
            }

            response = chain.proceed(requestBuilder.build());

        } catch (IOException e) {
            exception = e;
        }

        //请求结束时间
        long responseTime = System.currentTimeMillis();

        if (exception != null) {
            uploadNetTrace(request, null, traceID, spanID, "", exception.getMessage(), responseTime - requestTime);
            throw new IOException(exception);
        } else {
            String responseBody = "";
            Response.Builder responseBuilder = response.newBuilder();
            Response clone = responseBuilder.build();
            ResponseBody responseBody1 = clone.body();
            if (HttpHeaders.promisesBody(clone)) {
                if (responseBody1 != null) {
                    if (isPlaintext(responseBody1.contentType())) {
                        byte[] bytes = toByteArray(responseBody1.byteStream());
                        MediaType contentType = responseBody1.contentType();
                        responseBody = new String(bytes, getCharset(contentType));
                        responseBody1 = ResponseBody.create(responseBody1.contentType(), bytes);
                        response = response.newBuilder().body(responseBody1).build();
                    }
                }
            }
            uploadNetTrace(request, response, traceID, spanID, responseBody, "", responseTime - requestTime);
        }
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
            if (request.body() != null) {
                Buffer sink = new Buffer();
                request.body().writeTo(sink);
                String body = sink.readString(StandardCharsets.UTF_8);
                json.put("body", body);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * @param response
     * @param body
     * @return
     */
    private JSONObject buildResponseJsonContent(@Nullable Response response, String body, String error) {
        JSONObject json = new JSONObject();
        JSONObject headers = response != null ? new JSONObject(response.headers().toMultimap()) : new JSONObject();

        try {
            json.put("code", response != null ? response.code() : 0);
            json.put("headers", headers);
            json.put("body", body);
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
