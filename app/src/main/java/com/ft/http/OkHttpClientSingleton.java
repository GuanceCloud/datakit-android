package com.ft.http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 *  Okhttp 测试对象单例管理
 */
public class OkHttpClientSingleton {
    private static OkHttpClient instance;

    private OkHttpClientSingleton() {
    }

    public static synchronized OkHttpClient getInstance() {
        if (instance == null) {
            // connect time 10 秒
            instance = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS).build();
        }
        return instance;
    }
}
