package com.ft.tests;

import static com.ft.AllTests.hasPrepare;
import static com.ft.sdk.FTTraceHandler.DD_TRACE_TRACE_ID_KEY;
import static com.ft.sdk.FTTraceHandler.JAEGER_KEY;
import static com.ft.sdk.FTTraceHandler.SKYWALKING_V3_SW_8;
import static com.ft.sdk.FTTraceHandler.W3C_TRACEPARENT_KEY;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SAMPLED;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SPAN_ID;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_TRACE_ID;
import static com.ft.test.utils.RequestUtil.okhttpRequestUrl;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.TraceType;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Request;

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
        FTSdk.initTraceWithConfig(new FTTraceConfig().setEnableAutoTrace(true).setTraceType(TraceType.ZIPKIN_MULTI_HEADER));
        Request request = okhttpRequestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(ZIPKIN_SPAN_ID) &&
                request.headers().names().contains(ZIPKIN_TRACE_ID) &&
                request.headers().names().contains(ZIPKIN_SAMPLED);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceJaegerHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.JAEGER));
        Request request = okhttpRequestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(JAEGER_KEY);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceDDtraceHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.DDTRACE));
        Request request = okhttpRequestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(DD_TRACE_TRACE_ID_KEY);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceSkyWalkingV3HeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.SKYWALKING));
        Request request = okhttpRequestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(SKYWALKING_V3_SW_8);
        Assert.assertTrue(expect);
    }

    @Test
    public void traceW3CTraceParentTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.TRACEPARENT));
        Request request = okhttpRequestUrl("http://www.weather.com.cn/data/sk/101010100.html");
        boolean expect = request.headers().names().contains(W3C_TRACEPARENT_KEY);
        Assert.assertTrue(expect);
    }

}
