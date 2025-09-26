package com.ft.sdk.garble.utils;


/**
 * DC WebView utility class for direct method calls
 *
 * @author Brandon
 */
public class DCSWebViewUtils {

    private static final String LOG_TAG = Constants.LOG_TAG_PREFIX + "TBSWebViewUtils";

    private static boolean isDCSWebViewInitialized = false;
    private static boolean isDCWebViewAvailable = false;

    /**
     * Initialize DC WebView support
     * Call this method during SDK initialization
     */
    public static void initialize() {
        if (isDCSWebViewInitialized) {
            return;
        }

        isDCWebViewAvailable = PackageUtils.isDCSWebViewAvailable();
        if (isDCWebViewAvailable) {
            LogUtils.d(LOG_TAG, "DC WebView is available");
        } else {
            LogUtils.d(LOG_TAG, "DC WebView is not available");
        }

        isDCSWebViewInitialized = true;
    }

    /**
     * Check if DC WebView is available
     *
     * @return true if DC WebView is available and initialized
     */
    public static boolean isDCWebViewAvailable() {
        if (!isDCSWebViewInitialized) {
            initialize();
        }
        return isDCWebViewAvailable;
    }

    /**
     * Check if the given object is a DC WebView instance
     *
     * @param webView The object to check
     * @return true if it's a DC WebView instance
     */
    public static boolean isDCWebViewInstance(Object webView) {
        if (!isDCWebViewAvailable() || webView == null) {
            return false;
        }
        return webView instanceof io.dcloud.common.adapter.ui.webview.DCWebView;
    }

}