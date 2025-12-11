package com.ft.sdk.garble.utils;

import android.webkit.ValueCallback;


/**
 * TBS WebView utility class for direct method calls
 *
 * @author Brandon
 */
public class TBSWebViewUtils {

    private static final String LOG_TAG = Constants.LOG_TAG_PREFIX + "TBSWebViewUtils";

    // Flag to indicate if TBS WebView is available and initialized
    private static boolean isTBSWebViewInitialized = false;
    private static boolean isTBSWebViewAvailable = false;

    /**
     * Initialize TBS WebView support
     * Call this method during SDK initialization
     */
    public static void initialize() {
        if (isTBSWebViewInitialized) {
            return;
        }

        isTBSWebViewAvailable = PackageUtils.isTBSWebViewAvailable();
        if (isTBSWebViewAvailable) {
            LogUtils.d(LOG_TAG, "TBS WebView is available");
        } else {
            LogUtils.d(LOG_TAG, "TBS WebView is not available");
        }

        isTBSWebViewInitialized = true;
    }

    /**
     * Check if TBS WebView is available
     *
     * @return true if TBS WebView is available and initialized
     */
    public static boolean isTBSWebViewAvailable() {
        if (!isTBSWebViewInitialized) {
            initialize();
        }
        return isTBSWebViewAvailable;
    }

    /**
     * Check if the given object is a TBS WebView instance
     *
     * @param webView The object to check
     * @return true if it's a TBS WebView instance
     */
    public static boolean isTBSWebViewInstance(Object webView) {
        if (!isTBSWebViewAvailable() || webView == null) {
            return false;
        }
        return webView instanceof com.tencent.smtt.sdk.WebView;
    }

    /**
     * Set JavaScript enabled for TBS WebView
     *
     * @param webView TBS WebView instance
     * @param enabled Whether to enable JavaScript
     */
    public static void setJavaScriptEnabled(Object webView, boolean enabled) {
        if (!isTBSWebViewAvailable() || !isTBSWebViewInstance(webView)) {
            return;
        }

        try {
            com.tencent.smtt.sdk.WebView tbsWebView = (com.tencent.smtt.sdk.WebView) webView;
            com.tencent.smtt.sdk.WebSettings settings = tbsWebView.getSettings();
            settings.setJavaScriptEnabled(enabled);
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Failed to set JavaScript enabled for TBS WebView: " + e.getMessage());
        }
    }

    /**
     * Add JavaScript interface to TBS WebView
     *
     * @param webView TBS WebView instance
     * @param object  JavaScript interface object
     * @param name    Interface name
     */
    public static void addJavascriptInterface(Object webView, Object object, String name) {
        if (!isTBSWebViewAvailable() || !isTBSWebViewInstance(webView)) {
            return;
        }

        try {
            com.tencent.smtt.sdk.WebView tbsWebView = (com.tencent.smtt.sdk.WebView) webView;
            tbsWebView.addJavascriptInterface(object, name);
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Failed to add JavaScript interface for TBS WebView: " + e.getMessage());
        }
    }

    /**
     * Evaluate JavaScript in TBS WebView
     *
     * @param webView  TBS WebView instance
     * @param script   JavaScript code
     * @param callback Callback for result
     */
    public static void evaluateJavascript(Object webView, String script, ValueCallback<String> callback) {
        if (!isTBSWebViewAvailable() || !isTBSWebViewInstance(webView)) {
            return;
        }

        try {
            com.tencent.smtt.sdk.WebView tbsWebView = (com.tencent.smtt.sdk.WebView) webView;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                // Create TBS ValueCallback wrapper
                tbsWebView.evaluateJavascript(script, new com.tencent.smtt.sdk.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (callback != null) {
                            callback.onReceiveValue(value);
                        }
                    }
                });
            } else {
                tbsWebView.loadUrl("javascript:" + script);
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Failed to evaluate JavaScript for TBS WebView: " + e.getMessage());
        }
    }


    /**
     * Load URL in TBS WebView
     *
     * @param webView TBS WebView instance
     * @param url     URL to load
     */
    public static void loadUrl(Object webView, String url) {
        if (!isTBSWebViewAvailable() || !isTBSWebViewInstance(webView)) {
            return;
        }

        try {
            com.tencent.smtt.sdk.WebView tbsWebView = (com.tencent.smtt.sdk.WebView) webView;
            tbsWebView.loadUrl(url);
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Failed to load URL for TBS WebView: " + e.getMessage());
        }
    }
}