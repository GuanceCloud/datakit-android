package com.ft.sdk;

import com.ft.sdk.garble.utils.LogUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** Visitor ID configuration must not cause URLConnection transport instrumentation. */
public class FTURLConnectionVisitorIdTest {
    @Before public void setup() {
        LogUtils.setDebug(false);
        FTRUMConfigManager.get().release();
        FTTraceConfigManager.get().release();
        FTVisitorIdManager.get().init(
                new FTVisitorIdConfig().setHeader("x-visitor-id").addMatch("http://localhost/"),
                new FTVisitorIdManagerTest.MemoryStore(), new FTVisitorIdManagerTest.TestClock());
    }

    @After public void cleanup() {
        FTVisitorIdManager.get().stop();
        FTTraceConfigManager.get().release();
        FTRUMConfigManager.get().release();
    }

    @Test public void visitorOnlyLeavesLegacyAndNewHooksUnwrapped() throws Exception {
        FTURLConnectionTest.Connection original = new FTURLConnectionTest.Connection();
        assertSame(original, FTURLConnectionInstrumentation.wrap(original));
        assertSame(original, FTURLConnectionInstrumentation.wrap(original, null));
        assertSame(original, FTURLConnectionInstrumentation.wrap(original, Proxy.NO_PROXY));
        assertNull(original.getRequestProperty("x-visitor-id"));
        assertTrue(original.getInstanceFollowRedirects());
        assertEquals(0, original.connects);
        assertEquals(0, original.disconnects);
    }

    @Test public void manualEntrypointsOpenExactlyOnceWithOriginalProxyPolicy() throws Exception {
        for (Proxy proxy : new Proxy[]{null, Proxy.NO_PROXY,
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 1234))}) {
            final int[] opens = {0};
            final FTURLConnectionTest.Connection original = new FTURLConnectionTest.Connection();
            URL url = new URL(null, "http://localhost/test", new URLStreamHandler() {
                @Override protected URLConnection openConnection(URL url) {
                    assertNull(proxy);
                    opens[0]++;
                    return original;
                }
                @Override protected URLConnection openConnection(URL url, Proxy selected) {
                    assertEquals(proxy, selected);
                    opens[0]++;
                    return original;
                }
            });
            URLConnection result = proxy == null
                    ? FTURLConnectionInstrumentation.openConnection(url)
                    : FTURLConnectionInstrumentation.openConnection(url, proxy);
            assertSame(original, result);
            assertEquals(1, opens[0]);
            assertNull(original.getRequestProperty("x-visitor-id"));
        }
    }

    @Test public void resourceObserverNeverChangesRedirectPolicyOrHeaders() throws Exception {
        for (boolean follow : new boolean[]{false, true}) {
            FTURLConnectionTest.Fixture fixture = new FTURLConnectionTest.Fixture();
            fixture.wrapped.setInstanceFollowRedirects(follow);
            fixture.wrapped.setRequestProperty("x-visitor-id", "application-owned");
            fixture.original.status = HttpURLConnection.HTTP_MOVED_TEMP;
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, fixture.wrapped.getResponseCode());
            assertEquals(follow, fixture.original.getInstanceFollowRedirects());
            assertEquals("application-owned", fixture.original.getRequestProperty("x-visitor-id"));
            assertEquals(0, fixture.original.disconnects);
            fixture.wrapped.disconnect();
            assertEquals(1, fixture.original.disconnects);
        }
    }

    @Test public void metadataAndErrorProbesDoNotForceAdditionalResponseIo() throws Exception {
        FTURLConnectionTest.Connection original = new FTURLConnectionTest.Connection() {
            @Override public int getResponseCode() {
                fail("A metadata/error probe must not trigger an extra response-code read");
                return -1;
            }
            @Override public Map<String, List<String>> getHeaderFields() {
                return Collections.emptyMap();
            }
        };
        FTURLConnectionRequestState state = new FTURLConnectionRequestState(
                original.getURL().toString(), 0, new FTURLConnectionTest.Recorder());
        HttpURLConnection wrapped = new FTHttpURLConnection(original, new FTURLConnectionDelegate(original, state));
        assertTrue(wrapped.getHeaderFields().isEmpty());
        assertNull(wrapped.getErrorStream());
        assertNull(original.getRequestProperty("x-visitor-id"));
        wrapped.disconnect();
    }
}
