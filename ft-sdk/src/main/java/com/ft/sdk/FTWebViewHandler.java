package com.ft.sdk;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

final class FTWebViewHandler implements WebAppInterface.JsReceiver {

    private static final String LOG_TAG = "FTWebViewHandler";

    public static final String WEB_JS_STATUS = "status";
    public static final String WEB_JS_INNER_TAG = "_tag";
    public static final String WEB_JS_TYPE_RUM = "rum";
    public static final String WEB_JS_TYPE_TRACK = "track";
    public static final String WEB_JS_TYPE_LOG = "log";
    public static final String WEB_JS_TYPE_URL_VERIFY = "urlVerify";
    public static final String WEB_JS_NAME = "name";
    public static final String WEB_JS_DATA = "data";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private WebView mWebView;

    public void setWebView(WebView webview) {
        mWebView = webview;
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new WebAppInterface(webview.getContext(), this), "FTWebViewJavascriptBridge");

    }


    public FTWebViewHandler() {
    }

    @Override
    public void sendEvent(String s) {
        sendEvent(s, null);
    }

    @Override
    public void sendEvent(String s, String callbackMethod) {
        try {
            LogUtils.d(LOG_TAG, "sendEvent：" + s);
            JSONObject json = new JSONObject(s);
            String name = json.optString(WEB_JS_NAME);
            JSONObject data = json.optJSONObject(WEB_JS_DATA);
            String tag = json.optString(WEB_JS_INNER_TAG);
            if (name.equals(WEB_JS_TYPE_RUM)) {
                if (data != null) {
                    JSONObject tags = data.optJSONObject(Constants.TAGS);
                    JSONObject fields = data.optJSONObject(Constants.FIELDS);

                    JSONObject publicTags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
                    Iterator<String> keys = publicTags.keys();
                    if (tags == null) {
                        tags = new JSONObject();
                    }

                    while (keys.hasNext()) {
                        String key = keys.next();
                        tags.put(key, publicTags.opt(key));
                    }

                    String sessionId = FTRUMGlobalManager.get().getSessionId();
                    tags.put(Constants.KEY_RUM_SESSION_ID, sessionId);
                    tags.put(Constants.KEY_RUM_VIEW_IS_ACTIVE, false);
                    tags.put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true);

                    long time = data.optLong(Constants.TIME);
                    String measurement = data.optString(Constants.MEASUREMENT);
                    FTTrackInner.getInstance().rumWebView(time, measurement, tags, fields);
                }

            } else if (name.equals(WEB_JS_TYPE_TRACK)) {

            } else if (name.equals(WEB_JS_TYPE_LOG)) {

            } else if (name.equals(WEB_JS_TYPE_URL_VERIFY)) {

            }
            if (callbackMethod != null && callbackMethod.isEmpty()) {
                JSONObject retJson = new JSONObject();
                retJson.put(WEB_JS_STATUS, true);
                String ret = retJson.toString();
                String err = "{}";
                callbackFromNative(tag, callbackMethod, ret, err);
            }

        } catch (Exception e) {
            LogUtils.e(LOG_TAG, e.getMessage());
        }


    }

    @Override
    public void addEventListener(String s, String callBackMethod) {

        try {
            JSONObject json = new JSONObject(s);
            String name = json.optString(WEB_JS_NAME);
            String tag = json.optString(WEB_JS_INNER_TAG);

            // NO implement
            String ret = "{}";
            String err = "{}";
            callbackFromNative(tag, callBackMethod, ret, err);
        } catch (JSONException e) {
            LogUtils.e(LOG_TAG, e.getMessage());
        }

    }

    private void callJsMethod(String method) {
        callJsMethod(method, null);
    }

    private void callbackFromNative(String requestTag, String callBackMethod, String retJsonString, String errJsonString) {
        String separate = !retJsonString.isEmpty() && !errJsonString.isEmpty() ? "," : "";
        String jsStr = callBackMethod + "[" + requestTag + "](" + retJsonString + separate + errJsonString + ")";
        callJsMethod(jsStr);

    }

    private void callJsMethod(String method, CallbackFromJS callback) {
        mHandler.post(() -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(method, value -> {
                    if (callback != null) {
                        callback.callBack(value);
                    }
                });
            } else {
                LogUtils.e(LOG_TAG, "This Android Device may be low than 4.4, can't get js call Back ");
                mWebView.loadUrl("javascript:" + method);
            }

        });

    }


    interface CallbackFromJS {
        void callBack(String content);
    }


}
