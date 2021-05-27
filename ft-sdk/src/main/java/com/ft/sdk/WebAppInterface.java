package com.ft.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
    public interface JsReceiver {
        void sendEvent(String s);
        void sendEvent(String s, String callbackMethod);

        void addEventListener(String s, String callBackMethod);
    }

    private JsReceiver mJsReceiver;

    Context mContext;

    /**
     * Instantiate the interface and set the context
     */
    WebAppInterface(Context c, JsReceiver receiver) {
        mJsReceiver = receiver;
        mContext = c;
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void sendEvent(String string) {
        mJsReceiver.sendEvent(string);
    }

    @JavascriptInterface
    public void sendEvent(String string, String callBackMethod) {
        mJsReceiver.sendEvent(string,callBackMethod);
    }


    @JavascriptInterface
    public void addEventListener(String string, String callbackMethod) {
        mJsReceiver.addEventListener(string,callbackMethod);
    }



}