package com.ft.sdk.garble.http;

import android.util.Log;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.IOException;
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
    private final static String TAG = Constants.LOG_TAG_PREFIX + "OkHttpEngine";

    private static OkHttpClient client;
    private Request request;

    /**
     * Http 请求基础配置初始化
     * @param httpBuilder
     */
    @Override
    public void defaultConfig(HttpBuilder httpBuilder) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(httpBuilder.getSendOutTime(), TimeUnit.MILLISECONDS)
                    .readTimeout(httpBuilder.getReadOutTime(), TimeUnit.MILLISECONDS)
                    .build();
        }
    }

    /**
     * 创建请求对象
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
        request = new Request.Builder()
                .url(httpBuilder.getUrl())
                .method(httpBuilder.getMethod().name(), requestBody)
                .headers(builder.build())
                .build();
    }

    /**
     * 执行 http 请求，如果 http 正常，返回  code 和 body code = 103
     *
     * code = 103，IOException
     * code = 104，Exception
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
        } catch (IOException e) {
            LogUtils.e(TAG, e.getLocalizedMessage() + ",检查本地网络连接是否正常");
            return new FTResponseData(NetCodeStatus.FILE_IO_EXCEPTION_CODE, e.getLocalizedMessage() + ",检查本地网络连接是否正常");
        } catch (Exception e) {
            return new FTResponseData(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getLocalizedMessage());
        }
    }
}
