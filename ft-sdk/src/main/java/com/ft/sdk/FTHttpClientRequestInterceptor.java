package com.ft.sdk;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * author: huangDianHua
 * time: 2020/9/16 10:08:18
 * description: HttpClient Request Interceptor
 */
public class FTHttpClientRequestInterceptor implements HttpRequestInterceptor {
    private FTHttpClientInterceptor interceptor;
    public FTHttpClientRequestInterceptor(FTHttpClientInterceptor interceptor){
        this.interceptor = interceptor;
    }
    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        if (interceptor != null) {
            interceptor.process(request,entity,context);
        }
    }
}
