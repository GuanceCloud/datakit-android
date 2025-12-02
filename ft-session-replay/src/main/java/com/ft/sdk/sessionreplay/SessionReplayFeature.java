package com.ft.sdk.sessionreplay;


import android.app.Application;
import android.content.Context;

import com.ft.sdk.feature.FeatureEventReceiver;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.feature.FeatureStorageConfiguration;
import com.ft.sdk.sessionreplay.internal.DefaultRecorderProvider;
import com.ft.sdk.sessionreplay.internal.RecorderProvider;
import com.ft.sdk.sessionreplay.internal.ResourcesFeature;
import com.ft.sdk.sessionreplay.internal.SessionReplayRecordCallback;
import com.ft.sdk.sessionreplay.internal.StorageBackedFeature;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;
import com.ft.sdk.sessionreplay.internal.recorder.NoOpRecorder;
import com.ft.sdk.sessionreplay.internal.recorder.Recorder;
import com.ft.sdk.sessionreplay.internal.recorder.SessionReplayRecorder;
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;
import com.ft.sdk.sessionreplay.internal.resources.ResourceHashesEntryDeserializer;
import com.ft.sdk.sessionreplay.internal.resources.ResourceHashesEntrySerializer;
import com.ft.sdk.sessionreplay.internal.storage.NoOpRecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.SessionReplayRecordWriter;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SessionReplayFeature implements StorageBackedFeature, FeatureEventReceiver {

    private static final String TAG = "SessionReplayFeature";
    public static final FeatureStorageConfiguration STORAGE_CONFIGURATION =
            FeatureStorageConfiguration.DEFAULT.copyWith(
                    10 * 1024 * 1024, -1,
                    10 * 1024 * 1024, -1
            );

    public static final String REQUIRES_APPLICATION_CONTEXT_WARN_MESSAGE =
            "Session Replay could not be initialized without the Application context.";

    public static final String SESSION_SAMPLED_OUT_MESSAGE =
            "This session was sampled out from recording. No replay will be provided for it.";

    public static final String UNSUPPORTED_EVENT_TYPE =
            "Session Replay feature receive an event of unsupported type=%s.";

    public static final String UNKNOWN_EVENT_TYPE_PROPERTY_VALUE =
            "Session Replay feature received an event with unknown value of \"type\" property=%s.";

    public static final String EVENT_MISSING_MANDATORY_FIELDS =
            "Session Replay feature received an event where one or more mandatory (collect_key) fields" +
                    " are either missing or have wrong type.";

    public static final String CANNOT_START_RECORDING_NOT_INITIALIZED =
            "Cannot start session recording, because Session Replay feature is not initialized.";


    public static final String SESSION_REPLAY_SAMPLE_RATE_KEY = "session_replay_sample_rate";
    public static final String SESSION_REPLAY_ON_ERROR_SAMPLE_RATE_KEY = "session_replay_on_error_sample_rate";
    //    public static final String SESSION_REPLAY_PRIVACY_KEY = "session_replay_privacy";
    public static final String SESSION_REPLAY_TEXT_AND_INPUT_PRIVACY_KEY = "session_replay_text_and_input_privacy";
    public static final String SESSION_REPLAY_IMAGE_PRIVACY_KEY = "session_replay_image_privacy";
    public static final String SESSION_REPLAY_TOUCH_PRIVACY_KEY = "session_replay_touch_privacy";
    public static final String SESSION_REPLAY_START_IMMEDIATE_RECORDING_KEY =
            "session_replay_start_immediate_recording";
    public static final String SESSION_REPLAY_ENABLED_KEY =
            "session_replay_is_enabled";
    public static final String SESSION_REPLAY_ENABLED_ON_ERROR_KEY =
            "session_replay_is_enabled_on_error";

    private final FeatureSdkCore sdkCore;
    private final String customEndpointUrl;
    private final TouchPrivacy touchPrivacy;
    private final TextAndInputPrivacy textAndInputPrivacy;
    private final ImagePrivacy imagePrivacy;
    private final Sampler sessionRelaySampler;
    private final Sampler sessionRelayErrorSampler;
    private final RecorderProvider recorderProvider;
    private final String[] linkRumKeys;

    private final AtomicReference<String> currentRumSessionId = new AtomicReference<>();

    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    Recorder
            sessionReplayRecorder = new NoOpRecorder();
    RecordWriter
            dataWriter = new NoOpRecordWriter();
    final AtomicBoolean initialized = new AtomicBoolean(false);

    // region Constructor

    public SessionReplayFeature(FeatureSdkCore sdkCore, FTSessionReplayConfig config) {
        this(sdkCore,
                config.getCustomEndpointUrl(),
                config.getTextAndInputPrivacy(),
                config.getTouchPrivacy(),
                new TouchPrivacyManager(config.getTouchPrivacy()),
                config.getImagePrivacy(),
                config.getCustomMappers(),
                config.getCustomOptionSelectorDetectors(),
                config.getCustomDrawableMapper(),
                config.getSampleRate(),
                config.getSessionReplayOnErrorSampleRate(),
                config.isDelayInit(),
                config.isDynamicOptimizationEnabled(),
                config.getInternalCallback(), config.getRumLinkKeys());

    }

    public SessionReplayFeature(FeatureSdkCore sdkCore, String customEndpointUrl,
                                TextAndInputPrivacy textAndInputPrivacy,
                                TouchPrivacy touchPrivacy,
                                ImagePrivacy imagePrivacy,
                                Sampler sessionReplaySampler,
                                Sampler sessionReplayErrorSampler,
                                RecorderProvider recorderProvider, String[] linkRumKeys) {
        this.sdkCore = sdkCore;
        this.customEndpointUrl = customEndpointUrl;
        this.textAndInputPrivacy = textAndInputPrivacy;
        this.touchPrivacy = touchPrivacy;
        this.imagePrivacy = imagePrivacy;
        this.sessionRelaySampler = sessionReplaySampler;
        this.sessionRelayErrorSampler = sessionReplayErrorSampler;
        this.recorderProvider = recorderProvider;
        this.linkRumKeys = linkRumKeys == null ? new String[]{} : linkRumKeys;
    }

    public SessionReplayFeature(FeatureSdkCore sdkCore, String customEndpointUrl,
                                TextAndInputPrivacy textAndInputPrivacy,
                                TouchPrivacy touchPrivacy,
                                TouchPrivacyManager touchPrivacyManager,
                                ImagePrivacy imagePrivacy,
                                List<MapperTypeWrapper<?>> customMappers,
                                List<OptionSelectorDetector> customOptionSelectorDetectors,
                                List<DrawableToColorMapper> customDrawableMappers,
                                float sampleRate,
                                float sessionReplayOnErrorSampleRate,
                                boolean isDelayInit,
                                boolean dynamicOptimizationEnabled,
                                SessionReplayInternalCallback internalCallback, String[] linkRumKeys) {
        this(sdkCore, customEndpointUrl,
                textAndInputPrivacy,
                touchPrivacy,
                imagePrivacy,
                new RateBasedSampler(sampleRate),
                new RateBasedSampler(sessionReplayOnErrorSampleRate),
                new DefaultRecorderProvider(sdkCore,
                        textAndInputPrivacy,
                        imagePrivacy,
                        touchPrivacyManager, customMappers,
                        customOptionSelectorDetectors,
                        customDrawableMappers,
                        dynamicOptimizationEnabled,
                        internalCallback,
                        isDelayInit, linkRumKeys != null && linkRumKeys.length > 0
                ), linkRumKeys);
    }

    public RecordWriter getDataWriter() {
        return dataWriter;
    }

    public String[] getLinkRumKeys() {
        return linkRumKeys;
    }

    public String getPrivacyLevel() {
        if (this.touchPrivacy == TouchPrivacy.SHOW) {
            if (this.textAndInputPrivacy == TextAndInputPrivacy.MASK_SENSITIVE_INPUTS) {
                return "allow";
            } else if (this.textAndInputPrivacy == TextAndInputPrivacy.MASK_ALL_INPUTS) {
                return "mask-user-input";
            }
        }
        return "mask";
    }

    @Override
    public String getName() {
        return SESSION_REPLAY_FEATURE_NAME;
    }

    @Override
    public void onInitialize(Context appContext) {
        if (!(appContext instanceof Application)) {
            sdkCore.getInternalLogger().w(TAG, REQUIRES_APPLICATION_CONTEXT_WARN_MESSAGE);
            return;
        }

        sdkCore.setEventReceiver(getName(), this);

        ResourcesFeature resourcesFeature = registerResourceFeature(sdkCore);

        ResourceDataStoreManager resourceDataStoreManager = new ResourceDataStoreManager(
                sdkCore, new ResourceHashesEntrySerializer(), new ResourceHashesEntryDeserializer(sdkCore.getInternalLogger())
        );

        dataWriter = createDataWriter();
        sessionReplayRecorder = recorderProvider.provideSessionReplayRecorder(
                resourceDataStoreManager, resourcesFeature.getDataWriter(), dataWriter, (Application) appContext
        );
        sessionReplayRecorder.registerCallbacks();
        initialized.set(true);
        sdkCore.updateFeatureContext(getName(), new SessionReplayRecordCallback.UpdateCallBack() {
            @Override
            public void onUpdate(Map<String, Object> context) {
                context.put(SESSION_REPLAY_SAMPLE_RATE_KEY, sessionRelaySampler.getSampleRate() != null ?
                        sessionRelaySampler.getSampleRate() : null);
                context.put(SESSION_REPLAY_ON_ERROR_SAMPLE_RATE_KEY, sessionRelayErrorSampler.getSampleRate() != null ?
                        sessionRelayErrorSampler.getSampleRate() : null);
                context.put(SESSION_REPLAY_IMAGE_PRIVACY_KEY, imagePrivacy.toString().toLowerCase(Locale.US));
                context.put(SESSION_REPLAY_TOUCH_PRIVACY_KEY, touchPrivacy.toString().toLowerCase(Locale.US));
                context.put(SESSION_REPLAY_TEXT_AND_INPUT_PRIVACY_KEY, textAndInputPrivacy.toString().toLowerCase(Locale.US));
                context.put(SESSION_REPLAY_START_IMMEDIATE_RECORDING_KEY, false);
            }
        });
    }


    @Override
    public FeatureStorageConfiguration getStorageConfiguration() {
        return STORAGE_CONFIGURATION;
    }

    @Override
    public void onStop() {
        stopRecording();
        sessionReplayRecorder.unregisterCallbacks();
        sessionReplayRecorder.stopProcessingRecords();
        dataWriter = new NoOpRecordWriter();
        sessionReplayRecorder = new NoOpRecorder();
        initialized.set(false);
    }

    public void forceFullSnapShotForLinkView() {
        if (sessionReplayRecorder instanceof SessionReplayRecorder) {
            ((SessionReplayRecorder) sessionReplayRecorder).forceFullSnapshotForLinkView();
        }
    }

    @Override
    public void onReceive(Object event) {
        if (!(event instanceof Map)) {
            sdkCore.getInternalLogger().w(TAG, String.format(Locale.US, UNSUPPORTED_EVENT_TYPE, event.getClass().getCanonicalName()));
            return;
        }

        handleRumSession((Map<?, ?>) event);
    }

    public void handleRumSession(Map<?, ?> sessionMetadata) {
        if (Objects.requireNonNull(sessionMetadata.get(SessionReplayConstants.SESSION_REPLAY_BUS_MESSAGE_TYPE_KEY))
                .equals(SessionReplayConstants.RUM_SESSION_RENEWED_BUS_MESSAGE)) {
            checkStatusAndApplySample(sessionMetadata);
        } else {
            sdkCore.getInternalLogger().w(TAG, String.format(Locale.US, UNKNOWN_EVENT_TYPE_PROPERTY_VALUE, sessionMetadata.get(SessionReplayConstants.SESSION_REPLAY_BUS_MESSAGE_TYPE_KEY)));
        }
    }

    @SuppressWarnings("ReturnCount")
    private void checkStatusAndApplySample(Map<?, ?> sessionMetadata) {
        String collectType = (String) sessionMetadata.get(SessionReplayConstants.RUM_KEEP_SESSION_BUS_COLLECT_TYPE_KEY);
        String sessionId = (String) sessionMetadata.get(SessionReplayConstants.RUM_SESSION_ID_BUS_MESSAGE_KEY);

        if (collectType == null || sessionId == null) {
            sdkCore.getInternalLogger().w(TAG, EVENT_MISSING_MANDATORY_FIELDS);
            return;
        }

        if (currentRumSessionId.get() != null && currentRumSessionId.get().equals(sessionId)) {
            return; // we already handled this session
        }

        if (!checkIfInitialized()) {
            return;
        }

        boolean sessionSampled = sessionRelaySampler.sample();
        boolean sessionErrorSampled = !sessionSampled && sessionRelayErrorSampler.sample();
        if ("collect_by_sample".equals(collectType) && (sessionSampled || sessionErrorSampled)) {
            if (sessionErrorSampled) {
                sdkCore.setConsentProvider(TrackingConsent.SAMPLED_ON_ERROR_SESSION);
            } else {
                sdkCore.setConsentProvider(TrackingConsent.GRANTED);
            }
            startRecording(sessionErrorSampled);
        } else if ("collect_by_error_sample".equals(collectType) && (sessionSampled || sessionErrorSampled)) {
            sdkCore.setConsentProvider(TrackingConsent.SAMPLED_ON_ERROR_SESSION);
            startRecording(true);
        } else {
            sdkCore.getInternalLogger().w(TAG, SESSION_SAMPLED_OUT_MESSAGE);
            stopRecording();
        }
        currentRumSessionId.set(sessionId);
    }

    private boolean checkIfInitialized() {
        if (!initialized.get()) {
            sdkCore.getInternalLogger().w(TAG, CANNOT_START_RECORDING_NOT_INITIALIZED);
            return false;
        }
        return true;
    }

    /**
     * Resumes the replay recorder.
     */
    void startRecording(boolean isErrorSampled) {
        // Check initialization again so we don't forget to do it when this method is made public
        if (checkIfInitialized() && !isRecording.getAndSet(true)) {
            sdkCore.getInternalLogger().d(TAG, "start record");
            sdkCore.updateFeatureContext(getName(), new SessionReplayRecordCallback.UpdateCallBack() {

                @Override
                public void onUpdate(Map<String, Object> context) {
                    context.put(SESSION_REPLAY_ENABLED_KEY, true);
                    context.put(SESSION_REPLAY_ENABLED_ON_ERROR_KEY, isErrorSampled);
                }
            });
            sessionReplayRecorder.resumeRecorders();
        }
    }

    private RecordWriter createDataWriter() {
        SessionReplayRecordCallback recordCallback = new SessionReplayRecordCallback(sdkCore);
        return new SessionReplayRecordWriter(sdkCore, recordCallback);
    }

    /**
     * Stops the replay recorder.
     */
    void stopRecording() {
        if (isRecording.getAndSet(false)) {
            sdkCore.getInternalLogger().d(TAG, "stopRecording");
            sdkCore.updateFeatureContext(getName(),
                    new SessionReplayRecordCallback.UpdateCallBack() {
                        @Override
                        public void onUpdate(Map<String, Object> context) {
                            context.put(SESSION_REPLAY_ENABLED_KEY, false);
                        }
                    }
            );
            sessionReplayRecorder.stopRecorders();
        }
    }

    public boolean isRecording() {
        return isRecording.get();
    }

    private ResourcesFeature registerResourceFeature(FeatureSdkCore sdkCore) {
        ResourcesFeature resourcesFeature = new ResourcesFeature(sdkCore, customEndpointUrl);
        sdkCore.registerFeature(resourcesFeature);
        return resourcesFeature;
    }


}