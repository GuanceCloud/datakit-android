package com.ft.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

/**
 * web js 方法注册
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

    Context mContext;

    public WebAppInterface(Context c, JsReceiver receiver) {
        mJsReceiver = receiver;
        mContext = c;
    }

    /**
     * web view js 方法，用于无返回调用
     *  @param string 方法名
     */
    @JavascriptInterface
    public void sendEvent(String string) {
        mJsReceiver.sendEvent(string);
    }

    /**
     * web view js 方法，用于需要返回类型的数据，
     *
     * @param string 方法名
     */
    @JavascriptInterface
    public void sendEvent(String string, String callBackMethod) {
        mJsReceiver.sendEvent(string,callBackMethod);
    }


    /**
     * web view js 方法， 用于方法注册回调，适用监听类型的场景
     * @param string 方法名
     * @param callbackMethod 毁掉
     */
    @JavascriptInterface
    public void addEventListener(String string, String callbackMethod) {
        mJsReceiver.addEventListener(string,callbackMethod);
    }



}