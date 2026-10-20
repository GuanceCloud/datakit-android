package com.ft.sdk;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.utils.LogUtils;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class FTURLConnectionVisitorIdTest {
    private MockWebServer server;
    private FTVisitorIdManager manager;
    private FTVisitorIdManagerTest.MemoryStore store;
    private FTVisitorIdManagerTest.TestClock clock;

    @Before public void setup() throws Exception {
        LogUtils.setDebug(false);
        server = new MockWebServer();
        server.play();
        manager = FTVisitorIdManager.get();
        manager.stop();
        store = new FTVisitorIdManagerTest.MemoryStore();
        clock = new FTVisitorIdManagerTest.TestClock();
        configure(server.getUrl("/api/").toString());
    }

    private void configure(String prefix) {
        manager.init(new FTVisitorIdConfig().setHeader("x-visitor-id").addMatch(prefix), store, clock);
    }

    @After public void cleanup() throws Exception {
        manager.stop();
        FTTraceConfigManager.get().release();
        FTRUMConfigManager.get().release();
        FTHttpConfigManager.release();
        server.shutdown();
    }

    private static final class Call {
        final HttpURLConnection connection;
        final FTURLConnectionRequestState state;
        final FTURLConnectionTest.Recorder recorder = new FTURLConnectionTest.Recorder();
        Call(HttpURLConnection original, Proxy proxy) {
            original.setConnectTimeout(2000);
            original.setReadTimeout(2000);
            state = new FTURLConnectionRequestState(original.getURL().toString(), 0, recorder);
            connection = new FTHttpURLConnection(original, new FTURLConnectionDelegate(original, state,
                    new FTURLConnectionRedirects(original, proxy)));
        }
    }

    private Call call(String path) throws Exception {
        return new Call((HttpURLConnection) server.getUrl(path).openConnection(), null);
    }

    private static String consume(HttpURLConnection connection) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream input = connection.getInputStream()) {
            byte[] buffer = new byte[256];
            for (int read; (read = input.read(buffer)) != -1;) bytes.write(buffer, 0, read);
        } finally { connection.disconnect(); }
        return bytes.toString("UTF-8");
    }

    @Test public void matchedUnmatchedMatchedChainKeepsOriginalIdAcrossReset() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/outside"));
        server.enqueue(new MockResponse().setResponseCode(301).addHeader("Location", "/api/final"));
        server.enqueue(new MockResponse().setBody("done"));
        Call call = call("/api/start");
        String before = manager.getId();
        call.connection.connect();
        manager.reset();
        assertNotEquals(before, manager.getId());
        assertEquals("done", consume(call.connection));
        assertEquals(before, server.takeRequest().getHeader("x-visitor-id"));
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        assertEquals(before, server.takeRequest().getHeader("x-visitor-id"));
        assertEquals(before, call.state.visitorId);
        assertTrue(call.connection.getInstanceFollowRedirects());
        assertEquals(server.getUrl("/api/final"), call.connection.getURL());
        assertEquals(1, call.recorder.starts);
        assertEquals(1, call.recorder.finishes);
    }

    @Test public void crossOriginDropsVisitorAndAuthorizationButKeepsBusinessHeaders() throws Exception {
        MockWebServer other = new MockWebServer(); other.play();
        try {
            server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", other.getUrl("/api/end")));
            other.enqueue(new MockResponse().setBody("ok"));
            Call call = call("/api/start");
            call.connection.setRequestProperty("Authorization", "Bearer test-only");
            call.connection.setRequestProperty("X-Business", "kept");
            call.connection.setRequestProperty("X-Visitor-Id", "caller");
            assertEquals("ok", consume(call.connection));
            assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
            RecordedRequest last = other.takeRequest();
            assertNull(last.getHeader("x-visitor-id"));
            assertNull(last.getHeader("Authorization"));
            assertEquals("kept", last.getHeader("X-Business"));
        } finally { other.shutdown(); }
    }

    @Test public void firstUnmatchedRequestCanEnterMatch() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(303).addHeader("Location", "/api/end"));
        server.enqueue(new MockResponse());
        consume(call("/outside").connection);
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void disablingFollowReturnsRedirectAndKeepsSetting() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/outside"));
        Call call = call("/api/start");
        call.connection.setInstanceFollowRedirects(false);
        assertEquals(302, call.connection.getResponseCode());
        assertFalse(call.connection.getInstanceFollowRedirects());
        call.connection.disconnect();
        assertEquals(1, server.getRequestCount());
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void postRedirectsUseAndroidMethodRulesWithoutReplayingUpload() throws Exception {
        for (int code : new int[]{300, 301, 302, 303, 307, 308}) {
            server.enqueue(new MockResponse().setResponseCode(code).addHeader("Location", "/api/final"));
            if (code < 307) server.enqueue(new MockResponse().setBody("done"));
            Call call = call("/api/upload");
            call.connection.setDoOutput(true);
            call.connection.setFixedLengthStreamingMode(4);
            call.connection.setRequestProperty("Content-Type", "application/octet-stream");
            try (OutputStream out = call.connection.getOutputStream()) { out.write(new byte[]{1,2,3,4}); }
            assertEquals(code < 307 ? 200 : code, call.connection.getResponseCode());
            consume(call.connection);
            RecordedRequest first = server.takeRequest();
            assertEquals("POST", first.getMethod());
            assertArrayEquals(new byte[]{1,2,3,4}, first.getBody());
            if (code < 307) {
                RecordedRequest last = server.takeRequest();
                assertEquals("GET", last.getMethod());
                assertEquals(0, last.getBodySize());
                assertNull(last.getHeader("Content-Type"));
                assertEquals(manager.getId(), last.getHeader("x-visitor-id"));
            }
        }
    }

    @Test public void temporaryAndPermanentRedirectsFollowGetAndHead() throws Exception {
        for (int code : new int[]{307, 308}) for (String method : new String[]{"GET", "HEAD"}) {
            server.enqueue(new MockResponse().setResponseCode(code).addHeader("Location", "/outside"));
            server.enqueue(new MockResponse());
            Call call = call("/api/start"); call.connection.setRequestMethod(method);
            assertEquals(200, call.connection.getResponseCode()); call.connection.disconnect();
            assertEquals(method, server.takeRequest().getMethod());
            RecordedRequest last = server.takeRequest();
            assertEquals(method, last.getMethod());
            assertNull(last.getHeader("x-visitor-id"));
        }
    }

    @Test public void rejectsCrossSchemeAndBoundsLoops() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "https://localhost:1/api/end"));
        Call call = call("/api/start");
        assertEquals(302, call.connection.getResponseCode()); call.connection.disconnect();
        for (int i = 0; i < 21; i++) server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/api/loop"));
        call = call("/api/loop");
        try { call.connection.getResponseCode(); fail(); }
        catch (java.net.ProtocolException expected) { assertTrue(expected.getMessage().contains("Too many redirects")); }
        try { call.connection.getInputStream(); fail(); }
        catch (java.net.ProtocolException expected) { }
        call.connection.disconnect();
        assertEquals(22, server.getRequestCount());
        assertEquals(1, call.recorder.finishes);
        assertTrue(call.recorder.params.forceNetworkError);
    }

    @Test public void preservesExplicitProxyAcrossHostsAndDoesNotResolveDestinationDirectly() throws Exception {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", server.getPort()));
        configure("http://initial.invalid/api/");
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "http://next.invalid/api/end"));
        server.enqueue(new MockResponse().setBody("proxy"));
        URL url = new URL("http://initial.invalid/api/start");
        Call call = new Call((HttpURLConnection) url.openConnection(proxy), proxy);
        assertEquals(200, call.connection.getResponseCode());
        assertTrue(call.connection.usingProxy());
        assertEquals("proxy", consume(call.connection));
        RecordedRequest first = server.takeRequest(), last = server.takeRequest();
        assertTrue(first.getRequestLine().contains("http://initial.invalid/"));
        assertTrue(last.getRequestLine().contains("http://next.invalid/"));
        assertNotNull(first.getHeader("x-visitor-id"));
        assertNull(last.getHeader("x-visitor-id"));
    }

    @Test public void defaultProxySelectionAndExplicitNoProxyAreDistinct() throws Exception {
        ProxySelector previous = ProxySelector.getDefault();
        final int[] selections = {0};
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", server.getPort()));
        ProxySelector.setDefault(new ProxySelector() {
            public List<Proxy> select(URI uri) { selections[0]++; return Collections.singletonList(proxy); }
            public void connectFailed(URI uri, SocketAddress address, java.io.IOException error) { }
        });
        try {
            configure("http://initial.invalid/");
            server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "http://next.invalid/end"));
            server.enqueue(new MockResponse());
            consume(new Call((HttpURLConnection) new URL("http://initial.invalid/start").openConnection(), null).connection);
            assertTrue(selections[0] >= 2);
            selections[0] = 0;
            configure(server.getUrl("/").toString());
            server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/end"));
            server.enqueue(new MockResponse());
            consume(new Call((HttpURLConnection) server.getUrl("/start").openConnection(Proxy.NO_PROXY), Proxy.NO_PROXY).connection);
            assertEquals(0, selections[0]);
        } finally { ProxySelector.setDefault(previous); }
    }

    @Test public void manualEntrypointWorksWithoutResourceCollectionOrOkHttpHooks() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/api/final"));
        server.enqueue(new MockResponse());
        HttpURLConnection connection = (HttpURLConnection) FTURLConnectionInstrumentation.openConnection(server.getUrl("/api/start"));
        assertTrue(connection instanceof FTHttpURLConnection);
        consume(connection);
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
        assertEquals(manager.getId(), server.takeRequest().getHeader("x-visitor-id"));
        assertEquals(0, FTURLConnectionResourceReporter.activeCount());
    }

    @Test public void disabledVisitorPreservesNativeConnectionAndRedirectPolicy() throws Exception {
        manager.stop();
        HttpURLConnection original = (HttpURLConnection) server.getUrl("/api/start").openConnection();
        assertSame(original, FTURLConnectionInstrumentation.wrap(original, null));
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/api/final"));
        server.enqueue(new MockResponse());
        consume(new Call(original, null).connection);
        assertTrue(original.getInstanceFollowRedirects());
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void intakeAndShutdownAreRecheckedForFollowingHop() throws Exception {
        configure(server.getUrl("/").toString());
        FTHttpConfigManager.get().setDatakitUrl(server.getUrl("/collector").toString());
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/collector/v1/write/rum"));
        server.enqueue(new MockResponse());
        consume(call("/api/start").connection);
        assertNotNull(server.takeRequest().getHeader("x-visitor-id"));
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/api/final"));
        server.enqueue(new MockResponse());
        Call call = call("/api/start"); call.connection.connect(); manager.stop();
        consume(call.connection);
        assertNotNull(server.takeRequest().getHeader("x-visitor-id"));
        assertNull(server.takeRequest().getHeader("x-visitor-id"));
    }

    @Test public void headerAndContentEntryPointsResolveRedirectsBeforeReturning() throws Exception {
        for (int mode = 0; mode < 4; mode++) {
            server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location", "/outside").addHeader("X-Hop", "first"));
            server.enqueue(new MockResponse().setBody("done").addHeader("X-Hop", "last"));
            Call call = call("/api/start");
            if (mode == 0) assertEquals("last", call.connection.getHeaderField("X-Hop"));
            if (mode == 1) assertEquals("last", call.connection.getHeaderFields().get("X-Hop").get(0));
            if (mode == 2) assertEquals("OK", call.connection.getResponseMessage());
            if (mode == 3) assertNull(call.connection.getErrorStream());
            assertEquals("done", consume(call.connection));
            assertNotNull(server.takeRequest().getHeader("x-visitor-id"));
            assertNull(server.takeRequest().getHeader("x-visitor-id"));
        }
    }
}
