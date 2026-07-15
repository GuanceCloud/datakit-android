package com.ft.sdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
