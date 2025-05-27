package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.ResourceID;
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
         * 返回网络链接过程中的异常, 会覆盖 SDK {@link com.ft.sdk.garble.bean.ErrorType#NETWORK} 类型的错误
         *
         * @param e         请求发生的 IOException 数据
         * @param extraData 附加数据
         * @deprecated Use {@link ContentHandlerHelperEx#onExceptionWithFilter(Exception, HashMap)} instead.
         */
        @Deprecated
        public void onException(Exception e, HashMap<String, Object> extraData) {

        }
    }

    /**
     * {@link ContentHandlerHelper } 的基础上，可以过滤本地错误类型
     */
    public static abstract class ContentHandlerHelperEx extends ContentHandlerHelper {
        /**
         * 返回网络链接过程中的异常
         *
         * @param e         请求发生的 IOException 数据
         * @param extraData 附加数据
         * @return 是否覆盖 SDK {@link com.ft.sdk.garble.bean.ErrorType#NETWORK} 类型的错误
         */
        public boolean onExceptionWithFilter(Exception e, HashMap<String, Object> extraData) {
            return true;
        }

    }

    public FTResourceInterceptor() {

    }

    public FTResourceInterceptor(ContentHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTResourceInterceptor";
    /**
     * 最大读取 byte 计算 32MB
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
        IOException exception = null;
        String url = request.url().toString();

        FTRUMConfig rumConfig = FTRUMConfigManager.get().getConfig();
        boolean isInTakeUrl = rumConfig == null || rumConfig
                .getResourceUrlHandler().isInTakeUrl(url);

        final HashMap<String, Object> extraData = (handlerHelper != null) ? new HashMap<>() : null;
        try {
            if (handlerHelper != null) {
                handlerHelper.onRequest(request, extraData);
            }
            response = chain.proceed(request);

            if (isInTakeUrl) {
                LogUtils.d(TAG, url + " , ignore by ResourceUrlHandler");
                return response;
            }

            if (handlerHelper != null) {
                handlerHelper.onResponse(response, extraData);
            }

        } catch (IOException e) {
            if (isInTakeUrl) {
                throw e;
            } else {
                exception = e;
            }
        }
        String resourceId = Utils.identifyRequest(request);
        if (request.tag(ResourceID.class) != null) {
            LogUtils.d(TAG, "intercept id:" + resourceId + ",url:" + request.url());
        } else {
            LogUtils.d(TAG, "intercept id:" + resourceId);
        }
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
            boolean override = false;
            if (handlerHelper != null) {
                if (handlerHelper instanceof ContentHandlerHelperEx) {
                    override = ((ContentHandlerHelperEx) handlerHelper)
                            .onExceptionWithFilter(exception, extraData);
                } else {
                    handlerHelper.onException(exception, extraData);
                    override = true;
                }
            }

            if (!override) {
                params.requestErrorStack = LogUtils.getStackTraceString(exception);
                params.requestErrorMsg = LogUtils.getNetworkExceptionDesc(exception);
            }
        }

        if (handlerHelper != null) {
            if (extraData != null) {
                if (params.property == null) {
                    params.property = new HashMap<>();
                }
                params.property.putAll(extraData);
            }
        }


        FTRUMInnerManager.get().setTransformContent(resourceId, params);
        FTRUMInnerManager.get().stopResource(resourceId);

        if (exception != null) {
            throw exception;
        }
        return response;
    }


}
