package com.ft.sdk;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FTWebViewHandlerTest {

    @Test
    public void resolveContainerViewIdPrefersCurrentSlotBinding() {
        assertEquals("current-view",
                FTWebViewHandler.resolveContainerViewId("initial-view", "current-view"));
    }

    @Test
    public void resolveContainerViewIdFallsBackToInitialViewWithoutValidBinding() {
        assertEquals("initial-view",
                FTWebViewHandler.resolveContainerViewId("initial-view", null));
        assertEquals("initial-view",
                FTWebViewHandler.resolveContainerViewId("initial-view", ""));
        assertEquals("initial-view",
                FTWebViewHandler.resolveContainerViewId(
                "initial-view", SessionReplayBridge.NULL_UUID));
    }

    @Test
    public void lazyBridgeDoesNotExposeReplayCapabilitiesBeforeReplayStarts() throws Exception {
        Object previousConfig = setRumConfigForBridgeTest(new FTRUMConfig());
        try {
            WebAppInterface bridge = new WebAppInterface(null, new NoOpReceiver(),
                    null, null, null, true);

            assertNull(bridge.getCapabilities());
            assertNull(bridge.getPrivacyLevel());
        } finally {
            restoreRumConfigForBridgeTest(previousConfig);
        }
    }

    @Test
    public void lazyBridgeReadsRumWebViewConfigurationAfterRumStarts() throws Exception {
        Object previousConfig = setRumConfigForBridgeTest(new FTRUMConfig()
                .setRumAppId("rum-app-id")
                .setEnableTraceWebView(true)
                .setAllowWebViewHost(new String[]{"example.com"}));
        try {
            WebAppInterface bridge = new WebAppInterface(null, new NoOpReceiver(),
                    null, null, null, true);

            assertEquals("[\"example.com\"]", bridge.getAllowedWebViewHosts());
            assertNull(bridge.getCapabilities());
            assertNull(bridge.getPrivacyLevel());
        } finally {
            restoreRumConfigForBridgeTest(previousConfig);
        }
    }

    private static Object setRumConfigForBridgeTest(FTRUMConfig config) throws Exception {
        Field field = FTRUMConfigManager.class.getDeclaredField("config");
        field.setAccessible(true);
        Object previousConfig = field.get(FTRUMConfigManager.get());
        field.set(FTRUMConfigManager.get(), config);
        return previousConfig;
    }

    private static void restoreRumConfigForBridgeTest(Object config) throws Exception {
        Field field = FTRUMConfigManager.class.getDeclaredField("config");
        field.setAccessible(true);
        field.set(FTRUMConfigManager.get(), config);
    }

    private static final class NoOpReceiver implements WebAppInterface.JsReceiver {
        @Override
        public void sendEvent(String s) {
        }

        @Override
        public void sendEvent(String s, String callbackMethod) {
        }

        @Override
        public void addEventListener(String s, String callBackMethod) {
        }
    }
}
