package com.ft.sdk.garble.http;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.compress.DeflateInterceptor;
import com.ft.sdk.garble.compress.GzipInterceptor;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:51:48
 * description: 基于 okhttp 请求框架的 INetEngine
 */
public class OkHttpEngine implements INetEngine {
    private static OkHttpClient client;
    private Request request;

    /**
     * Http 请求基础配置初始化
     *
     * @param httpBuilder
     */
    @Override
    public void defaultConfig(HttpBuilder httpBuilder) {
        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            switch (FTHttpConfigManager.get().getCompressType()) {
                case GZIP:
                    builder.addInterceptor(new GzipInterceptor());
                    break;
                case DEFLATE:
                    builder.addInterceptor(new DeflateInterceptor());
                    break;
                case NONE:
                default:
            }
            client = builder
                    .connectTimeout(httpBuilder.getSendOutTime(), TimeUnit.MILLISECONDS)
                    .readTimeout(httpBuilder.getReadOutTime(), TimeUnit.MILLISECONDS)
                    .build();
        }
    }

    /**
     * 创建请求对象
     *
     * @param httpBuilder
     */
    @Override
    public void createRequest(HttpBuilder httpBuilder) {
        RequestBody requestBody = null;
        if (httpBuilder.getMethod() == RequestMethod.POST) {
            requestBody = RequestBody.create(null, httpBuilder.getBodyString());
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
     * 执行 http 请求，如果 http 正常，返回  code 和 body
     * code = {@link NetCodeStatus#FILE_IO_EXCEPTION_CODE}，IOException
     * code = {@link NetCodeStatus#UNKNOWN_EXCEPTION_CODE}，Exception
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
            return new FTResponseData(NetCodeStatus.FILE_TIMEOUT_CODE, e.getLocalizedMessage() + ",网络超时");
        } catch (IOException e) {
            return new FTResponseData(NetCodeStatus.FILE_IO_EXCEPTION_CODE, e.getLocalizedMessage() + ",检查本地网络连接是否正常");
        } catch (Exception e) {
            return new FTResponseData(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getLocalizedMessage());
        }
    }
}
