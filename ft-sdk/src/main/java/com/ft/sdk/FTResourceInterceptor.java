package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.http.NetStatusMonitor;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;

/**
 * OKHttp Resource Interceptor
 *
 * 记录 RUM Resource 指标数据
 *
 * @author Brandon
 */
public class FTResourceInterceptor extends NetStatusMonitor implements Interceptor {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTResourceInterceptor";

    /**
     * @param chain
     * @return
     * @throws IOException
     */
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        Exception exception = null;
        String url = request.url().toString();

        FTRUMConfig rumConfig = FTRUMConfigManager.get().getConfig();
        boolean isInTakeUrl = rumConfig == null || rumConfig
                .getResourceUrlHandler().isInTakeUrl(url);

        try {
            response = chain.proceed(request);

            if (isInTakeUrl) {
                return response;
            }

        } catch (IOException e) {
            exception = e;
        }

        String resourceId = Utils.identifyRequest(request);
        LogUtils.d(TAG, "intercept id:" + url + "," + resourceId);
        FTRUMGlobalManager.get().startResource(resourceId);
        ResourceParams params = new ResourceParams();
        params.url = url;
        params.requestHeader = request.headers().toString();
        params.resourceMethod = request.method();

        if (exception != null) {
            throw new IOException(exception);
        } else {
            String responseBodyString = "";
            ResponseBody responseBody = response.body();
            if (HttpHeaders.hasBody(response)) {
                if (responseBody != null) {
                    byte[] bytes = Utils.toByteArray(responseBody.byteStream());
                    MediaType contentType = responseBody.contentType();
                    responseBodyString = new String(bytes, Utils.getCharset(contentType));
                    ResponseBody copyResponseBody = ResponseBody.create(responseBody.contentType(), bytes);
                    response = response.newBuilder().body(copyResponseBody).build();

                    params.responseHeader = response.headers().toString();
                    params.responseBody = responseBodyString;
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

    /**
     * 记录网络请求中耗时指标 {@link NetStatusBean},在 {@link  okhttp3.EventListener#callEnd(Call)} 时进行调用
     *
     * @param requestId
     * @param bean
     */
    @Override
    protected void getNetStatusInfoWhenCallEnd(String requestId, NetStatusBean bean) {
        FTRUMGlobalManager.get().setNetState(requestId, bean);
    }


}
