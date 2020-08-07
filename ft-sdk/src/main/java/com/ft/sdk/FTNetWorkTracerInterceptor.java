package com.ft.sdk;

import com.ft.sdk.garble.FTExceptionHandler;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.SkyWalkingUtils;
import com.ft.sdk.garble.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
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
    public static final String SKYWALKING_V3_SW_X = "sw8";


    //是否可以采样
    boolean enableTrace;

    private void uploadNetTrace(Request request, @Nullable Response response, String traceID,
                                String spanID, String responseBody, String error, long duration, long dateline) {
        try {
            if (!enableTrace) {
                return;
            }
            String operationName = request.method() + "/http";

            JSONObject requestContent = buildRequestJsonContent(request);
            JSONObject responseContent = buildResponseJsonContent(response, responseBody, error);
            boolean isError = response == null || response.code() > 400;

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestContent", requestContent);
            jsonObject.put("responseContent", responseContent);
            if (isOverMaxLength(jsonObject.toString())) {
                return;
            }

            String endPoint = request.url().host() + ":" + request.url().port();
            LogBean logBean = new LogBean(Constants.USER_AGENT, jsonObject, dateline);
            logBean.setOperationName(operationName);
            logBean.setDuration(duration * 1000);
            logBean.setClazz("tracing");
            logBean.setSpanType("entry");
            logBean.setEndpoint(endPoint);
            logBean.setIsError(String.valueOf(isError));
            logBean.setServiceName(FTExceptionHandler.get().getTrackServiceName());
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
        enableTrace = Utils.enableTraceSamplingRate();
        if (!FTHttpConfig.get().networkTrace) {
            return chain.proceed(request);
        }
        Response response = null;
        Request.Builder requestBuilder = request.newBuilder();
        String traceID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        String spanID = Utils.getGUID_16();
        Exception exception = null;

        //请求开始时间
        long requestTime = System.currentTimeMillis();
        Request newRequest = null;
        try {
            String sampled;
            //抓取数据内容
            if (enableTrace) {
                sampled = "1";
            } else {
                sampled = "0";
            }
            String parentSpanID = "0000000000000000";

            //在数据中添加标记
            if (FTHttpConfig.get().traceType == TraceType.ZIPKIN) {
                requestBuilder.addHeader(ZIPKIN_SPAN_ID, spanID);
                requestBuilder.addHeader(ZIPKIN_TRACE_ID, traceID);
                requestBuilder.addHeader(ZIPKIN_SAMPLED, sampled);
            } else if (FTHttpConfig.get().traceType == TraceType.JAEGER) {
                requestBuilder.addHeader(JAEGER_KEY, traceID + ":" + spanID + ":" + parentSpanID + ":" + sampled);
            } else if (FTHttpConfig.get().traceType == TraceType.SKYWALKING_V3) {
                SkyWalkingUtils skyWalkingUtils = new SkyWalkingUtils(sampled,requestTime,request.url());
                traceID = skyWalkingUtils.getNewTraceId();
                spanID = skyWalkingUtils.getNewSpanId();
                requestBuilder.addHeader(SKYWALKING_V3_SW_X, skyWalkingUtils.getSw8());

            }
            newRequest = requestBuilder.build();
            response = chain.proceed(requestBuilder.build());

        } catch (IOException e) {
            exception = e;
        }

        //请求结束时间
        long responseTime = System.currentTimeMillis();

        if (exception != null) {
            uploadNetTrace(newRequest, null, traceID, spanID, "", exception.getMessage(),
                    responseTime - requestTime, requestTime);
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
                    }
                }
            }
            uploadNetTrace(newRequest, response, traceID, spanID, responseBody, "", responseTime - requestTime, requestTime);
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
            JSONObject jbBody = null;
            JSONArray jaBody = null;
            try {
                jbBody = new JSONObject(body);
                json.put("body", jbBody);
            } catch (JSONException e) {
            }
            if (jbBody == null) {
                try {
                    jaBody = new JSONArray(body);
                    json.put("body", jaBody);
                } catch (JSONException e) {
                }
            }
            if (jaBody == null && jbBody == null) {
                json.put("body", body);
            }
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
        List<String> supportContentType = FTHttpConfig.get().traceContentType;
        if (supportContentType == null) {
            return false;
        }
        if (supportContentType.contains(contentType)) {
            return true;
        }
        return false;
    }

    /**
     * 是否超过 30KB
     *
     * @param content
     * @return
     */
    private static boolean isOverMaxLength(String content) {
        byte[] b = content.getBytes(StandardCharsets.UTF_8);
        return b.length > 30720;
    }

}
