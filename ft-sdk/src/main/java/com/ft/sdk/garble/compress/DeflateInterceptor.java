package com.ft.sdk.garble.compress;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.zip.Deflater;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 *
 */
public class DeflateInterceptor implements Interceptor {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "DeflateInterceptor";

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        // If there is no request body or there is already a Content-Encoding header, 
        // pass through
        if (originalRequest.body() == null || originalRequest.header(Constants.SYNC_DATA_CONTENT_ENCODING_HEADER) != null) {
            return chain.proceed(originalRequest);
        }

        // Create deflate compressed request body
        Request compressedRequest = originalRequest.newBuilder()
                .header(Constants.SYNC_DATA_CONTENT_ENCODING_HEADER, "deflate")
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
            public void writeTo(@NotNull BufferedSink sink) throws IOException {
                try {
                    Deflater deflater = new Deflater();//with zlib wrap
//                Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
//                //no zlib wrap
                    BufferedSink deflateSink = Okio.buffer(new DeflaterSink(sink, deflater));
                    body.writeTo(deflateSink);
                    deflateSink.close();
                } catch (Throwable t) {
                    LogUtils.e(TAG, "Deflate writeTo error" + LogUtils.getStackTraceString(t));
                    throw t;
                }
            }
        };
    }
}