package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.http.HttpUrl;
import com.ft.sdk.garble.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OKHttp Trace Interceptor
 * <p>
 * 为 http 链路添加链路 header
 */
public class FTTraceInterceptor implements Interceptor {

    private final boolean webViewTrace;

    public FTTraceInterceptor() {
        this(false);
    }


    public FTTraceInterceptor(boolean webViewTrace) {
        this.webViewTrace = webViewTrace;
    }


    public FTTraceInterceptor(HeaderHandler headerHandler) {
        this.webViewTrace = false;
        this.headerHandler = headerHandler;
    }

    private HeaderHandler headerHandler;

    /**
     * 自定义 TraceHeader
     */
    public abstract static class HeaderHandler {
        /**
         *
         * @param request OKHttp 请求 Request
         * @return 替换 TraceHeader 内容
         */
        public abstract HashMap<String, String> getTraceHeader(Request request);

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
        HttpUrl httpUrl = new HttpUrl(url.host(), url.encodedPath(), url.port());
        HashMap<String, String> requestHeaders = headerHandler != null ?
                headerHandler.getTraceHeader(request) : FTTraceManager.get().getTraceHeader(uniqueKey, httpUrl);
        try {

            for (String key : requestHeaders.keySet()) {
                requestBuilder.header(key, requestHeaders.get(key));//避免重试出现重复头
            }

            response = chain.proceed(requestBuilder.build());

        } catch (IOException e) {
            exception = e;
        }

        if (exception != null) {
//            FTTraceManager.get().addTrace(uniqueKey, request.method(), requestHeaders,
//                    null, 0, exception.getMessage());
            throw new IOException(exception);
        }
//        else {
//            String responseBodyString = "";
//            String errorMsg = "";
//            int code = response.code();
//            if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
//                ResponseBody responseBody = response.body();
//                if (HttpHeaders.hasBody(response)) {
//                    if (responseBody != null) {
//                        if (isSupportFormat(responseBody.contentType())) {
//                            byte[] bytes = Utils.toByteArray(responseBody.byteStream());
//                            MediaType contentType = responseBody.contentType();
//                            responseBodyString = new String(bytes, Utils.getCharset(contentType));
//                            errorMsg = responseBodyString;
//
//                            ResponseBody copyResponseBody = ResponseBody.create(responseBody.contentType(), bytes);
//                            response = response.newBuilder().body(copyResponseBody).build();
//
//                        }
//                    }
//                }
//            }
//            HashMap<String, String> responseHeaders = new HashMap<>();
//            for (String key : response.headers().names()) {
//                responseHeaders.put(key, response.header(key));
//            }
//            FTTraceManager.get().addTrace(uniqueKey, request.method(),
//                    requestHeaders, responseHeaders, response.code(), errorMsg);
//        }
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
        return supportContentType.contains(contentType);
    }


}
