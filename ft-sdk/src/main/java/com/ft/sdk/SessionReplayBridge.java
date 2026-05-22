package com.ft.sdk;

import android.content.Context;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reflection boundary for the optional ft-session-replay dependency.
 * <p>
 * Keep direct references to replay classes inside this bridge so ft-sdk can be used without
 * ft-session-replay on the application classpath.
 */
final class SessionReplayBridge {

    static final String NULL_UUID = new UUID(0, 0).toString();

    /**
     * These keys must stay aligned with SessionReplayRecordCallback metadata.
     */
    static final String HAS_REPLAY_KEY = "has_replay";
    static final String VIEW_RECORDS_COUNT_KEY = "records_count";
    static final String SAMPLED_ON_ERROR = "sampled_on_reply_error";

    private static final String TAG = "SessionReplayBridge";

    /**
     * Replay string constants mirrored from the optional module.
     */
    private static final String FEATURE_NAME = "session-replay";
    private static final String TRACKING_CONSENT_SAMPLED_ON_ERROR = "SAMPLED_ON_ERROR_SESSION";

    interface ViewChangeCallback {
        void onViewChanged(String viewId);
    }

    interface SlotRebindCallback {
        void onSlotRebound(long slotId);
    }

    private SessionReplayBridge() {
    }

    static boolean isReplayAvailable() {
        try {
            // PackageUtils checks BuildConfig by name and does not load replay runtime classes.
            return PackageUtils.isSessionReplay();
        } catch (Throwable ignored) {
            return false;
        }
    }

    static boolean isReplayEnabled() {
        if (!isReplayAvailable()) {
            return false;
        }
        Object manager = getManager();
        Boolean enabled = invokeBoolean(manager, "isReplayEnable");
        return enabled != null && enabled;
    }

    static void enable(Object sessionReplayConfig, Context context) {
        // The public entry accepts Object to avoid a hard method-signature dependency on replay.
        if (sessionReplayConfig == null) {
            LogUtils.e(TAG, "Session Replay config cannot be null");
            return;
        }
        if (!isReplayAvailable()) {
            LogUtils.e(TAG, "ft-session-replay not integrated, Session Replay enable skipped");
            return;
        }
        if (SDKVersionValidator.validateSDKVersions()) {
            LogUtils.e(TAG, "SDK version mismatch detected, SessionReplay enable failed");
            return;
        }
        try {
            Class<?> configClass = Class.forName("com.ft.sdk.sessionreplay.FTSessionReplayConfig");
            if (!configClass.isInstance(sessionReplayConfig)) {
                LogUtils.e(TAG, "Invalid Session Replay config type:" + sessionReplayConfig.getClass().getName());
                return;
            }

            Object manager = getManager();
            if (manager == null) {
                LogUtils.e(TAG, "SessionReplayManager unavailable");
                return;
            }

            invoke(manager, "init", new Class[]{Context.class}, context);

            // Construct SessionReplayFeature reflectively only after replay is confirmed present.
            Class<?> featureSdkCoreClass = Class.forName("com.ft.sdk.feature.FeatureSdkCore");
            Class<?> sessionReplayFeatureClass = Class.forName("com.ft.sdk.sessionreplay.SessionReplayFeature");
            Constructor<?> constructor = sessionReplayFeatureClass.getConstructor(featureSdkCoreClass, configClass);
            Object sessionReplayFeature = constructor.newInstance(manager, sessionReplayConfig);

            Class<?> featureClass = Class.forName("com.ft.sdk.feature.Feature");
            invoke(manager, "registerFeature", new Class[]{featureClass}, sessionReplayFeature);
            LogUtils.d(TAG, "init SR:" + sessionReplayConfig);
        } catch (Throwable e) {
            LogUtils.e(TAG, "Session Replay enable failed:\n" + LogUtils.getStackTraceString(e));
        }
    }

    static void mergeConfigFromCache(Object sessionReplayConfig, Float sampleRate,
                                     Float onErrorSampleRate) {
        if (sessionReplayConfig == null) {
            return;
        }
        if (sampleRate != null) {
            invoke(sessionReplayConfig, "setSampleRate", new Class[]{float.class}, sampleRate);
        }
        if (onErrorSampleRate != null) {
            invoke(sessionReplayConfig, "setSessionReplayOnErrorSampleRate",
                    new Class[]{float.class}, onErrorSampleRate);
        }
    }

    static void stop() {
        Object manager = getManagerIfEnabled();
        invokeDeclared(manager, "stop");
    }

    static void sendSessionRenewed(String sessionId, CollectType collectType, boolean forceFresh) {
        Object manager = getManagerIfEnabled();
        if (manager == null) {
            return;
        }
        Object scope = invoke(manager, "getFeature", new Class[]{String.class}, FEATURE_NAME);
        if (scope == null) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        // The replay feature consumes this event to rotate writers when RUM starts a new session.
        map.put("type", "rum_session_renewed");
        map.put("collect_key", collectType.getValue());
        map.put("sessionId", sessionId);
        map.put("force_refresh", forceFresh);
        invoke(scope, "sendEvent", new Class[]{Object.class}, map);
    }

    static void appendRumLinkKeys(Map<String, Object> values) {
        Object manager = getManagerIfEnabled();
        invoke(manager, "appendSessionReplayRUMLinkKeys", new Class[]{Map.class}, values);
    }

    static void appendRumLinkKey(String key, Object value) {
        Object manager = getManagerIfEnabled();
        invoke(manager, "appendSessionReplayRUMLinkKeys", new Class[]{String.class, Object.class}, key, value);
    }

    static void appendRumLinkKeysWithView(String viewId, Map<String, Object> values) {
        Object manager = getManagerIfEnabled();
        invoke(manager, "appendSessionReplayRUMLinkKeysWithView", new Class[]{String.class, Map.class},
                viewId, values);
    }

    static boolean checkFieldContextChanged(String viewId, Map<String, Object> values) {
        Object manager = getManagerIfEnabled();
        Boolean changed = invokeBoolean(manager, "checkFieldContextChanged",
                new Class[]{String.class, Map.class}, viewId, values);
        return changed != null && changed;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> getTagLinkMap() {
        Object manager = getManagerIfEnabled();
        Object map = invoke(manager, "getTagLinkMap");
        if (map instanceof Map) {
            return (Map<String, Object>) map;
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    static Map<String, Map<String, Object>> getFieldLinkMap() {
        Object manager = getManagerIfEnabled();
        Object map = invoke(manager, "getFieldLinkMap");
        if (map instanceof Map) {
            return (Map<String, Map<String, Object>>) map;
        }
        return new HashMap<>();
    }

    static void tryGetFullSnapshotForLinkView() {
        Object manager = getManagerIfEnabled();
        invoke(manager, "tryGetFullSnapshotForLinkView");
    }

    static String getPrivacyLevel() {
        Object manager = getManagerIfEnabled();
        Object privacyLevel = invoke(manager, "getPrivacyLevel");
        return privacyLevel instanceof String ? (String) privacyLevel : "mask";
    }

    static String[] getRumLinkKeys() {
        Object manager = getManagerIfEnabled();
        Object keys = invoke(manager, "getRumLinkKeys");
        return keys instanceof String[] ? (String[]) keys : new String[]{};
    }

    static void setSampleRate(float sampleRate) {
        Object manager = getManagerIfEnabled();
        invoke(manager, "setSampleRate", new Class[]{float.class}, sampleRate);
    }

    static void setSessionReplayOnErrorSampleRate(float sampleRate) {
        Object manager = getManagerIfEnabled();
        invoke(manager, "setSessionReplayOnErrorSampleRate", new Class[]{float.class}, sampleRate);
    }

    static Float sampleRate() {
        Object manager = getManagerIfEnabled();
        Object sampleRate = invoke(manager, "sampleRate");
        return sampleRate instanceof Float ? (Float) sampleRate : null;
    }

    static Float sessionReplayOnErrorSampleRate() {
        Object manager = getManagerIfEnabled();
        Object sampleRate = invoke(manager, "sessionReplayOnErrorSampleRate");
        return sampleRate instanceof Float ? (Float) sampleRate : null;
    }

    static void hotUpdate(Float sampleRate, Float onErrorSampleRate) {
        Object manager = getManagerIfEnabled();
        invoke(manager, "hotUpdate", new Class[]{Float.class, Float.class}, sampleRate, onErrorSampleRate);
    }

    static Object newWebViewDataBatcher(boolean isDCWebView) {
        Object manager = getManagerIfEnabled();
        if (manager == null) {
            return null;
        }
        try {
            Object internalLogger = invoke(manager, "getInternalLogger");
            Class<?> internalLoggerClass = Class.forName("com.ft.sdk.sessionreplay.utils.InternalLogger");
            Class<?> dataBatcherClass = Class.forName("com.ft.sdk.sessionreplay.webview.DataBatcher");
            Class<?> writerCallbackClass = Class.forName("com.ft.sdk.sessionreplay.webview.DataBatcher$WriterCallback");
            // DataBatcher owns replay types, so expose only the writer callback through a proxy.
            Object writerCallback = Proxy.newProxyInstance(writerCallbackClass.getClassLoader(),
                    new Class[]{writerCallbackClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if ("getWriter".equals(method.getName())) {
                                return SessionReplayBridge.invoke(manager, "getCurrentSessionWriter");
                            }
                            return null;
                        }
                    });
            Constructor<?> constructor = dataBatcherClass.getConstructor(
                    internalLoggerClass, boolean.class, writerCallbackClass);
            return constructor.newInstance(internalLogger, isDCWebView, writerCallback);
        } catch (Throwable e) {
            LogUtils.e(TAG, "Create Session Replay WebView data batcher failed:\n"
                    + LogUtils.getStackTraceString(e));
        }
        return null;
    }

    static void onWebViewData(Object dataBatcher, String applicationId, String sessionId,
                              String viewId, Map<String, Object> globalContext, String data) {
        if (dataBatcher == null || !isReplayEnabled()) {
            return;
        }
        try {
            Class<?> contextClass = Class.forName("com.ft.sdk.sessionreplay.utils.SessionReplayRumContext");
            Object context = contextClass.getConstructor(String.class, String.class, String.class, Map.class)
                    .newInstance(applicationId, sessionId, viewId, globalContext);
            Method onData = dataBatcher.getClass().getMethod("onData", contextClass, String.class);
            onData.invoke(dataBatcher, context, data);
        } catch (Throwable e) {
            LogUtils.e(TAG, "Handle Session Replay WebView data failed:\n"
                    + LogUtils.getStackTraceString(e));
        }
    }

    static String getSlotViewId(long slotId) {
        Object binder = getSlotIdWebviewBinder();
        Object viewId = invoke(binder, "getViewId", new Class[]{long.class}, slotId);
        return viewId instanceof String ? (String) viewId : null;
    }

    static void bindSlot(long slotId, String viewId, final ViewChangeCallback callback) {
        Object binder = getSlotIdWebviewBinder();
        if (binder == null) {
            return;
        }
        try {
            Class<?> callbackClass = Class.forName("com.ft.sdk.sessionreplay.SlotIdWebviewBinder$BindViewChangeCallBack");
            // Proxy replay callbacks back into ft-sdk without importing replay callback interfaces.
            Object callbackProxy = Proxy.newProxyInstance(callbackClass.getClassLoader(),
                    new Class[]{callbackClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if ("onViewChanged".equals(method.getName()) && args != null && args.length > 0) {
                                callback.onViewChanged((String) args[0]);
                            }
                            return null;
                        }
                    });
            Method bind = binder.getClass().getMethod("bind", long.class, String.class, callbackClass);
            bind.invoke(binder, slotId, viewId, callbackProxy);
        } catch (Throwable e) {
            LogUtils.e(TAG, "Bind Session Replay WebView slot failed:\n"
                    + LogUtils.getStackTraceString(e));
        }
    }

    static void setSlotRebindCallback(long slotId, final SlotRebindCallback callback) {
        Object binder = getSlotIdWebviewBinder();
        if (binder == null) {
            return;
        }
        try {
            Class<?> callbackClass = Class.forName("com.ft.sdk.sessionreplay.SlotIdWebviewBinder$SlotRebindCallBack");
            // Same optional-dependency boundary as bindSlot; replay owns the callback interface.
            Object callbackProxy = Proxy.newProxyInstance(callbackClass.getClassLoader(),
                    new Class[]{callbackClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if ("onSlotRebound".equals(method.getName()) && args != null && args.length > 0) {
                                callback.onSlotRebound((Long) args[0]);
                            }
                            return null;
                        }
                    });
            Method setCallback = binder.getClass().getMethod("setSlotRebindCallback", long.class, callbackClass);
            setCallback.invoke(binder, slotId, callbackProxy);
        } catch (Throwable e) {
            LogUtils.e(TAG, "Set Session Replay WebView slot rebind callback failed:\n"
                    + LogUtils.getStackTraceString(e));
        }
    }

    static boolean isSlotActive(long slotId) {
        Object binder = getSlotIdWebviewBinder();
        Boolean active = invokeBoolean(binder, "isActive", new Class[]{long.class}, slotId);
        return active != null && active;
    }

    static long getLatestSlotId() {
        Object binder = getSlotIdWebviewBinder();
        Object slotId = invoke(binder, "getLatestSlotId");
        return slotId instanceof Long ? (Long) slotId : 0;
    }

    static boolean isSampledOnErrorSession() {
        Object manager = getManagerIfEnabled();
        Object consent = invoke(manager, "getConsentProvider");
        return consent != null && TRACKING_CONSENT_SAMPLED_ON_ERROR.equals(consent.toString());
    }

    private static Object getSlotIdWebviewBinder() {
        Object manager = getManagerIfEnabled();
        return invoke(manager, "getSlotIdWebviewBinder");
    }

    private static Object getManagerIfEnabled() {
        // Avoid loading SessionReplayManager until replay is both packaged and registered.
        if (!isReplayEnabled()) {
            return null;
        }
        return getManager();
    }

    private static Object getManager() {
        try {
            Class<?> managerClass = Class.forName("com.ft.sdk.SessionReplayManager");
            return managerClass.getMethod("get").invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Boolean invokeBoolean(Object target, String methodName, Class<?>[] parameterTypes,
                                         Object... args) {
        Object value = invoke(target, methodName, parameterTypes, args);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private static Boolean invokeBoolean(Object target, String methodName) {
        Object value = invoke(target, methodName);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private static Object invoke(Object target, String methodName) {
        return invoke(target, methodName, new Class[]{});
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        if (target == null) {
            return null;
        }
        try {
            Method method;
            try {
                method = target.getClass().getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                // Some bridge targets are package-private methods on SessionReplayManager.
                method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object invokeDeclared(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
