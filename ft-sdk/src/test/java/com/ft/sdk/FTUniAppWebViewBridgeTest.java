package com.ft.sdk;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FTUniAppWebViewBridgeTest {

    @Test
    public void disableFirstViewBridgeStopsLazyBridgeSelectionForLaterWebViews() {
        try {
            FTUniAppWebViewBridge.enableFirstViewBridge();
            FTUniAppWebViewBridge.disableFirstViewBridge();
            assertFalse(FTAutoTrack.consumeUniAppFirstViewWebViewBridge(true, false));
        } finally {
            FTUniAppWebViewBridge.disableFirstViewBridge();
        }
    }

    @Test
    public void firstEligibleDCloudWebViewConsumesLazyBridgeSelection() {
        try {
            FTUniAppWebViewBridge.enableFirstViewBridge();

            assertTrue(FTAutoTrack.consumeUniAppFirstViewWebViewBridge(true, false));
            assertFalse(FTAutoTrack.consumeUniAppFirstViewWebViewBridge(true, false));
        } finally {
            FTUniAppWebViewBridge.disableFirstViewBridge();
        }
    }

    @Test
    public void ineligibleWebViewDoesNotConsumeLazyBridgeSelection() {
        try {
            FTUniAppWebViewBridge.enableFirstViewBridge();

            assertFalse(FTAutoTrack.consumeUniAppFirstViewWebViewBridge(false, false));
            assertFalse(FTAutoTrack.consumeUniAppFirstViewWebViewBridge(true, true));
            assertTrue(FTAutoTrack.consumeUniAppFirstViewWebViewBridge(true, false));
        } finally {
            FTUniAppWebViewBridge.disableFirstViewBridge();
        }
    }
}
