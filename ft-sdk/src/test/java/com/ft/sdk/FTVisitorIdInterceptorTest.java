package com.ft.sdk;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.DataType;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.Assert.*;

public class FTVisitorIdInterceptorTest {
    private MockWebServer server;
    private FTVisitorIdManager manager;

    @Before public void setUp() throws Exception {
        com.ft.sdk.garble.utils.LogUtils.setDebug(false);
        server = new MockWebServer();
        server.play();
        manager = FTVisitorIdManager.get();
        manager.stop();
        configure("/api/");
    }

    private void configure(String prefix) {
        manager.init(new FTVisitorIdConfig().setHeader("x-visitor-id")
                        .addMatch(okhttp3.HttpUrl.get(server.getUrl(prefix)).toString()),
                new FTVisitorIdManagerTest.MemoryStore(), new FTVisitorIdManagerTest.TestClock());
    }

    @After public void tearDown() throws Exception {
        manager.stop();
        FTHttpConfigManager.release();
        FTTraceConfigManager.get().release();
        FTRUMConfigManager.get().release();
        server.shutdown();
    }

    private OkHttpClient.Builder builder() {
        return new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS);
    }

    private Request request(String path) {
        return new Request.Builder().url(server.getUrl(path)).build();
    }

    @Test public void manualIntegrationReplacesDuplicateHeaderAndPreservesBodyAndTrace() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));
        Request original = request("/api/post").newBuilder()
                .addHeader("X-Visitor-Id", "caller-1").addHeader("x-visitor-id", "caller-2")
                .header("traceparent", "original-trace").header("X-Business", "kept")
                .post(RequestBody.create(null, "payload")).build();
        OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
        try (Response response = client.newCall(original).execute()) { assertEquals(200, response.code()); }
        RecordedRequest sent = server.takeRequest();
        assertEquals(manager.getId(), sent.getHeader("x-visitor-id"));
        assertEquals(new WebAppInterface(null, null, null, null, null).getVisitorid(),
                sent.getHeader("x-visitor-id"));
        assertEquals(1, sent.getHeaders("x-visitor-id").size());
        assertEquals("original-trace", sent.getHeader("traceparent"));
        assertEquals("kept", sent.getHeader("X-Business"));
        assertEquals("POST", sent.getMethod());
        assertEquals("payload", sent.getUtf8Body());
        assertEquals(2, original.headers("x-visitor-id").size());
        assertNull(original.tag(FTVisitorIdInterceptor.Snapshot.class));
    }

    @Test public void unmatchedDisabledAndIntakeRequestsDoNotReceiveSdkHeader() throws Exception {
        OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/other").newBuilder()
                .header("x-visitor-id", "caller").build()).execute()) { }
        assertEquals("caller", server.takeRequest().getHeader("x-visitor-id"));
        FTHttpConfigManager.get().setDatakitUrl(server.getUrl("/api/").toString());
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/api/v1/write/rum?precision=ms")).execute()) { }
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        FTHttpConfigManager.get().setDatakitUrl("https://different.example/");
        manager.stop();
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/api/disabled")).execute()) { }
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void intakeExclusionUsesEndpointsAndTracksDynamicConfiguration() throws Exception {
        configure("/");
        OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
        FTHttpConfigManager.get().setDatakitUrl(server.getUrl("/collector").toString());
        for (String path : new String[]{"/collector/v1/write/rum", "/collector/v1/write/logging",
                "/collector/v1/write/rum/replay", "/collector/v1/write/rum/replay_assets",
                "/collector/v1/check/rum/replay_assets", "/collector/v1/env_variable",
                "/collector/v1/datakit/pull?filters=true"}) {
            server.enqueue(new MockResponse());
            try (Response ignored = client.newCall(request(path)).execute()) { }
            assertNull(path, server.takeRequest().getHeader("x-visitor-id"));
        }
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/collector/api/business")).execute()) { }
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
        FTHttpConfigManager.get().setDatawayUrl(server.getUrl("/new/").toString(), "test-token");
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/new/v1/write/rum?token=test-token")).execute()) { }
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/collector/v1/write/rum")).execute()) { }
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void customIntakeExclusionAppliesWithoutResourceCollection() throws Exception {
        Field configField = FTRUMConfigManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(FTRUMConfigManager.get(), new FTRUMConfig().setEnableTraceUserResource(false)
                .setResourceUrlHandler(url -> url.contains("excluded")));
        OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/api/excluded")).execute()) { }
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/api/business")).execute()) { }
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void redirectsRemoveSdkHeaderOutsideMatchAndInjectWhenEnteringMatch() throws Exception {
        OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/other"));
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/api/redirect")).execute()) { }
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/api/destination"));
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/outside")).execute()) { }
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void nullConfigurationDisablesAlreadyInstalledInterceptor() throws Exception {
        OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/api/enabled")).execute()) { }
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));

        manager.init(null, new FTVisitorIdManagerTest.MemoryStore(), new FTVisitorIdManagerTest.TestClock());
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(request("/api/disabled")).execute()) { }
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void crossOriginRedirectCannotLeakSdkHeader() throws Exception {
        MockWebServer other = new MockWebServer();
        other.play();
        try {
            server.enqueue(new MockResponse().setResponseCode(302)
                    .addHeader("Location", other.getUrl("/api/")));
            other.enqueue(new MockResponse());
            OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
            try (Response ignored = client.newCall(request("/api/redirect")).execute()) { }
            assertNotNull(server.takeRequest().getHeader("x-visitor-id"));
            assertNull(other.takeRequest().getHeader("x-visitor-id"));
        } finally { other.shutdown(); }
    }

    @Test public void automaticHookIgnoresRumAndTraceSamplingAndDeduplicates() throws Exception {
        Field configField = FTRUMConfigManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(FTRUMConfigManager.get(), new FTRUMConfig().setRumAppId("test")
                .setSamplingRate(0).setEnableTraceUserResource(false));
        for (boolean trace : new boolean[]{false, true}) {
            FTTraceConfigManager.get().initWithConfig(new FTTraceConfig().setEnableAutoTrace(trace)
                    .setTraceType(TraceType.TRACEPARENT).setSamplingRate(0));
            OkHttpClient.Builder builder = builder();
            FTVisitorIdInterceptor.install(builder);
            FTAutoTrack.trackOkHttpBuilder(builder);
            OkHttpClient client = FTAutoTrack.trackOkHttpBuilder(builder);
            assertEquals(1, client.interceptors().stream()
                    .filter(value -> value instanceof FTVisitorIdInterceptor).count());
            assertEquals(1, client.networkInterceptors().size());
            server.enqueue(new MockResponse());
            try (Response ignored = client.newCall(request("/api/sample")).execute()) { }
            assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
        }
    }

    @Test public void requestSnapshotSurvivesResetAndNetworkErrorDoesNotRotateAgain() throws Exception {
        String before = manager.getId();
        OkHttpClient.Builder builder = builder();
        builder.addInterceptor(chain -> {
            Request request = chain.request();
            assertEquals(before, request.header("x-visitor-id"));
            FTSdk.resetVisitorId();
            String after = manager.getId();
            FTVisitorIdInterceptor.Snapshot snapshot = request.tag(FTVisitorIdInterceptor.Snapshot.class);
            assertNotNull(snapshot);
            SyncDataHelper helper = new SyncDataHelper();
            helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
            helper.initRUMConfig(new FTRUMConfig());
            for (String measurement : new String[]{"resource", "error"}) {
                String line = helper.getBodyContent(measurement, new HashMap<>(),
                        new HashMap<>(), 1, DataType.RUM_APP, "test", snapshot.id);
                assertTrue(line.contains("visitor_id=" + before));
                assertFalse(line.contains(after));
            }
            throw new IOException("simulated failure");
        });
        OkHttpClient client = FTVisitorIdInterceptor.install(builder).build();
        try {
            client.newCall(request("/api/failure")).execute();
            fail("Expected network error");
        } catch (IOException expected) { assertEquals("simulated failure", expected.getMessage()); }
        String after = manager.getId();
        assertNotEquals(before, after);
        server.enqueue(new MockResponse().setResponseCode(401));
        client = FTVisitorIdInterceptor.install(builder()).build();
        try (Response response = client.newCall(request("/api/unauthorized")).execute()) {
            assertEquals(401, response.code());
        }
        assertEquals(after, server.takeRequest().getHeader("x-visitor-id"));
        assertEquals(after, manager.getId());
    }

    @Test public void ttlExpiryDuringRequestKeepsWireSnapshotAndNextCallRotates() throws Exception {
        FTVisitorIdManagerTest.TestClock clock = new FTVisitorIdManagerTest.TestClock();
        manager.init(new FTVisitorIdConfig().setHeader("x-visitor-id")
                .addMatch(okhttp3.HttpUrl.get(server.getUrl("/api/")).toString()),
                new FTVisitorIdManagerTest.MemoryStore(), clock);
        String first = manager.getId();
        OkHttpClient.Builder builder = builder();
        builder.addInterceptor(chain -> {
            clock.now += 86400000;
            assertNotEquals(first, manager.getId());
            return chain.proceed(chain.request());
        });
        server.enqueue(new MockResponse());
        try (Response ignored = FTVisitorIdInterceptor.install(builder).build()
                .newCall(request("/api/slow")).execute()) { }
        assertEquals(first, server.takeRequest().getHeader("x-visitor-id"));
        String next = manager.getId();
        server.enqueue(new MockResponse());
        try (Response ignored = FTVisitorIdInterceptor.install(builder()).build()
                .newCall(request("/api/next")).execute()) { }
        assertEquals(next, server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void stopWhileRequestIsInFlightRemovesHeaderBeforeNetwork() throws Exception {
        OkHttpClient.Builder builder = builder();
        builder.addInterceptor(chain -> {
            manager.stop();
            return chain.proceed(chain.request());
        });
        server.enqueue(new MockResponse());
        try (Response ignored = FTVisitorIdInterceptor.install(builder).build()
                .newCall(request("/api/stopped")).execute()) { }
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void reusingResponseRequestStartsWithFreshVisitorId() throws Exception {
        OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
        server.enqueue(new MockResponse());
        Request reused;
        try (Response response = client.newCall(request("/api/reuse")).execute()) {
            reused = response.request();
        }
        String first = server.takeRequest().getHeader("x-visitor-id");
        manager.reset();
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(reused).execute()) { }
        String next = server.takeRequest().getHeader("x-visitor-id");
        assertEquals(manager.getId(), next);
        assertNotEquals(first, next);
        manager.stop();
        server.enqueue(new MockResponse());
        try (Response ignored = client.newCall(reused).execute()) { }
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void reinitializingInsideMatcherCannotApplyOldMatchToNewHeader() throws Exception {
        manager.init(new FTVisitorIdConfig().setHeader("x-visitor-id")
                .addMatch((FTVisitorIdConfig.UrlMatcher) url -> {
                    manager.init(new FTVisitorIdConfig().setHeader("x-new-id")
                            .addMatch("https://different.example/"),
                            new FTVisitorIdManagerTest.MemoryStore(), new FTVisitorIdManagerTest.TestClock());
                    return true;
                }), new FTVisitorIdManagerTest.MemoryStore(), new FTVisitorIdManagerTest.TestClock());
        server.enqueue(new MockResponse());
        OkHttpClient client = FTVisitorIdInterceptor.install(builder()).build();
        try (Response ignored = client.newCall(request("/api/reinit")).execute()) { }
        RecordedRequest sent = server.takeRequest();
        assertNull(sent.getHeader("x-visitor-id"));
        assertNull(sent.getHeader("x-new-id"));
    }
}
