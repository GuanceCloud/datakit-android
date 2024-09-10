package com.ft.sdk;

import android.content.Context;

import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureContextUpdateReceiver;
import com.ft.sdk.feature.FeatureEventReceiver;
import com.ft.sdk.feature.FeatureScope;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.SDKFeature;
import com.ft.sdk.sessionreplay.SessionInnerLogger;
import com.ft.sdk.sessionreplay.internal.SessionReplayRecordCallback;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;


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

    private Context context;

    public void init(Context context) {
        this.context = context;
    }


    @Override
    public InternalLogger getInternalLogger() {
        return new SessionInnerLogger();
    }

    @Override
    public void registerFeature(Feature feature) {
        SDKFeature scope = new SDKFeature(feature, getInternalLogger());
        features.put(feature.getName(), scope);
        scope.init(context, null);
    }

    @Override
    public FeatureScope getFeature(String featureName) {
        return features.get(featureName);
    }

    @Override
    public void updateFeatureContext(String featureName, SessionReplayRecordCallback.UpdateCallBack updateCallback) {
        SDKFeature scope = features.get(featureName);
        if (scope != null) {
            synchronized (scope) {
                //fixme
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
        return null;
    }

    @Override
    public ScheduledExecutorService createScheduledExecutorService(String executorContext) {
        return null;
    }

    @Override
    public String getName() {
        return "session-replay";
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

}
