package com.ft.sdk.tests;

import static com.ft.sdk.FTTraceHandler.DD_TRACE_ORIGIN_KEY;
import static com.ft.sdk.FTTraceHandler.DD_TRACE_PARENT_SPAN_ID_KEY;
import static com.ft.sdk.FTTraceHandler.DD_TRACE_SAMPLING_PRIORITY_KEY;
import static com.ft.sdk.FTTraceHandler.JAEGER_KEY;
import static com.ft.sdk.FTTraceHandler.SKYWALKING_V3_SW_8;
import static com.ft.sdk.FTTraceHandler.W3C_TRACEPARENT_KEY;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SAMPLED;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SPAN_ID;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_TRACE_ID;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.FTTraceHandler;
import com.ft.sdk.FTTraceManager;
import com.ft.sdk.TraceType;
import com.ft.test.base.FTBaseTest;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * Trace 链路 propagation header 测试
 *
 * @author Brandon
 */
public class TraceHeaderTest extends FTBaseTest {

    /**
     * {@link  TraceType#ZIPKIN_MULTI_HEADER} 类型测试
     */
    @Test
    public void traceZipKinHeaderTest() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.ZIPKIN_MULTI_HEADER));
        HashMap<String, String> headerMap = getHeaders();
        String traceId = headerMap.get(ZIPKIN_TRACE_ID);
        String sample = headerMap.get(ZIPKIN_SAMPLED);
        String spanId = headerMap.get(ZIPKIN_SPAN_ID);
        Assert.assertNotNull(traceId);
        Assert.assertNotNull(sample);
        Assert.assertNotNull(spanId);
        Assert.assertFalse(traceId.isEmpty());
        Assert.assertFalse(sample.isEmpty());
        Assert.assertFalse(spanId.isEmpty());

    }

    /**
     * {@link  TraceType#ZIPKIN_SINGLE_HEADER} 类型测试
     */
    @Test
    public void traceZipKinSingleHeaderTest() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.ZIPKIN_SINGLE_HEADER));

        String header = getHeaders().get(FTTraceHandler.ZIPKIN_B3_HEADER);
        Assert.assertNotNull(header);
        Assert.assertFalse(header.isEmpty());
    }

    /**
     * {@link  TraceType#JAEGER} 类型测试
     */
    @Test
    public void traceJaegerHeaderTest() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.JAEGER));

        String header = getHeaders().get(JAEGER_KEY);
        Assert.assertNotNull(header);
        Assert.assertFalse(header.isEmpty());
    }

    /**
     * {@link  TraceType#DDTRACE} 类型测试
     */
    @Test
    public void traceDDtraceHeaderTest() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.DDTRACE));
        HashMap<String, String> headerMap = getHeaders();

        String traceId = headerMap.get(DD_TRACE_PARENT_SPAN_ID_KEY);
        String key = headerMap.get(DD_TRACE_ORIGIN_KEY);
        String samplingPriorityKey = headerMap.get(DD_TRACE_SAMPLING_PRIORITY_KEY);
        Assert.assertNotNull(traceId);
        Assert.assertNotNull(samplingPriorityKey);
        Assert.assertNotNull(key);
        Assert.assertFalse(traceId.isEmpty());
        Assert.assertFalse(key.isEmpty());
        Assert.assertFalse(samplingPriorityKey.isEmpty());

    }

    /**
     * {@link  TraceType#SKYWALKING} 类型测试
     */
    @Test
    public void traceSkyWalkingV3HeaderTest() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.SKYWALKING));

        String header = getHeaders().get(SKYWALKING_V3_SW_8);
        Assert.assertNotNull(header);
        Assert.assertFalse(header.isEmpty());
    }

    /**
     * {@link  TraceType#TRACEPARENT} 类型测试
     */
    @Test
    public void traceW3CTraceParentTest() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.TRACEPARENT));

        String header = getHeaders().get(W3C_TRACEPARENT_KEY);
        Assert.assertNotNull(header);
        Assert.assertFalse(header.isEmpty());
    }

    /**
     * 获取 http 请求的所有头参数
     *
     * @return
     */
    private HashMap<String, String> getHeaders() {
        String key = "uuid";
        return FTTraceManager.get().getTraceHeader(key, "https://www.test.url");

    }
}
