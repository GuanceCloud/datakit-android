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
 * Record RUM Resource metrics data
 *
 * @author Brandon
 */
public class FTResourceInterceptor implements Interceptor {

    private ContentHandlerHelper handlerHelper;


    /**
     * Intercept OkHttp Request and Response,
     * through {@link #onRequest(Request, HashMap)}{@link  #onResponse(Response, HashMap)} extraData
     * append custom collected data
     */
    public abstract static class ContentHandlerHelper {
        /**
         * OKHttp Request
         *
         * @param request
         * @param extraData Additional data
         */
        public abstract void onRequest(Request request, HashMap<String, Object> extraData);

        /**
         * OKHttp Response
         *
         * @param response
         * @param extraData Additional data
         */
        public abstract void onResponse(Response response, HashMap<String, Object> extraData) throws IOException;

        /**
         * Return exceptions during network connection, will override SDK {@link com.ft.sdk.garble.bean.ErrorType#NETWORK} type errors
         *
         * @param e         IOException data that occurred during the request
         * @param extraData Additional data
         * @deprecated Use {@link ContentHandlerHelperEx#onExceptionWithFilter(Exception, HashMap)} instead.
         */
        @Deprecated
        public void onException(Exception e, HashMap<String, Object> extraData) {

        }
    }

    /**
     * Based on {@link ContentHandlerHelper }, can filter local error types, after using {@link ContentHandlerHelperEx},
     * {@link ContentHandlerHelperEx#onException} will no longer accept callbacks. By default, local network errors are not filtered
     */
    public static abstract class ContentHandlerHelperEx extends ContentHandlerHelper {
        /**
         * Return exceptions during network connection
         *
         * @param e         IOException data that occurred during the request
         * @param extraData Additional data
         * @return Whether to filter local network {@link com.ft.sdk.garble.bean.ErrorType#NETWORK} type errors. true means filter
         */
        public boolean onExceptionWithFilter(Exception e, HashMap<String, Object> extraData) {
            return false;
        }

    }

    public FTResourceInterceptor() {

    }

    public FTResourceInterceptor(ContentHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTResourceInterceptor";
    /**
     * Maximum read byte calculation 32MB
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
        boolean isInTakeUrl = rumConfig.isRumEnable() && rumConfig
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
        boolean reGenerate = false;
        if (request.tag(ResourceID.class) != null) {
            LogUtils.d(TAG, "intercept id:" + resourceId + ",url:" + request.url());
        } else {
            LogUtils.d(TAG, "intercept id:" + resourceId);
            reGenerate = true;
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

                    //If "Content-Length" is used here
                    if (params.responseContentLength == 0) {
                        params.responseContentLength = responseBodyString.length();
                        params.responseContentLength += responseHeaderString.length();
                    }
                } else {
                    LogUtils.d(TAG, "response body empty");
                }
            } else {
                //If "Content-Length" is not data then calculate
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


        FTRUMInnerManager.get().setTransformContent(resourceId, params, reGenerate);
        FTRUMInnerManager.get().stopResource(resourceId);

        if (exception != null) {
            throw exception;
        }
        return response;
    }


}
