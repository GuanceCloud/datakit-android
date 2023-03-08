package com.ft.sdk.garble.http;

import android.util.Log;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.utils.NetUtils;

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
    private static OkHttpClient client;
    private Request request;

    @Override
    public void defaultConfig(HttpBuilder httpBuilder) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(httpBuilder.getSendOutTime(), TimeUnit.MILLISECONDS)
                    .readTimeout(httpBuilder.getReadOutTime(), TimeUnit.MILLISECONDS)
                    .build();
        }
    }

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

    @Override
    public ResponseData execute() {
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String string = "";
            if (responseBody != null) {
                string = responseBody.string();
            }
            return new ResponseData(response.code(), string);
        } catch (IOException e) {
            Log.e("FT-SDK", e.getLocalizedMessage() + ",检查本地网络连接是否正常");
            return new ResponseData(103, e.getLocalizedMessage() + ",检查本地网络连接是否正常");
        } catch (Exception e) {
            return new ResponseData(104, e.getLocalizedMessage());
        }
    }
}
