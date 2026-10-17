package com.ft.sdk;

import android.webkit.JavascriptInterface;

import com.ft.sdk.garble.utils.LogUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FTVisitorIdWebViewBridgeTest {
    private FTVisitorIdManager manager;
    private FTVisitorIdManagerTest.MemoryStore store;
    private FTVisitorIdManagerTest.TestClock clock;
    private WebAppInterface bridge;

    @Before
    public void setUp() {
        LogUtils.setDebug(false);
        manager = FTVisitorIdManager.get();
        manager.stop();
        store = new FTVisitorIdManagerTest.MemoryStore();
        clock = new FTVisitorIdManagerTest.TestClock();
        bridge = new WebAppInterface(null, null, null, null, null);
    }

    @After
    public void tearDown() {
        manager.stop();
    }

    @Test
    public void exposesExactSynchronousJavascriptProtocol() throws Exception {
        Method getter = WebAppInterface.class.getMethod("getVisitorid");
        assertNotNull(getter.getAnnotation(JavascriptInterface.class));
        assertTrue(Modifier.isPublic(getter.getModifiers()));
        assertEquals(String.class, getter.getReturnType());
        assertEquals(0, getter.getParameterTypes().length);
    }

    @Test
    public void unavailableNativeIdReturnsNullWithoutCreatingIdentity() {
        assertNull(bridge.getVisitorid());
        manager.init(null, store, clock);
        assertNull(bridge.getVisitorid());
        assertEquals(0, store.writes);
    }

    @Test
    public void existingBridgeObservesInitializationDisableAndRestart() {
        assertNull(bridge.getVisitorid());
        manager.init(FTVisitorIdManagerTest.enabled(), store, clock);
        String id = bridge.getVisitorid();
        assertEquals(manager.getId(), id);
        assertTrue(id.matches("p[0-9a-f]{32}"));

        manager.init(null, store, clock);
        assertNull(bridge.getVisitorid());
        manager.init(FTVisitorIdManagerTest.enabled(), store, clock);
        assertEquals(id, bridge.getVisitorid());
        manager.stop();
        assertNull(bridge.getVisitorid());
        manager.init(FTVisitorIdManagerTest.enabled(), store, clock);
        assertEquals(id, bridge.getVisitorid());
        assertEquals(1, store.writes);
    }

    @Test
    public void nativeResetIsImmediatelyVisibleWithoutReplacingBridge() {
        manager.init(FTVisitorIdManagerTest.enabled(), store, clock);
        String first = bridge.getVisitorid();
        FTSdk.resetVisitorId();
        String reset = bridge.getVisitorid();
        assertNotEquals(first, reset);
        assertEquals(manager.getId(), reset);
        assertEquals(reset, bridge.getVisitorid());
        assertEquals(2, store.writes);
    }

    @Test
    public void readsUseNativeTtlWithoutSlidingExpiration() {
        manager.init(FTVisitorIdManagerTest.enabled().setTtlHours(1), store, clock);
        String first = bridge.getVisitorid();
        clock.now += 3600000 - 1;
        assertEquals(first, bridge.getVisitorid());
        clock.now++;
        String rotated = bridge.getVisitorid();
        assertNotEquals(first, rotated);
        assertEquals(manager.getId(), rotated);
        assertEquals(2, store.writes);
    }

    @Test
    public void storageFailureExposesStableNativeFallbackAndItsReset() {
        store.writable = false;
        manager.init(FTVisitorIdManagerTest.enabled(), store, clock);
        String fallback = bridge.getVisitorid();
        assertTrue(fallback.matches("s[0-9a-f]{32}"));
        for (int i = 0; i < 10; i++) assertEquals(fallback, bridge.getVisitorid());
        assertEquals(1, store.writes);
        FTSdk.resetVisitorId();
        assertNotEquals(fallback, bridge.getVisitorid());
        assertEquals(manager.getId(), bridge.getVisitorid());
    }

    @Test
    public void legacyWebCallsAndBridgeMetadataKeepTheirContract() {
        manager.init(FTVisitorIdManagerTest.enabled(), store, clock);
        String id = manager.getId();
        List<String> received = new ArrayList<>();
        WebAppInterface legacyBridge = new WebAppInterface(null, new WebAppInterface.JsReceiver() {
            @Override public void sendEvent(String event) {
                received.add(event);
            }

            @Override public void sendEvent(String event, String callback) {
                received.add(event + ":" + callback);
            }

            @Override public void addEventListener(String event, String callback) {
                received.add(event + ":" + callback);
            }
        }, new String[]{"example.com"}, "mask", new String[]{"records"});
        legacyBridge.sendEvent("rum-event");
        legacyBridge.sendEvent("callback-event", "onResult");
        legacyBridge.addEventListener("view", "onView");
        assertEquals(Arrays.asList("rum-event", "callback-event:onResult", "view:onView"), received);
        assertEquals("[\"example.com\"]", legacyBridge.getAllowedWebViewHosts());
        assertEquals("mask", legacyBridge.getPrivacyLevel());
        assertEquals("[\"records\"]", legacyBridge.getCapabilities());
        assertNull(bridge.getAllowedWebViewHosts());
        assertNull(bridge.getCapabilities());
        assertEquals(id, manager.getId());
        assertEquals(1, store.writes);
    }
}
