package com.ft.sdk.feature;

import com.ft.sdk.api.SdkCore;
import com.ft.sdk.sessionreplay.internal.SessionReplayRecordCallback;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public interface FeatureSdkCore extends SdkCore {

    /**
     * Logger for the internal SDK purposes.
     */
     InternalLogger getInternalLogger();

    /**
     * Registers a feature to this instance of the Datadog SDK.
     *
     * @param feature the feature to be registered.
     */
     void registerFeature(Feature feature);

    /**
     * Retrieves a registered feature.
     *
     * @param featureName the name of the feature to retrieve
     * @return the registered feature with the given name, or null
     */
     FeatureScope getFeature(String featureName);

    /**
     * Updates the context if exists with the new entries. If there is no context yet for the
     * provided [featureName], a new one will be created.
     *
     * @param featureName Feature name.
     * @param updateCallback Provides current feature context for the update. If there is no feature
     * with the given name registered, callback won't be called.
     */
     void updateFeatureContext(String featureName, SessionReplayRecordCallback.UpdateCallBack updateCallback);

    /**
     * Retrieves the context for the particular feature.
     *
     * @param featureName Feature name.
     * @return Context for the given feature or empty map if feature is not registered.
     */
     Map<String, Object> getFeatureContext(String featureName);

    /**
     * Sets event receiver for the given feature.
     *
     * @param featureName Feature name.
     * @param receiver Event receiver.
     */
     void setEventReceiver(String featureName, FeatureEventReceiver receiver);

    /**
     * Sets context update receiver for the given feature.
     *
     * @param featureName Feature name.
     * @param listener Listener to remove.
     */
     void setContextUpdateReceiver(String featureName, FeatureContextUpdateReceiver listener);

    /**
     * Removes context update listener for the given feature.
     *
     * @param featureName Feature name.
     * @param listener Listener to remove.
     */
     void removeContextUpdateReceiver(String featureName, FeatureContextUpdateReceiver listener);

    /**
     * Removes events receive for the given feature.
     *
     * @param featureName Feature name.
     */
     void removeEventReceiver(String featureName);

    /**
     * Returns a new single thread [ExecutorService], set up with backpressure and internal monitoring.
     *
     * @param executorContext Context to be used for logging and naming threads running on this executor.
     */
     ExecutorService createSingleThreadExecutorService(String executorContext);

    /**
     * Returns a new [ScheduledExecutorService], set up with internal monitoring.
     * It will use a default of one thread and can spawn at most as many thread as there are CPU cores.
     *
     * @param executorContext Context to be used for logging and naming threads running on this executor.
     */
     ScheduledExecutorService createScheduledExecutorService(String executorContext);
}

