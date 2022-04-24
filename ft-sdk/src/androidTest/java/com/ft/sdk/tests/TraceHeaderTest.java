package com.ft.sdk.tests;

import static com.ft.sdk.FTTraceHandler.DD_TRACE_ORIGIN_KEY;
import static com.ft.sdk.FTTraceHandler.DD_TRACE_PARENT_SPAN_ID_KEY;
import static com.ft.sdk.FTTraceHandler.DD_TRACE_SAMPLED;
import static com.ft.sdk.FTTraceHandler.DD_TRACE_SAMPLING_PRIORITY_KEY;
import static com.ft.sdk.FTTraceHandler.JAEGER_KEY;
import static com.ft.sdk.FTTraceHandler.SKYWALKING_V3_SW_8;
import static com.ft.sdk.FTTraceHandler.W3C_TRACEPARENT_KEY;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SAMPLED;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_SPAN_ID;
import static com.ft.sdk.FTTraceHandler.ZIPKIN_TRACE_ID;

import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.FTTraceHandler;
import com.ft.sdk.FTTraceManager;
import com.ft.sdk.TraceType;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;


public class TraceHeaderTest {

    @Test
    public void traceZipKinHeaderTest() {
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

    @Test
    public void traceZipKinSingleHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.ZIPKIN_SINGLE_HEADER));

        String header = getHeaders().get(FTTraceHandler.ZIPKIN_B3_HEADER);
        Assert.assertNotNull(header);
        Assert.assertFalse(header.isEmpty());
    }

    @Test
    public void traceJaegerHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.JAEGER));

        String header = getHeaders().get(JAEGER_KEY);
        Assert.assertNotNull(header);
        Assert.assertFalse(header.isEmpty());
    }

    @Test
    public void traceDDtraceHeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.DDTRACE));
        HashMap<String, String> headerMap = getHeaders();

        String traceId = headerMap.get(DD_TRACE_PARENT_SPAN_ID_KEY);
        String sample = headerMap.get(DD_TRACE_SAMPLED);
        String key = headerMap.get(DD_TRACE_ORIGIN_KEY);
        String samplingPriorityKey = headerMap.get(DD_TRACE_SAMPLING_PRIORITY_KEY);
        Assert.assertNotNull(traceId);
        Assert.assertNotNull(sample);
        Assert.assertNotNull(samplingPriorityKey);
        Assert.assertNotNull(key);
        Assert.assertFalse(traceId.isEmpty());
        Assert.assertFalse(sample.isEmpty());
        Assert.assertFalse(key.isEmpty());
        Assert.assertFalse(samplingPriorityKey.isEmpty());

    }

    @Test
    public void traceSkyWalkingV3HeaderTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.SKYWALKING));

        String header = getHeaders().get(SKYWALKING_V3_SW_8);
        Assert.assertNotNull(header);
        Assert.assertFalse(header.isEmpty());
    }

    @Test
    public void traceW3CTraceParentTest() {
        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setTraceType(TraceType.TRACEPARENT));

        String header = getHeaders().get(W3C_TRACEPARENT_KEY);
        Assert.assertNotNull(header);
        Assert.assertFalse(header.isEmpty());
    }


    private HashMap<String, String> getHeaders() {
        String key = "uuid";
        HashMap<String, String> map = null;
        try {
            map = FTTraceManager.get()
                    .getTraceHeader(key, "https://www.baidu.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return map;

    }
}
