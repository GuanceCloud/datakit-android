package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.http.HttpUrl;
import com.ft.sdk.garble.utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
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
public class FTTraceInterceptor implements Interceptor {

    private final boolean webViewTrace;

    public FTTraceInterceptor() {
        this(false);
    }


    public FTTraceInterceptor(boolean webViewTrace) {
        this.webViewTrace = webViewTrace;
    }


    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        Request.Builder requestBuilder = request.newBuilder();
        Exception exception = null;

        String uniqueKey = Utils.identifyRequest(request);
        okhttp3.HttpUrl url = request.url();
        HttpUrl httpUrl = new HttpUrl(url.host(), url.encodedPath(), url.port(), url.toString());
        HashMap<String, String> requestHeaders = FTTraceManager.get().getTraceHeader(uniqueKey, httpUrl);
        try {

            for (String key : requestHeaders.keySet()) {
                requestBuilder.header(key, requestHeaders.get(key));//避免重试出现重复头
            }

            response = chain.proceed(requestBuilder.build());

        } catch (IOException e) {
            exception = e;
        }

        if (exception != null) {
            FTTraceManager.get().addTrace(uniqueKey, request.method(), requestHeaders,
                    null, 0, exception.getMessage());
            throw new IOException(exception);
        } else {
            String responseBody = "";
            String errorMsg = "";
            int code = response.code();
            if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                Response.Builder responseBuilder = response.newBuilder();
                Response clone = responseBuilder.build();
                ResponseBody responseBody1 = clone.body();
                if (HttpHeaders.hasBody(clone)) {
                    if (responseBody1 != null) {
                        if (isSupportFormat(responseBody1.contentType())) {
                            byte[] bytes = Utils.toByteArray(responseBody1.byteStream());
                            MediaType contentType = responseBody1.contentType();
                            responseBody = new String(bytes, Utils.getCharset(contentType));
                            errorMsg = responseBody;
                        }
                    }
                }
            }
            HashMap<String, String> responseHeaders = new HashMap<>();
            for (String key : response.headers().names()) {
                responseHeaders.put(key, response.header(key));
            }
            FTTraceManager.get().addTrace(uniqueKey, request.method(),
                    requestHeaders, responseHeaders, response.code(), errorMsg);
        }
        return response;
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
        FTTraceConfig config = FTTraceConfigManager.get().getConfig();
        if (config == null) {
            return false;
        }
        List<String> supportContentType = config.getTraceContentType();
        if (supportContentType == null) {
            return false;
        }
        if (supportContentType.contains(contentType)) {
            return true;
        }
        return false;
    }


}
