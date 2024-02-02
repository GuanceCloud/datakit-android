package com.ft.tests;

import static com.ft.AllTests.hasPrepare;
import static com.ft.sdk.FTTraceHandler.DD_TRACE_TRACE_ID_KEY;
import static com.ft.sdk.FTTraceHandler.JAEGER_KEY;
import static com.ft.sdk.FTTraceHandler.SKYWALKING_V3_SW_8;
import static com.ft.sdk.FTTraceHandler.W3C_TRACEPARENT_KEY;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_B3_HEADER;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SAMPLED;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SPAN_ID;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_TRACE_ID;
import static com.ft.test.utils.RequestUtil.okhttpRequestUrl;

import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BaseTest;
import com.ft.BuildConfig;
import com.ft.DebugMainActivity;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.TraceType;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.Request;

/**
 * author: huangDianHua
 * time: 2020/8/26 13:54:56
 * description: 网络请求 Trace Header 正确性检测
 */
@RunWith(AndroidJUnit4.class)
public class TraceHeaderTest extends BaseTest {
    private static  final  String TEST_URL ="https://www.guance.com";

    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);

    @BeforeClass
    public static void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        FTSDKConfig ftsdkConfig = FTSDKConfig
                .builder(BuildConfig.DATAKIT_URL);
        FTSdk.install(ftsdkConfig);
        FTSdk.initTraceWithConfig(new FTTraceConfig().setEnableLinkRUMData(false));

    }

    /**
     * {@link TraceType#ZIPKIN_MULTI_HEADER}
     */
    @Test
    public void traceZipKinMultiHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig().setEnableAutoTrace(true).setTraceType(TraceType.ZIPKIN_MULTI_HEADER));
        Request request = okhttpRequestUrl(TEST_URL);
        boolean expect = request.headers().names().contains(ZIPKIN_SPAN_ID) &&
                request.headers().names().contains(ZIPKIN_TRACE_ID) &&
                request.headers().names().contains(ZIPKIN_SAMPLED);
        Assert.assertTrue(expect);
    }

    /**
     * {@link TraceType#ZIPKIN_SINGLE_HEADER}
     */
    @Test
    public void traceZipKinSingleHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig().setEnableAutoTrace(true).setTraceType(TraceType.ZIPKIN_SINGLE_HEADER));
        Request request = okhttpRequestUrl(TEST_URL);
        boolean expect = request.headers().names().contains(ZIPKIN_B3_HEADER);
        Assert.assertTrue(expect);
    }

    /**
     * {@link TraceType#JAEGER}
     */
    @Test
    public void traceJaegerHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.JAEGER));
        Request request = okhttpRequestUrl(TEST_URL);
        boolean expect = request.headers().names().contains(JAEGER_KEY);
        Assert.assertTrue(expect);
    }

    /**
     * {@link TraceType#DDTRACE}
     */
    @Test
    public void traceDDtraceHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.DDTRACE));
        Request request = okhttpRequestUrl(TEST_URL);
        boolean expect = request.headers().names().contains(DD_TRACE_TRACE_ID_KEY);
        Assert.assertTrue(expect);
    }

    /**
     * {@link TraceType#SKYWALKING}
     */
    @Test
    public void traceSkyWalkingV3HeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.SKYWALKING));
        Request request = okhttpRequestUrl(TEST_URL);
        boolean expect = request.headers().names().contains(SKYWALKING_V3_SW_8);
        Assert.assertTrue(expect);
    }

    /**
     * {@link TraceType#TRACEPARENT}
     */
    @Test
    public void traceW3CTraceParentTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.TRACEPARENT));
        Request request = okhttpRequestUrl(TEST_URL);
        boolean expect = request.headers().names().contains(W3C_TRACEPARENT_KEY);
        Assert.assertTrue(expect);
    }

    @Override
    public void tearDown() {

    }
}
