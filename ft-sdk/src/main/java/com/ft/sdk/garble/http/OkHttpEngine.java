package com.ft.sdk.garble.http;

import android.util.Pair;

import com.ft.sdk.garble.compress.DeflateInterceptor;
import com.ft.sdk.garble.utils.Constants;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Dns;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:51:48
 * description: INetEngine based on okhttp request framework
 */
public class OkHttpEngine implements INetEngine {
    private static OkHttpClient client;
    private Request request;

    /**
     * Http request basic configuration initialization
     *
     * @param httpBuilder
     */
    @Override
    public void defaultConfig(HttpBuilder httpBuilder) {
        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (httpBuilder.getHttpConfig().isCompressIntakeRequests()) {
                builder.addInterceptor(new DeflateInterceptor());
            }
            if (httpBuilder.getHttpConfig().getDns() != null) {
                builder.dns((Dns) httpBuilder.getHttpConfig().getDns());
            } else {
                builder.dns(new RotatingDnsResolver());
            }
            if (httpBuilder.getHttpConfig().getProxy() != null) {
                builder.proxy(httpBuilder.getHttpConfig().getProxy());
            }
            if (httpBuilder.getHttpConfig().getAuthenticator() != null) {
                builder.proxyAuthenticator((Authenticator) httpBuilder.getHttpConfig().getAuthenticator());
            }
            client = builder
                    .connectTimeout(httpBuilder.getSendOutTime(), TimeUnit.MILLISECONDS)
                    .readTimeout(httpBuilder.getReadOutTime(), TimeUnit.MILLISECONDS)
                    .build();
        }
    }

    /**
     * Create request object
     *
     * @param httpBuilder
     */
    @Override
    public void createRequest(HttpBuilder httpBuilder) {
        RequestBody requestBody = null;
        HashMap<String, String> headers = httpBuilder.getHeadParams();
        String contentType = headers.get(Constants.SYNC_DATA_CONTENT_TYPE_HEADER);
        if (httpBuilder.getMethod() == RequestMethod.POST) {
            if ("multipart/form-data".equalsIgnoreCase(contentType)) {
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);

                // Add form field
                HashMap<String, String> formFields = httpBuilder.getFormParams();
                for (Map.Entry<String, String> field : formFields.entrySet()) {
                    multipartBuilder.addFormDataPart(field.getKey(), field.getValue());
                }

                for (Map.Entry<String, Pair<String, byte[]>> fileFields : httpBuilder.getFileParams().entrySet()) {
                    RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"),
                            fileFields.getValue().second);
                    multipartBuilder.addFormDataPart(fileFields.getKey(), fileFields.getValue().first,
                            fileBody);
                }
                // Add file part
                requestBody = multipartBuilder.build();
            } else {
                // Process normal text request
                requestBody = RequestBody.create(null, httpBuilder.getBodyString());
            }
        }
        Headers.Builder builder = new Headers.Builder();
        HashMap<String, String> hashMap = httpBuilder.getHeadParams();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        Request.Builder requestBuilder = new Request.Builder();


        request = requestBuilder
                .url(httpBuilder.getUrl())
                .method(httpBuilder.getMethod().name(), requestBody)
                .headers(builder.build())
                .build();
    }

    /**
     * Execute http request, if http is normal, return code and body
     * code = {@link NetCodeStatus#FILE_IO_EXCEPTION_CODE}, IOException
     * code = {@link NetCodeStatus#UNKNOWN_EXCEPTION_CODE}, Exception
     *
     * @return
     */
    @Override
    public FTResponseData execute() {
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String string = "";
            if (responseBody != null) {
                string = responseBody.string();
            }
            return new FTResponseData(response.code(), string);
        } catch (SocketTimeoutException e) {
            return new FTResponseData(NetCodeStatus.FILE_TIMEOUT_CODE, e.getLocalizedMessage() + ",Time out");
        } catch (IOException e) {
            return new FTResponseData(NetCodeStatus.FILE_IO_EXCEPTION_CODE, e.getLocalizedMessage() + ",Check Local Network");
        } catch (Exception e) {
            return new FTResponseData(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getLocalizedMessage());
        }
    }
}
