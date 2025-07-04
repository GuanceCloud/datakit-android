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
 * description: Initiate a simple request
 */
public class RequestUtil {
    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new FTTraceInterceptor())
            .addInterceptor(new FTResourceInterceptor())
            .eventListenerFactory(new FTResourceEventListener.FTFactory())
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     *  Initiate an HTTP request
     * @param url Request URL
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
