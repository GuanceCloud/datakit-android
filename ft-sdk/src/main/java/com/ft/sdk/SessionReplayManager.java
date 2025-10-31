package com.ft.sdk;

import static com.ft.sdk.feature.Feature.SESSION_REPLAY_FEATURE_NAME;
import static com.ft.sdk.feature.Feature.SESSION_REPLAY_RESOURCES_FEATURE_NAME;

import android.app.Activity;
import android.content.Context;

import com.ft.sdk.api.TrackingConsentProvider;
import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureContextUpdateReceiver;
import com.ft.sdk.feature.FeatureEventReceiver;
import com.ft.sdk.feature.FeatureScope;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.garble.threadpool.ThreadPoolFactory;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.sessionreplay.SDKFeature;
import com.ft.sdk.sessionreplay.SessionInnerLogger;
import com.ft.sdk.sessionreplay.SessionReplayFeature;
import com.ft.sdk.sessionreplay.internal.SessionReplayRecordCallback;
import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;
import com.ft.sdk.sessionreplay.internal.storage.NoOpRecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;


public class SessionReplayManager implements FeatureSdkCore {

    private static final String TAG = "SessionReplayManager";

    private static final String MISSING_FEATURE_FOR_EVENT_RECEIVER =
            "Cannot add event receiver for feature \"%s\", it is not registered.";
    private static final String EVENT_RECEIVER_ALREADY_EXISTS =
            "Feature \"%s\" already has event receiver registered, overwriting it.";

    private static class SingletonHolder {
        private static final SessionReplayManager INSTANCE = new SessionReplayManager();
    }

    public static SessionReplayManager get() {
        return SessionReplayManager.SingletonHolder.INSTANCE;
    }

    private final Map<String, SDKFeature> features = new ConcurrentHashMap<>();

    private final TrackingConsentProvider trackingConsentProvider = new TrackingConsentProvider(TrackingConsent.NOT_GRANTED);

    private Context context;

//    private RecordWriter currentSessionWriter = new NoOpRecordWriter();

    private SessionReplayFeature sessionReplayFeature;
    private String privacyLevel = "mask";
    private String[] rumLinkKeys = new String[]{};

    public void init(Context context) {
        this.context = context;
    }

    public void setConsentProvider(TrackingConsent consentProvider) {
        this.trackingConsentProvider.setConsent(consentProvider);
    }

    public TrackingConsent getConsentProvider() {
        return this.trackingConsentProvider.getConsent();
    }

    private final Map<String, Map<String, Object>> fieldLinkMap = new ConcurrentHashMap<>();
    private final Map<String, Object> tagLinkMap = new ConcurrentHashMap<>();
    private static final int FIELD_LINK_MAP_CAPACITY = 5;
    private final Deque<String> fieldLinkOrder = new ArrayDeque<>();

    @Override
    public long getErrorTimeLine() {
        return SyncTaskManager.get().getErrorTimeLine();
    }

    @Override
    public InternalLogger getInternalLogger() {
        return new SessionInnerLogger();
    }

    @Override
    public void registerFeature(Feature feature) {
        SDKFeature scope = new SDKFeature(this, feature, getInternalLogger(), trackingConsentProvider);
        features.put(feature.getName(), scope);
        scope.init(context, null);
        if (feature instanceof SessionReplayFeature) {
            sessionReplayFeature = (SessionReplayFeature) feature;
            privacyLevel = ((SessionReplayFeature) feature).getPrivacyLevel();
            rumLinkKeys = ((SessionReplayFeature) feature).getLinkRumKeys();
            if (rumLinkKeys.length > 0) {
                appendSessionReplayRUMLinkKeys(FTTrackInner.getInstance().getSessionReplayRUMLinksKeys(rumLinkKeys));
            }
        }
    }

    public RecordWriter getCurrentSessionWriter() {
        return sessionReplayFeature.isRecording() ? sessionReplayFeature.getDataWriter()
                : new NoOpRecordWriter();
    }

    public void tryGetFullSnapshotForLinkView() {
        if (sessionReplayFeature.isRecording()) {
            sessionReplayFeature.forceFullSnapShotForLinkView();
        }
    }

    public String getPrivacyLevel() {
        return privacyLevel;
    }

    public String[] getRumLinkKeys() {
        return rumLinkKeys;
    }

    @Override
    public FeatureScope getFeature(String featureName) {
        return features.get(featureName);
    }


    public boolean isReplayEnable() {
        return features.containsKey(SESSION_REPLAY_FEATURE_NAME);
    }

    public boolean isReplayResourceEnable() {
        return features.containsKey(SESSION_REPLAY_RESOURCES_FEATURE_NAME);
    }

    void appendSessionReplayRUMLinkKeys(String key, Object value) {
        if (rumLinkKeys != null && rumLinkKeys.length > 0) {
            tagLinkMap.put(key, value);
        }
    }

    void appendSessionReplayRUMLinkKeys(Map<String, Object> maps) {
        tagLinkMap.putAll(maps);
    }

    void appendSessionReplayRUMLinkKeysWithView(String viewId, Map<String, Object> property) {
        if (property == null) return;
        if (rumLinkKeys != null && rumLinkKeys.length > 0) {
            if (!Utils.isNullOrEmpty(viewId)) {
                HashMap<String, Object> rumLinkData = new HashMap<>();

                // Check keys in fieldMaps
                for (String rumKey : rumLinkKeys) {
                    for (String fieldKey : property.keySet()) {
                        if (fieldKey.contains(rumKey)) {
                            rumLinkData.put(fieldKey, property.get(fieldKey));
                        }
                    }
                }

                // Store matched data to global hashMap if any matches found (cap size to 10 by removing oldest)
                if (!rumLinkData.isEmpty()) {
                    synchronized (fieldLinkMap) {
                        boolean isNewKey = !fieldLinkMap.containsKey(viewId);
                        fieldLinkMap.put(viewId, rumLinkData);
                        if (isNewKey) {
                            fieldLinkOrder.addLast(viewId);
                            while (fieldLinkOrder.size() > FIELD_LINK_MAP_CAPACITY) {
                                String eldestKey = fieldLinkOrder.removeFirst();
                                fieldLinkMap.remove(eldestKey);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean checkFieldContextChanged(String viewId, Map<String, Object> map) {
        return Utils.checkContextChanged(viewId, fieldLinkMap, map);
    }

    public Map<String, Map<String, Object>> getFieldLinkMap() {
        return fieldLinkMap;
    }

    public Map<String, Object> getTagLinkMap() {
        return tagLinkMap;
    }

    @Override
    public void updateFeatureContext(String featureName, SessionReplayRecordCallback.UpdateCallBack updateCallback) {
        SDKFeature scope = features.get(featureName);
        if (scope != null) {
            synchronized (scope) {
                Map<String, Object> featureContext = getFeatureContext(featureName);
                updateCallback.onUpdate(featureContext);

                for (String key : features.keySet()) {
                    if (!key.equals(featureName)) {
                        SDKFeature feature = features.get(key);
                        if (feature != null) {
                            feature.notifyContextUpdated(featureName, featureContext);
                        }
                    }
                }
                FTRUMInnerManager.get().updateSessionViewMap(featureContext);
            }
        }
    }

    @Override
    public Map<String, Object> getFeatureContext(String featureName) {
        return FTRUMInnerManager.get().getCurrentViewSession();
    }

    @Override
    public void setEventReceiver(String featureName, FeatureEventReceiver receiver) {
        SDKFeature scope = features.get(featureName);
        if (scope == null) {
            getInternalLogger().w(TAG, String.format(MISSING_FEATURE_FOR_EVENT_RECEIVER, featureName));
        } else {
            if (scope.getEventReceiver().get() != null) {
                getInternalLogger().w(TAG, String.format(EVENT_RECEIVER_ALREADY_EXISTS, featureName));
            }
            scope.getEventReceiver().set(receiver);
        }
    }

    @Override
    public void setContextUpdateReceiver(String featureName, FeatureContextUpdateReceiver listener) {
        SDKFeature scope = features.get(featureName);
        if (scope != null) {
            scope.setContextUpdateListener(listener);
        }
    }

    @Override
    public void removeContextUpdateReceiver(String featureName, FeatureContextUpdateReceiver listener) {
        SDKFeature scope = features.get(featureName);
        if (scope != null) {
            scope.removeContextUpdateListener(listener);
        }
    }

    @Override
    public void removeEventReceiver(String featureName) {
        SDKFeature scope = features.get(featureName);
        if (scope != null) {
            scope.getEventReceiver().set(null);
        }
    }

    @Override
    public ExecutorService createSingleThreadExecutorService(String executorContext) {
        return new ThreadPoolFactory(executorContext).getExecutor();
    }

    @Override
    public ScheduledExecutorService createScheduledExecutorService(String executorContext) {
        //no use
        return new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "FT-" + executorContext);
                thread.setPriority(4);
                return thread;
            }
        });
    }

    @Override
    public String getName() {
        return SESSION_REPLAY_FEATURE_NAME;
    }

    @Override
    public String getService() {
        return "";
    }


    void stop() {
        for (SDKFeature value : features.values()) {
            value.stop();
        }
        features.clear();
    }

    @Override
    public Activity curentActivity() {
        return FTActivityManager.get().curentActivity();
    }
}
