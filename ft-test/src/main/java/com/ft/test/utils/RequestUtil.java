package com.ft.test.utils;

import androidx.annotation.NonNull;

import com.ft.sdk.FTResourceEventListener;
import com.ft.sdk.FTResourceInterceptor;
import com.ft.sdk.FTTraceInterceptor;
import com.ft.sdk.garble.http.RequestMethod;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author: huangDianHua
 * time: 2020/9/1 17:47:53
 * description:发起一个简单的请求
 */
public class RequestUtil {
    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new FTTraceInterceptor())
            .addInterceptor(new FTResourceInterceptor())
            .eventListenerFactory(new FTResourceEventListener.FTFactory())
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     *  发起一个 http 请求
     * @param url 请求地址
     * @return
     */
    public static Request okhttpRequestUrl(@NonNull String url) {
        Request.Builder builder = new Request.Builder().url(url)
                .method(RequestMethod.GET.name(), null);
        Request request = null;
        try {
            Response response = client.newCall(builder.build()).execute();
            request = response.request();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

}
