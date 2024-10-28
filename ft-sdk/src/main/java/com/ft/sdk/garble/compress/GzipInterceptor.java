package com.ft.sdk.garble.compress;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

public class GzipInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // 如果请求体为空或请求头中已标识不需要压缩，直接返回原始请求
        if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest);
        }

        // 新建压缩后的请求
        Request compressedRequest = originalRequest.newBuilder()
                .header("Content-Encoding", "gzip")
                .method(originalRequest.method(), gzip(originalRequest.body())) // 对请求体进行 GZIP 压缩
                .build();

        return chain.proceed(compressedRequest);
    }

    // 使用 GZIP 压缩请求体
    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                // 由于 GZIP 是流式处理，无法提前知道压缩后的内容长度
                return -1;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink)); // 使用 GZIP 压缩
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }
}
