package com.ft.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.ft.sdk.garble.utils.Utils;

import java.util.Arrays;

/**
 * Web JS method registration
 *
 * @author Brandon
 */

public class WebAppInterface {
    public interface JsReceiver {
        void sendEvent(String s);

        void sendEvent(String s, String callbackMethod);

        void addEventListener(String s, String callBackMethod);
    }

    private final JsReceiver mJsReceiver;
    private final String[] mAllowWebViewHost;
    private final String[] capabilities;
    private final String privacyLevel;

    Context mContext;

    public WebAppInterface(Context c, JsReceiver receiver,
                           String[] allowWebViewHost, String privacyLevel, String[] capabilities) {
        mJsReceiver = receiver;
        mContext = c;
        mAllowWebViewHost = allowWebViewHost;
        //fixme read from config
        this.capabilities = capabilities;
        this.privacyLevel = privacyLevel;
    }

    /**
     * WebView JS method, used for calls without return value
     *
     * @param string Method name
     */
    @JavascriptInterface
    public void sendEvent(String string) {
        mJsReceiver.sendEvent(string);
    }

    /**
     * WebView JS method, used for data that requires a return type
     *
     * @param string Method name
     */
    @JavascriptInterface
    public void sendEvent(String string, String callBackMethod) {
        mJsReceiver.sendEvent(string, callBackMethod);
    }


    @JavascriptInterface
    public String getAllowedWebViewHosts() {
        if (mAllowWebViewHost == null) {
            return null;
        }
        return Utils.setToJsonString(Arrays.asList(mAllowWebViewHost));
    }


    @JavascriptInterface
    public String getPrivacyLevel() {
        return privacyLevel;
    }

    @JavascriptInterface
    public String getCapabilities() {
        if (capabilities == null) {
            return null;
        }
        return Utils.setToJsonString(Arrays.asList(capabilities));
    }


    /**
     * WebView JS method, used for method registration callback, suitable for listener-type scenarios
     *
     * @param string         Method name
     * @param callbackMethod Callback method
     */
    @JavascriptInterface
    public void addEventListener(String string, String callbackMethod) {
        mJsReceiver.addEventListener(string, callbackMethod);
    }


}