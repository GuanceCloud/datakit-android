package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.http.NetStatusMonitor;
import com.ft.sdk.garble.utils.Utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;

/**
 *
 */
public class FTResourceInterceptor extends NetStatusMonitor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        Exception exception = null;
        String resourceId = Utils.identifyRequest(request);
        FTRUMGlobalManager.get().startResource(resourceId);

        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            exception = e;
        }
        ResourceParams params = new ResourceParams();
        params.url = request.url().toString();
        params.requestHeader = request.headers().toString();
        params.resourceMethod = request.method();

        if (exception != null) {
            throw new IOException(exception);
        } else {
            String responseBody = "";
            Response.Builder responseBuilder = response.newBuilder();
            Response clone = responseBuilder.build();
            ResponseBody responseBody1 = clone.body();
            if (HttpHeaders.hasBody(clone)) {
                if (responseBody1 != null) {
                    byte[] bytes = Utils.toByteArray(responseBody1.byteStream());
                    MediaType contentType = responseBody1.contentType();
                    responseBody = new String(bytes, Utils.getCharset(contentType));
                    responseBody1 = ResponseBody.create(responseBody1.contentType(), bytes);
                    response = response.newBuilder().body(responseBody1).build();

                    params.responseHeader = response.headers().toString();
                    params.responseBody = responseBody;
                    params.responseContentType = response.header("Content-Type");
                    params.responseConnection = response.header("Connection");
                    params.responseContentEncoding = response.header("Content-Encoding");
                    params.resourceStatus = response.code();

                }
            }
        }
        FTRUMGlobalManager.get().setTransformContent(resourceId, params);
        FTRUMGlobalManager.get().stopResource(resourceId);
        return response;
    }

    @Override
    protected void getNetStatusInfoWhenCallEnd(String requestId, NetStatusBean bean) {
        FTRUMGlobalManager.get().putRUMResourcePerformance(requestId, bean);
    }


}
