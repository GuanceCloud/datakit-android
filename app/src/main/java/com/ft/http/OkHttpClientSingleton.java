package com.ft.http;

import com.ft.sdk.FTResourceEventListener;
import com.ft.sdk.FTResourceInterceptor;
import com.ft.sdk.FTTraceInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Okhttp 测试对象单例管理
 */
public class OkHttpClientSingleton {
    private static OkHttpClient instance;

    private OkHttpClientSingleton() {
    }

    public static synchronized OkHttpClient getInstance() {
        if (instance == null) {
            // connect time 10 秒
            instance = new OkHttpClient.Builder()
//                    .eventListener(new EventListener() {
//                        @Override
//                        public void callStart(@NotNull Call call) {
//                            super.callStart(call);
//                            LogUtils.d("RequestUtil", "custom EventListener");
//                        }
//                    })
                    .connectTimeout(10, TimeUnit.SECONDS).build();
        }
        return instance;
    }

    public static synchronized OkHttpClient getInstanceBeforeSDKInit() {
        if (instance == null) {
            // connect time 10 秒
            instance = new OkHttpClient.Builder()
                    .addInterceptor(new FTResourceInterceptor())
                    .addInterceptor(new FTTraceInterceptor())
                    .eventListenerFactory(new FTResourceEventListener.FTFactory())
                    .connectTimeout(10, TimeUnit.SECONDS).build();
        }
        return instance;
    }
}
