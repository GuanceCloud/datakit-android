package com.ft.sdk.garble.compress;

import java.io.IOException;
import java.util.zip.Deflater;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.DeflaterSink;
import okio.Okio;

public class DeflateInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // 如果没有请求体或已经有 Content-Encoding 头，直接放行
        if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest);
        }

        // 创建 deflate 压缩的请求体
        Request compressedRequest = originalRequest.newBuilder()
                .header("Content-Encoding", "deflate")
                .method(originalRequest.method(), deflate(originalRequest.body()))
                .build();

        return chain.proceed(compressedRequest);
    }

    private RequestBody deflate(final RequestBody body) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Deflater deflater = new Deflater();//with zlib wrap
//                Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);//no zlib wrap
                BufferedSink deflateSink = Okio.buffer(new DeflaterSink(sink, deflater));
                body.writeTo(deflateSink);
                deflateSink.close();
            }
        };
    }
}