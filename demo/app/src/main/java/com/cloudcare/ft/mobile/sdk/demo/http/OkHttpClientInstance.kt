package com.cloudcare.ft.mobile.sdk.demo.http

import okhttp3.OkHttpClient
import okhttp3.Protocol

object OkHttpClientInstance {

    private const val READ_TIMEOUT = 30000L
    private const val CONNECT_TIMEOUT = 10000L


    private var okHttpClient: OkHttpClient? = null

    fun get(): OkHttpClient {
        if (okHttpClient == null || (okHttpClient?.connectionPool?.connectionCount() ?: 0) > 0) {
            createOkHttpClient()
        }
        return okHttpClient!!
    }

    private fun createOkHttpClient() {
        val list = ArrayList<Protocol>()
        list.add(Protocol.HTTP_1_1)
        list.add(Protocol.HTTP_2)

        val builder = OkHttpClient.Builder()
        builder.protocols(list)
            .connectTimeout(CONNECT_TIMEOUT, java.util.concurrent.TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT, java.util.concurrent.TimeUnit.MILLISECONDS)

        okHttpClient = builder.build()
    }
}