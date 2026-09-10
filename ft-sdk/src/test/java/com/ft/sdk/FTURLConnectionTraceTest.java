package com.ft.sdk;

import org.junit.After;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FTURLConnectionTraceTest {
    @After public void releaseTraceConfig() {
        FTTraceConfigManager.get().release();
    }

    private static void configure(TraceType type, boolean automatic, float rate) {
        FTTraceConfigManager.get().initWithConfig(new FTTraceConfig().setTraceType(type)
                .setEnableAutoTrace(automatic).setEnableLinkRUMData(true).setSamplingRate(rate));
    }

    private static HashMap<String, String> headers(HttpURLConnection connection) {
        HashMap<String, String> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : connection.getRequestProperties().entrySet()) {
            result.put(entry.getKey(), entry.getValue().get(0));
        }
        return result;
    }

    @Test public void injectsConfiguredContextOnceBeforeIoAndCapturesHeaders() throws Exception {
        for (TraceType type : new TraceType[]{TraceType.DDTRACE, TraceType.TRACEPARENT,
                TraceType.ZIPKIN_MULTI_HEADER, TraceType.ZIPKIN_SINGLE_HEADER, TraceType.JAEGER}) {
            configure(type, true, 1f);
            FTURLConnectionTest.Fixture f = new FTURLConnectionTest.Fixture();
            f.wrapped.setRequestProperty("X-App", "preserved");
            assertNull(f.state.traceId);
            f.wrapped.getResponseCode();
            HashMap<String, String> sent = headers(f.original);
            TraceContext context = TraceContext.Simple.fromTraceType(sent);
            assertNotNull(type.name(), context);
            assertFalse(context.getTraceId().isEmpty());
            assertFalse(context.getSpanId().isEmpty());
            assertEquals(context.getTraceId(), f.state.traceId);
            assertEquals(context.getSpanId(), f.state.spanId);
            assertEquals("preserved", sent.get("X-App"));
            assertEquals(f.original.getRequestProperties(), f.state.params.requestHeaderMap);
            f.wrapped.connect(); f.wrapped.getResponseCode(); f.wrapped.getInputStream();
            assertEquals(sent, headers(f.original));
            assertEquals(1, f.recorder.starts);
            // Context is published only when a real Resource is completed, avoiding the map TTL.
            assertNull(FTTraceManager.get().getHandler(f.state.id));
            f.wrapped.disconnect();
        }
    }

    @Test public void zeroSamplingStillPropagatesIdsWithUnsampledDecision() throws Exception {
        configure(TraceType.TRACEPARENT, true, 0f);
        FTURLConnectionTest.Fixture f = new FTURLConnectionTest.Fixture();
        f.wrapped.getResponseCode();
        assertEquals("00-" + f.state.traceId + "-" + f.state.spanId + "-00",
                f.original.getRequestProperty("traceparent"));
        configure(TraceType.DDTRACE, true, 0f);
        f = new FTURLConnectionTest.Fixture(); f.wrapped.getResponseCode();
        assertNotNull(f.state.traceId);
        assertEquals("-1", f.original.getRequestProperty("x-datadog-sampling-priority"));
    }

    @Test public void disabledTracePreservesApplicationContextWithoutClaimingLink() throws Exception {
        configure(TraceType.TRACEPARENT, false, 1f);
        FTURLConnectionTest.Fixture f = new FTURLConnectionTest.Fixture();
        f.wrapped.setRequestProperty("traceparent", "application-context");
        f.wrapped.getResponseCode();
        assertEquals("application-context", f.original.getRequestProperty("traceparent"));
        assertNull(f.state.traceId); assertNull(f.state.spanId);
    }

    @Test public void enabledTraceReplacesSameFormatHeadersLikeOkHttp() throws Exception {
        configure(TraceType.TRACEPARENT, true, 1f);
        FTURLConnectionTest.Fixture f = new FTURLConnectionTest.Fixture();
        f.wrapped.setRequestProperty("traceparent", "old-context");
        f.wrapped.getResponseCode();
        assertEquals("00-" + f.state.traceId + "-" + f.state.spanId + "-01",
                f.original.getRequestProperty("traceparent"));
        assertEquals(1, f.original.getRequestProperties().get("traceparent").size());
    }

    @Test public void rejectedHeaderDoesNotReplaceBusinessIoOrCreateLink() throws Exception {
        configure(TraceType.DDTRACE, true, 1f);
        FTURLConnectionTest.Connection original = new FTURLConnectionTest.Connection() {
            @Override public void setRequestProperty(String key, String value) {
                throw new IllegalStateException("already connected");
            }
        };
        FTURLConnectionTest.Recorder recorder = new FTURLConnectionTest.Recorder();
        FTURLConnectionRequestState state = new FTURLConnectionRequestState(
                original.getURL().toString(), 0, recorder);
        HttpURLConnection wrapped = new FTHttpURLConnection(original, new FTURLConnectionDelegate(original, state));
        try (InputStream input = wrapped.getInputStream()) { while (input.read() != -1) { } }
        assertEquals(1, recorder.finishes);
        assertFalse(recorder.params.forceNetworkError);
        assertNull(state.traceId); assertNull(state.spanId);
    }

    @Test public void nullErrorProbeGetsFreshContextOnActualRequestAndDiscardClearsIds() throws Exception {
        configure(TraceType.TRACEPARENT, true, 1f);
        FTURLConnectionTest.Fixture f = new FTURLConnectionTest.Fixture();
        assertNull(f.wrapped.getErrorStream());
        String firstHeader = f.original.getRequestProperty("traceparent");
        assertNotNull(firstHeader);
        assertNull(f.state.traceId); assertNull(f.state.spanId);
        f.wrapped.getResponseCode();
        assertNotEquals(firstHeader, f.original.getRequestProperty("traceparent"));
        assertNotNull(f.state.traceId);
        f.state.discard();
        assertNull(f.state.traceId); assertNull(f.state.spanId);
    }

    @Test public void rejectedResourceStartDoesNotInjectTraceHeaders() throws Exception {
        configure(TraceType.TRACEPARENT, true, 1f);
        FTURLConnectionTest.Fixture f = new FTURLConnectionTest.Fixture();
        f.state.discard();
        f.wrapped.getResponseCode();
        assertNull(f.original.getRequestProperty("traceparent"));
        assertNull(f.state.traceId);
    }
}
