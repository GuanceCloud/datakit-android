package com.ft.sdk;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * 用于配合 web 接收应用内 webview 数据指标
 * 需要配合观测云 web js sdk 使用
 * <a href="https://github.com/GuanceCloud/datakit-js">datakit-js</a>,
 * {@link #FT_WEB_VIEW_JAVASCRIPT_BRIDGE}，
 * 参考 Demo 见 app/src/main/assets/local_sample.html
 *
 * @author Brandon
 */
final class FTWebViewHandler implements WebAppInterface.JsReceiver {

    private static final String LOG_TAG = Constants.LOG_TAG_PREFIX + "FTWebViewHandler";

    /**
     * 回调状态，true 为正常，反之为 false
     */
    public static final String WEB_JS_STATUS = "status";

    /**
     * 用于标记回调标签，数值为 time ms
     */
    public static final String WEB_JS_INNER_TAG = "_tag";
    /**
     * RUM 数据
     */
    public static final String WEB_JS_TYPE_RUM = "rum";
    /**
     * 指标类型传输
     */
    public static final String WEB_JS_TYPE_TRACK = "track";
    /**
     * 日志类型数据
     */
    public static final String WEB_JS_TYPE_LOG = "log";
    /**
     * url 验证，用于验证地址属于白名单
     */
    public static final String WEB_JS_TYPE_URL_VERIFY = "urlVerify";

    /**
     * 传递方法名
     */
    public static final String WEB_JS_NAME = "name";
    /**
     * 方法参数
     */
    public static final String WEB_JS_DATA = "data";

    /**
     * 需要配合 Web
     */
    private static final String FT_WEB_VIEW_JAVASCRIPT_BRIDGE = "FTWebViewJavascriptBridge";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private WebView mWebView;

    /**
     * 在 web view 注册 js 方法
     *
     * @param webview
     */
    public void setWebView(WebView webview) {
        mWebView = webview;
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new WebAppInterface(webview.getContext(), this), FT_WEB_VIEW_JAVASCRIPT_BRIDGE);

    }


    public FTWebViewHandler() {
    }


    @Override
    public void sendEvent(String s) {
        sendEvent(s, null);
    }

    /**
     * datakit-js 在 webview 调用的通道方法，处理 datakit 业务逻辑相关
     * {@link #WEB_JS_NAME}
     * {@link #WEB_JS_TYPE_RUM}
     * {@link #WEB_JS_TYPE_TRACK}  (未涉及)
     * {@link #WEB_JS_TYPE_LOG} (未涉及)
     * {@link #WEB_JS_TYPE_URL_VERIFY} (未涉及)
     *
     * @param s
     * @param callbackMethod
     */
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

                    JSONObject publicTags = FTRUMConfigManager.get().getRUMPublicDynamicTags(true);
                    Iterator<String> keys = publicTags.keys();
                    if (tags == null) {
                        tags = new JSONObject();
                    }
                    if (fields == null) {
                        fields = new JSONObject();
                    }
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (!key.equals(Constants.KEY_SERVICE)) {
                            tags.put(key, publicTags.opt(key));
                        }
                    }

                    String sessionId = FTRUMGlobalManager.get().getSessionId();
                    tags.put(Constants.KEY_RUM_SESSION_ID, sessionId);
                    tags.put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true);
                    fields.put(Constants.KEY_RUM_VIEW_IS_ACTIVE, false);

                    long time = data.optLong(Constants.TIME);
                    String measurement = data.optString(Constants.MEASUREMENT);
                    FTTrackInner.getInstance().rumWebView(time, measurement, tags, fields);
                }

            } else if (name.equals(WEB_JS_TYPE_TRACK)) {
                //no use
            } else if (name.equals(WEB_JS_TYPE_LOG)) {
                //no use
            } else if (name.equals(WEB_JS_TYPE_URL_VERIFY)) {
                //no use
            }
            if (callbackMethod != null && !callbackMethod.isEmpty()) {
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

    /**
     * 添加监听方法
     *
     * @param s
     * @param callBackMethod
     */

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

    /**
     * 调用 webview 页面 js 方法
     *
     * @param method
     */
    private void callJsMethod(String method) {
        callJsMethod(method, null);
    }

    /**
     * 调用 webview 页面 js 方法
     *
     * @param requestTag
     * @param callBackMethod 回调方法
     * @param retJsonString  调用成功，返回 json 数据
     * @param errJsonString  调用是失败，返回 json 数据
     */
    private void callbackFromNative(String requestTag, String callBackMethod, String retJsonString, String errJsonString) {
        String separate = !retJsonString.isEmpty() && !errJsonString.isEmpty() ? "," : "";
        String jsStr = callBackMethod + "[\"" + requestTag + "\"](" + retJsonString + separate + errJsonString + ")";
        callJsMethod(jsStr);

    }

    /**
     * 调用 webview 页面 js 方法，并接受页面回调
     *
     * @param method   js 方法
     * @param callback 回调
     */
    private void callJsMethod(final String method, final CallbackFromJS callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.evaluateJavascript(method, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            if (callback != null) {
                                callback.callBack(value);
                            }
                        }
                    });
                } else {
                    LogUtils.e(LOG_TAG, "This Android Device may be low than 4.4, can't get js call Back ");
                    mWebView.loadUrl("javascript:" + method);
                }

            }
        });

    }


    /**
     * 页面 js 回调
     */
    interface CallbackFromJS {
        void callBack(String content);
    }


}
