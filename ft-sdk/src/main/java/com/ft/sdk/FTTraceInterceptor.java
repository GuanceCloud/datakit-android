package com.ft.sdk;

import androidx.annotation.NonNull;

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
 * Add link header for http link
 */
public class FTTraceInterceptor implements Interceptor {


    public FTTraceInterceptor() {
    }

    public FTTraceInterceptor(HeaderHandler headerHandler) {
        this.headerHandler = headerHandler;
    }

    private HeaderHandler headerHandler;

    /**
     * Custom TraceHeader
     */
    public abstract static class HeaderHandler extends TraceRUMLinkable {
        /**
         * @param request OKHttp request
         * @return Replace TraceHeader content
         */
        public abstract HashMap<String, String> getTraceHeader(Request request);

    }

    /**
     * trace_id and span_id associated with RUM
     */
    public static class TraceRUMLinkable {
        public String getTraceID() {
            return null;
        }

        public String getSpanID() {
            return null;
        }
    }


    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        Request.Builder requestBuilder = request.newBuilder();
        IOException exception = null;

        String resourceId = Utils.identifyRequest(request);
        HashMap<String, String> requestHeaders;
        if (headerHandler != null) {
            requestHeaders = headerHandler.getTraceHeader(request);
            FTTraceManager.get().putTraceHandler(resourceId, headerHandler);
        } else {
            requestHeaders = FTTraceManager.get().getTraceHeader(resourceId, request.url() + "");
        }
        try {

            if (requestHeaders != null) {
                for (String key : requestHeaders.keySet()) {
                    requestBuilder.header(key, requestHeaders.get(key));//Avoid duplicate headers on retry
                }
            }

            response = chain.proceed(requestBuilder.build());

        } catch (IOException e) {
            exception = e;
        }

        if (exception != null) {
//            FTTraceManager.get().addTrace(uniqueKey, request.method(), requestHeaders,
//                    null, 0, exception.getMessage());
            throw exception;
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
     * Supported content capture
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
