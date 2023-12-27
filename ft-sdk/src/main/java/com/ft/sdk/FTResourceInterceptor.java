package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;

/**
 * OKHttp Resource Interceptor
 * <p>
 * 记录 RUM Resource 指标数据
 *
 * @author Brandon
 */
public class FTResourceInterceptor implements Interceptor {

    private ContentHandlerHelper handlerHelper;


    /**
     * 拦截 Okhttp 的 Request 和 Response ，
     * 通过 {@link #onRequest(Request, HashMap)}{@link  #onResponse(Response, HashMap)} 中 extraData
     * 追加自定义采集的数据
     */
    public abstract static class ContentHandlerHelper {

        private final HashMap<String, Object> extraData = new HashMap<>();

        /**
         * OKHttp Request
         *
         * @param request
         * @param extraData 附加数据
         */
        public abstract void onRequest(Request request, HashMap<String, Object> extraData);

        /**
         * OKHttp Response
         *
         * @param response
         * @param extraData 附加数据
         */
        public abstract void onResponse(Response response, HashMap<String, Object> extraData) throws IOException;

        /**
         * 返回网络链接过程中的异常
         *
         * @param e         请求发生的 IOException 数据
         * @param extraData 附加数据
         */
        public abstract void onException(Exception e, HashMap<String, Object> extraData);

    }

    public FTResourceInterceptor() {

    }

    public FTResourceInterceptor(ContentHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTResourceInterceptor";
    /**
     * 最大读取 byte 计算 32KB
     */
    private static final int BYTE_LIMIT_COUNT = 33554432;

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
            if (handlerHelper != null) {
                handlerHelper.onRequest(request, handlerHelper.extraData);
            }
            response = chain.proceed(request);

            if (isInTakeUrl) {
                return response;
            }

            if (handlerHelper != null) {
                handlerHelper.onResponse(response, handlerHelper.extraData);
            }

        } catch (IOException e) {
            if (isInTakeUrl) {
                throw new IOException(e);
            } else {
                exception = e;
            }
        }

        String resourceId = Utils.identifyRequest(request);
        LogUtils.d(TAG, "intercept id:" + resourceId);
        FTRUMInnerManager.get().startResource(resourceId);
        ResourceParams params = new ResourceParams();
        params.url = url;
        params.requestHeader = request.headers().toString();
        params.resourceMethod = request.method();

        if (exception == null) {
            String responseBodyString = "";
            String responseHeaderString = response.headers().toString();
            params.responseHeader = responseHeaderString;
            params.responseContentType = response.header("Content-Type");
            params.responseConnection = response.header("Connection");
            params.responseContentEncoding = response.header("Content-Encoding");
            String contentLength = response.header("Content-Length");
            params.responseContentLength = Long.parseLong(contentLength != null ? contentLength : "0");


            params.resourceStatus = response.code();
            if (params.resourceStatus >= HttpURLConnection.HTTP_BAD_REQUEST) {
                ResponseBody responseBody = response.body();
                if (HttpHeaders.hasBody(response)) {
                    if (responseBody != null) {
                        byte[] bytes = Utils.toByteArray(responseBody.byteStream());
                        MediaType contentType = responseBody.contentType();
                        responseBodyString = new String(bytes, Utils.getCharset(contentType));
                        ResponseBody copyResponseBody = ResponseBody.create(responseBody.contentType(), bytes);
                        response.close();

                        response = response.newBuilder().body(copyResponseBody).build();
                    }
                    params.responseBody = responseBodyString;

                    //如果 "Content-Length" 此处使用
                    if (params.responseContentLength == 0) {
                        params.responseContentLength = responseBodyString.length();
                        params.responseContentLength += responseHeaderString.length();
                    }
                } else {
                    LogUtils.d(TAG, "response body empty");
                }
            } else {
                //如果 "Content-Length" 没有数据则计算
                if (params.responseContentLength == 0) {
                    ResponseBody peekBody = response.peekBody(BYTE_LIMIT_COUNT);
                    params.responseContentLength = peekBody.contentLength();
                    params.responseContentLength += responseHeaderString.length();
                }
            }
        } else {
            if (handlerHelper != null) {
                handlerHelper.onException(exception, handlerHelper.extraData);
            }
        }

        if (handlerHelper != null) {
            if (params.property == null) {
                params.property = new HashMap<>();
            }
            params.property.putAll(handlerHelper.extraData);

        }


        FTRUMInnerManager.get().setTransformContent(resourceId, params);
        FTRUMInnerManager.get().stopResource(resourceId);

        if (exception != null) {
            throw new IOException(exception);
        }
        return response;
    }


}
