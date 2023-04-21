package com.cloudcare.ft.mobile.sdk.custom.okhttp

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 自定义实现拦截器，如果需要对请求数据二次处理，请求 Interceptor 放置到最后，否则可能会发生丢失 Resource 数据问题
 * @see com.cloudcare.ft.mobile.sdk.demo.ManualActivity
 */
class CustomInterceptor : okhttp3.Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //在这里实现你的方法
        return chain.proceed(chain.request())
    }
}