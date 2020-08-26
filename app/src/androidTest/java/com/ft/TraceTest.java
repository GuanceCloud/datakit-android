package com.ft;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTNetWorkTracerInterceptor;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.http.RequestMethod;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.ft.sdk.FTNetWorkTracerInterceptor.JAEGER_KEY;
import static com.ft.sdk.FTNetWorkTracerInterceptor.SKYWALKING_V3_SW_6;
import static com.ft.sdk.FTNetWorkTracerInterceptor.SKYWALKING_V3_SW_8;
import static com.ft.sdk.FTNetWorkTracerInterceptor.ZIPKIN_SAMPLED;
import static com.ft.sdk.FTNetWorkTracerInterceptor.ZIPKIN_SPAN_ID;
import static com.ft.sdk.FTNetWorkTracerInterceptor.ZIPKIN_TRACE_ID;

/**
 * author: huangDianHua
 * time: 2020/8/26 13:54:56
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class TraceTest {
    Context context;
    static boolean hasPrepare;
    FTSDKConfig ftsdkConfig;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = DemoApplication.getContext();
        ftsdkConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setNetworkTrace(true);
    }

    @Test
    public void traceZipKinHeaderTest() {
        ftsdkConfig.setTraceType(TraceType.ZIPKIN);
        FTSdk.install(ftsdkConfig);
        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(ZIPKIN_SPAN_ID) &&
                request.headers().names().contains(ZIPKIN_TRACE_ID) &&
                request.headers().names().contains(ZIPKIN_SAMPLED);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceJaegerHeaderTest() {
        ftsdkConfig.setTraceType(TraceType.JAEGER);
        FTSdk.install(ftsdkConfig);
        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(JAEGER_KEY);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceSkyWalkingV3HeaderTest() {
        ftsdkConfig.setTraceType(TraceType.SKYWALKING_V3);
        FTSdk.install(ftsdkConfig);
        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(SKYWALKING_V3_SW_8);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceSkyWalkingV2HeaderTest() {
        ftsdkConfig.setTraceType(TraceType.SKYWALKING_V2);
        FTSdk.install(ftsdkConfig);
        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(SKYWALKING_V3_SW_6);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceNormalNetworkTest(){
        FTSdk.install(ftsdkConfig);
        Response response = requestUrlResponse("http://www.weather.com.cn/data/sk/101010100.html");
        Assert.assertEquals(HttpURLConnection.HTTP_OK,response.code());
    }

    /**
     * 通过查看输出的异常信息
     */
    @Test
    public void traceTimeoutNetworkTest(){
        FTSdk.install(ftsdkConfig);
        requestUrlResponse("https://www.google.com");
    }

    /**
     * 通过查看输出的异常信息
     */
    @Test
    public void traceErrorNetworkTest(){
        FTSdk.install(ftsdkConfig);
        requestUrlResponse("https://error.url");
    }


    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new FTNetWorkTracerInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    private Request requestUrl(@NonNull String url) {
        Request.Builder builder = new Request.Builder().url(url)
                .method(RequestMethod.GET.name(), null);
        Request request =null;
        try {
            Response response = client.newCall(builder.build()).execute();
            request = response.request();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

    private Response requestUrlResponse(@NonNull String url) {
        Request.Builder builder = new Request.Builder().url(url)
                .method(RequestMethod.GET.name(), null);
        Response response =null;
        try {
            response = client.newCall(builder.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}