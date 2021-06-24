package com.ft.tests;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTHttpClientInterceptor;
import com.ft.sdk.FTHttpClientRequestInterceptor;
import com.ft.sdk.FTHttpClientResponseInterceptor;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.TraceType;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Request;

import static com.ft.AllTests.hasPrepare;
import static com.ft.sdk.FTTraceHandler.DD_TRACE_TRACE_ID_KEY;
import static com.ft.sdk.FTTraceHandler.JAEGER_KEY;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SAMPLED;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SPAN_ID;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_TRACE_ID;
import static com.ft.utils.RequestUtil.requestUrl;

/**
 * author: huangDianHua
 * time: 2020/8/26 13:54:56
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class TraceHeaderTest extends BaseTest {

    @BeforeClass
    public static void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        Context context = MockApplication.getContext();
        FTSDKConfig ftsdkConfig = FTSDKConfig
                .builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setXDataKitUUID("ft-dataKit-uuid-001");
        FTSdk.install(ftsdkConfig);
        FTSdk.initTraceWithConfig(new FTTraceConfig().setEnableLinkRUMData(false));

    }

    @Test
    public void traceZipKinHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(TraceType.ZIPKIN));
        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(ZIPKIN_SPAN_ID) &&
                request.headers().names().contains(ZIPKIN_TRACE_ID) &&
                request.headers().names().contains(ZIPKIN_SAMPLED);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceZipKinHeaderSpanIdFormatTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(TraceType.ZIPKIN));
        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        String spanId = request.headers().get(ZIPKIN_SPAN_ID);
        boolean spanIdResult = spanId.length() == 16;
        System.out.println(spanId);
        for (char c : spanId.toCharArray()) {
            if ((c > 57 && c < 'a') || c > 'e') {
                spanIdResult = false;
            }
        }
        Assert.assertTrue(spanIdResult);
    }

    @Test
    public void traceJaegerHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(TraceType.JAEGER));
        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(JAEGER_KEY);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceDDtraceHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(TraceType.DDTRACE));
        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(DD_TRACE_TRACE_ID_KEY);
        Assert.assertTrue(expect);
    }

//    @Test
//    public void traceSkyWalkingV3HeaderTest() {
//        ftsdkConfig.setTraceType(TraceType.SKYWALKING_V3);
//        FTSdk.install(ftsdkConfig);
//        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
//        boolean expect = request.headers().names().contains(SKYWALKING_V3_SW_8);
//        Assert.assertTrue(expect);
//    }
//
//    @Test
//    public void traceSkyWalkingV2HeaderTest() {
//        ftsdkConfig.setTraceType(TraceType.SKYWALKING_V2);
//        FTSdk.install(ftsdkConfig);
//        Request request = requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
//        boolean expect = request.headers().names().contains(SKYWALKING_V3_SW_6);
//        Assert.assertTrue(expect);
//    }

    @Test
    public void traceHttpClientGetTest() throws IOException, InterruptedException, ParseException {
        FTHttpClientInterceptor interceptor = new FTHttpClientInterceptor();
        CloseableHttpClient httpClient = HttpClients.custom()
                .addRequestInterceptorFirst(new FTHttpClientRequestInterceptor(interceptor))
                .addResponseInterceptorLast(new FTHttpClientResponseInterceptor(interceptor))
                .build();
        HttpGet httpGet = new HttpGet("http://www.weather.com.cn/data/sk/101010100.html");
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        System.out.println("response:" + EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));
        httpResponse.close();
        Thread.sleep(15000);
    }

    @Test
    public void traceHttpClientPostTest() throws Exception {
        resumeSyncTask();
        FTHttpClientInterceptor interceptor = new FTHttpClientInterceptor();
        CloseableHttpClient httpClient = HttpClients.custom()
                .addRequestInterceptorFirst(new FTHttpClientRequestInterceptor(interceptor))
                .addResponseInterceptorLast(new FTHttpClientResponseInterceptor(interceptor))
                .build();

        HttpPost httpPost = new HttpPost("https://www.tutorialspoint.com");
        EntityBuilder builder = EntityBuilder.create();
        builder.setParameters(new BasicNameValuePair("what", "ouHa.."));
        httpPost.setEntity(builder.build());
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        System.out.println("response:" + EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));
        httpResponse.close();
        Thread.sleep(15000);
    }
}
