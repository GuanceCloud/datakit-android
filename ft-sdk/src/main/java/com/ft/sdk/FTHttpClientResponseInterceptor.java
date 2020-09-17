package com.ft.sdk;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * author: huangDianHua
 * time: 2020/9/16 10:11:14
 * description: HttpClient Response Interceptor
 */
public class FTHttpClientResponseInterceptor implements HttpResponseInterceptor {
    private FTHttpClientInterceptor interceptor;
    public FTHttpClientResponseInterceptor(FTHttpClientInterceptor interceptor){
        this.interceptor = interceptor;
    }
    @Override
    public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        if (interceptor != null) {
            interceptor.process(response,entity,context);
        }
    }
}
