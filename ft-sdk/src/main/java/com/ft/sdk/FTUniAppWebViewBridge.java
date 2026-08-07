package com.ft.sdk;

/**
 * Public control surface for the optional UniApp first-document bridge.
 *
 * <p>The UTS Session Replay module enables this bridge before UniApp creates
 * its first render WebView. The first eligible DCloud WebView consumes the
 * bridge selection. A successful {@code GCUniSessionReplay.setConfig()} call
 * also disables it as a fallback, so later WebViews use the normal RUM
 * WebView path.</p>
 */
public final class FTUniAppWebViewBridge {

    private FTUniAppWebViewBridge() {
    }

    public static void enableFirstViewBridge() {
        FTAutoTrack.enableUniAppFirstViewWebViewBridge();
    }

    public static void disableFirstViewBridge() {
        FTAutoTrack.disableUniAppFirstViewWebViewBridge();
    }
}
