package com.ft.sdk;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.TBSWebViewUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;
import com.ft.sdk.sessionreplay.webview.DataBatcher;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Used to receive application-level webview data indicators
 * Requires use of web js sdk
 * <a href="https://github.com/GuanceCloud/datakit-js">datakit-js</a>,
 * {@link #FT_WEB_VIEW_JAVASCRIPT_BRIDGE}，
 * See Demo in app/src/main/assets/local_sample.html
 *
 * @author Brandon
 */
final class FTWebViewHandler implements WebAppInterface.JsReceiver {

    private static final String LOG_TAG = Constants.LOG_TAG_PREFIX + "FTWebViewHandler";

    /**
     * Callback status, true is normal, false is abnormal
     */
    public static final String WEB_JS_STATUS = "status";

    /**
     * Used to mark the callback tag, the value is time ms
     */
    public static final String WEB_JS_INNER_TAG = "_tag";
    /**
     * RUM data
     */
    public static final String WEB_JS_TYPE_RUM = "rum";

    /**
     * webview session replay data
     */
    public static final String WEB_JS_TYPE_SESSION_REPLAY = "session_replay";
    /**
     * Indicator type transmission
     */
    public static final String WEB_JS_TYPE_TRACK = "track";
    /**
     * Log type data
     */
    public static final String WEB_JS_TYPE_LOG = "log";
    /**
     * url verification, used to verify that the address belongs to the whitelist
     */
    public static final String WEB_JS_TYPE_URL_VERIFY = "urlVerify";

    /**
     * Pass method name
     */
    public static final String WEB_JS_NAME = "name";
    /**
     * Method parameters
     */
    public static final String WEB_JS_DATA = "data";

    /**
     * Requires use of Web
     */
    private static final String FT_WEB_VIEW_JAVASCRIPT_BRIDGE = "FTWebViewJavascriptBridge";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private String nativeViewName;
    private long slotID;
    private String webViewId;
    private String nativeViewId;

    private DataBatcher dataBatcher;
    private View mWebView;

    /**
     * Register js method in web view
     *
     * @param webview
     */
    public void setWebView(View webview) {
        FTRUMConfig config = FTRUMConfigManager.get().getConfig();
        if (config.isRumEnable() && config.isEnableTraceWebView()) {
            if (FTSdk.isSessionReplaySupport()) {
                setWebView(webview, config.getAllowWebViewHost(),
                        SessionReplayManager.get().getPrivacyLevel(), new String[]{"records"});
            } else {
                setWebView(webview, config.getAllowWebViewHost());
            }
        }
    }

    private void setWebView(View webview, String[] allowWebViewHost) {
        setWebView(webview, allowWebViewHost, null, null);
    }

    private void setWebView(View webview, String[] allowWebViewHost,
                            String privacyLevel, String[] capabilities) {
        mWebView = webview;
        Activity activity = AopUtils.getActivityFromContext(webview.getContext());
        nativeViewName = AopUtils.getClassName(activity);
        nativeViewId = FTRUMInnerManager.get().getViewId();//  while be error,when WebView -> NativeView -> WebView
        if (webview instanceof WebView) {
            ((WebView) webview).getSettings().setJavaScriptEnabled(true);
            ((WebView) webview).addJavascriptInterface(new WebAppInterface(webview.getContext(), this,
                            allowWebViewHost, privacyLevel, capabilities),
                    FT_WEB_VIEW_JAVASCRIPT_BRIDGE);
        }
        // Handle TBS WebView using optimized utility
        else if (TBSWebViewUtils.isTBSWebViewInstance(webview)) {
            TBSWebViewUtils.setJavaScriptEnabled(webview, true);
            TBSWebViewUtils.addJavascriptInterface(webview, new WebAppInterface(webview.getContext(), this,
                            allowWebViewHost, privacyLevel, capabilities),
                    FT_WEB_VIEW_JAVASCRIPT_BRIDGE);
        }
        webview.setTag(R.id.ft_webview_handled_tag_view_value, "handled");
        if (capabilities != null && capabilities.length != 0) {
            slotID = System.identityHashCode(webview);
            dataBatcher = new DataBatcher(SessionReplayManager.get().getCurrentSessionWriter());
        }
    }


    public FTWebViewHandler() {
    }


    @Override
    public void sendEvent(String s) {
        sendEvent(s, null);
    }

    /**
     * datakit-js in webview called channel method, processing datakit business logic related
     * {@link #WEB_JS_NAME}
     * {@link #WEB_JS_TYPE_RUM}
     * {@link #WEB_JS_TYPE_TRACK}  (not involved)
     * {@link #WEB_JS_TYPE_LOG} (not involved)
     * {@link #WEB_JS_TYPE_URL_VERIFY} (not involved)
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
                    JSONObject jsonTags = data.optJSONObject(Constants.TAGS);
                    JSONObject jsonFields = data.optJSONObject(Constants.FIELDS);
                    if (jsonTags == null) {
                        jsonTags = new JSONObject();
                    }
                    if (jsonFields == null) {
                        jsonFields = new JSONObject();
                    }

                    String sessionId = FTRUMInnerManager.get().getSessionId();
                    webViewId = jsonTags.optString("view_id");
                    jsonTags.put(Constants.KEY_RUM_SESSION_ID, sessionId);
                    jsonTags.put(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true);

                    String measurement = data.optString(Constants.MEASUREMENT);
                    if (Constants.FT_MEASUREMENT_RUM_VIEW.equals(measurement)) {
                        jsonFields.put(Constants.KEY_RUM_VIEW_IS_ACTIVE, false);
                    }
                    String referrer = jsonTags.optString(Constants.KEY_RUM_VIEW_REFERRER);
                    if (Utils.isNullOrEmpty(referrer)) {
                        jsonTags.put(Constants.KEY_RUM_VIEW_REFERRER, nativeViewName);
                    }

                    HashMap<String, Object> dynamicTags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
                    HashMap<String, Object> tagMaps = Utils.jsonToMap(jsonTags);
                    tagMaps.putAll(dynamicTags);
                    HashMap<String, Object> fieldMaps = Utils.jsonToMap(jsonFields);

                    long time = data.optLong(Constants.TIME) * 1000000;
                    if (measurement.equals(Constants.FT_MEASUREMENT_RUM_ERROR)) {
                        SyncTaskManager.get().setErrorTimeLine(time, null);
                    }

                    if (FTSdk.isSessionReplaySupport()
                            && !nativeViewId.equals(SessionReplayRumContext.NULL_UUID)) {
                        HashMap<String, String> source = new HashMap<>();
                        source.put("source", "android");
                        source.put("view_id", nativeViewId);
                        tagMaps.put("container", Utils.hashMapObjectToJson(source));
                    }

                    CollectType collectType = FTRUMInnerManager.get().checkSessionWillCollect(sessionId);
                    FTTrackInner.getInstance().rumWebView(time, measurement,
                            tagMaps, fieldMaps, collectType);
                }

            } else if (name.equals(WEB_JS_TYPE_SESSION_REPLAY)) {
                if (!FTSdk.isSessionReplaySupport()) return;
                if (data != null) {
                    data.put("slotId", slotID + "");
                    SessionReplayRumContext newContext = new SessionReplayRumContext(
                            FTRUMInnerManager.get().getApplicationID(),
                            FTRUMInnerManager.get().getSessionId(),
                            webViewId);
                    dataBatcher.onData(newContext, MobileRecord.MobileWebviewSnapshotRecord.fromJson(data.toString()));
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

        } catch (
                Exception e) {
            LogUtils.e(LOG_TAG, e.getMessage());
        }


    }

    /**
     * Add listener method
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
     * Call webview page js method
     *
     * @param method
     */
    private void callJsMethod(String method) {
        callJsMethod(method, null);
    }

    /**
     * Call webview page js method
     *
     * @param requestTag
     * @param callBackMethod callback method
     * @param retJsonString  Call successfully, return json data
     * @param errJsonString  Call is failed, return json data
     */
    private void callbackFromNative(String requestTag, String callBackMethod, String retJsonString, String errJsonString) {
        String separate = !retJsonString.isEmpty() && !errJsonString.isEmpty() ? "," : "";
        String jsStr = callBackMethod + "[\"" + requestTag + "\"](" + retJsonString + separate + errJsonString + ")";
        callJsMethod(jsStr);

    }

    /**
     * Call webview page js method, and accept page callback
     * Note: This method is called from sendEvent but we don't have direct access to the WebView
     * The actual JavaScript execution should be handled by the calling context
     *
     * @param method   js method
     * @param callback callback
     */
    private void callJsMethod(final String method, final CallbackFromJS callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mWebView instanceof WebView) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        ((WebView) mWebView).evaluateJavascript(method, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                if (callback != null) {
                                    callback.callBack(value);
                                }
                            }
                        });
                    } else {
                        LogUtils.e(LOG_TAG, "This Android Device may be low than 4.4, can't get js call Back ");
                        ((WebView) mWebView).loadUrl("javascript:" + method);
                    }
                } else if (TBSWebViewUtils.isTBSWebViewInstance(mWebView)) {
                    TBSWebViewUtils.evaluateJavascript(mWebView, method, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            if (callback != null) {
                                callback.callBack(value);
                            }
                        }
                    });
                } else {
                    LogUtils.w(LOG_TAG, "Unsupported WebView type for JavaScript execution");
                }

            }
        });
    }


    /**
     * Page js callback
     */
    interface CallbackFromJS {
        void callBack(String content);
    }


}