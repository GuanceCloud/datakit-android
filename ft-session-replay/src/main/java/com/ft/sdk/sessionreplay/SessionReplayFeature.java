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
import com.ft.sdk.sessionreplay.internal.recorder.NoOpRecorder;
import com.ft.sdk.sessionreplay.internal.recorder.Recorder;
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
            "Session Replay feature received an event where one or more mandatory (keepSession) fields" +
                    " are either missing or have wrong type.";

    public static final String CANNOT_START_RECORDING_NOT_INITIALIZED =
            "Cannot start session recording, because Session Replay feature is not initialized.";

    public static final String SESSION_REPLAY_FEATURE_NAME = "session-replay";

    public static final String SESSION_REPLAY_SAMPLE_RATE_KEY = "session_replay_sample_rate";
    public static final String SESSION_REPLAY_PRIVACY_KEY = "session_replay_privacy";
    public static final String SESSION_REPLAY_TEXT_AND_INPUT_PRIVACY_KEY = "session_replay_text_and_input_privacy";
    public static final String SESSION_REPLAY_IMAGE_PRIVACY_KEY = "session_replay_image_privacy";
    public static final String SESSION_REPLAY_TOUCH_PRIVACY_KEY = "session_replay_touch_privacy";
    public static final String SESSION_REPLAY_MANUAL_RECORDING_KEY =
            "session_replay_requires_manual_recording";
    public static final String SESSION_REPLAY_ENABLED_KEY =
            "session_replay_is_enabled";

    private final FeatureSdkCore sdkCore;
    private final String customEndpointUrl;
    private final SessionReplayPrivacy privacy;
    private final TouchPrivacy touchPrivacy;
    private final TextAndInputPrivacy textAndInputPrivacy;
    private final ImagePrivacy imagePrivacy;
    private final Sampler rateBasedSampler;
    private final RecorderProvider recorderProvider;

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
                config.getPrivacy(),
                config.getTextAndInputPrivacy(),
                config.getTouchPrivacy(),
                new TouchPrivacyManager(config.getTouchPrivacy()),
                config.getImagePrivacy(),
                config.getCustomMappers(),
                config.getCustomOptionSelectorDetectors(),
                config.getCustomDrawableMapper(),
                config.getSampleRate(),
                config.isDelayInit(),
                config.isDynamicOptimizationEnabled(),
                config.getInternalCallback());

    }

    public SessionReplayFeature(FeatureSdkCore sdkCore, String customEndpointUrl,
                                SessionReplayPrivacy privacy,
                                TextAndInputPrivacy textAndInputPrivacy,
                                TouchPrivacy touchPrivacy,
                                ImagePrivacy imagePrivacy,
                                Sampler rateBasedSampler,
                                RecorderProvider recorderProvider) {
        this.sdkCore = sdkCore;
        this.customEndpointUrl = customEndpointUrl;
        this.privacy = privacy;
        this.textAndInputPrivacy = textAndInputPrivacy;
        this.touchPrivacy = touchPrivacy;
        this.imagePrivacy = imagePrivacy;
        this.rateBasedSampler = rateBasedSampler;
        this.recorderProvider = recorderProvider;
    }

    public SessionReplayFeature(FeatureSdkCore sdkCore, String customEndpointUrl,
                                SessionReplayPrivacy privacy,
                                TextAndInputPrivacy textAndInputPrivacy,
                                TouchPrivacy touchPrivacy,
                                TouchPrivacyManager touchPrivacyManager,
                                ImagePrivacy imagePrivacy,
                                List<MapperTypeWrapper<?>> customMappers,
                                List<OptionSelectorDetector> customOptionSelectorDetectors,
                                List<DrawableToColorMapper> customDrawableMappers,
                                float sampleRate,
                                boolean isDelayInit,
                                boolean dynamicOptimizationEnabled,
                                SessionReplayInternalCallback internalCallback) {
        this(sdkCore, customEndpointUrl,
                privacy,
                textAndInputPrivacy,
                touchPrivacy,
                imagePrivacy,
                new RateBasedSampler(sampleRate),
                new DefaultRecorderProvider(sdkCore,
                        textAndInputPrivacy,
                        imagePrivacy,
                        touchPrivacyManager, customMappers,
                        customOptionSelectorDetectors,
                        customDrawableMappers,
                        dynamicOptimizationEnabled,
                        internalCallback,
                        isDelayInit
                ));
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
                context.put(SESSION_REPLAY_SAMPLE_RATE_KEY, rateBasedSampler.getSampleRate() != null ?
                        rateBasedSampler.getSampleRate().longValue() : null);
                context.put(SESSION_REPLAY_PRIVACY_KEY, privacy.toString().toLowerCase(Locale.US));
                context.put(SESSION_REPLAY_PRIVACY_KEY, privacy.toString().toLowerCase(Locale.US));
                context.put(SESSION_REPLAY_PRIVACY_KEY, privacy.toString().toLowerCase(Locale.US));
                context.put(SESSION_REPLAY_PRIVACY_KEY, privacy.toString().toLowerCase(Locale.US));
                context.put(SESSION_REPLAY_MANUAL_RECORDING_KEY, false);
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
        Boolean keepSession = (Boolean) sessionMetadata.get(SessionReplayConstants.RUM_KEEP_SESSION_BUS_MESSAGE_KEY);
        String sessionId = (String) sessionMetadata.get(SessionReplayConstants.RUM_SESSION_ID_BUS_MESSAGE_KEY);

        if (keepSession == null || sessionId == null) {
            sdkCore.getInternalLogger().w(TAG, EVENT_MISSING_MANDATORY_FIELDS);
            return;
        }

        if (currentRumSessionId.get() != null && currentRumSessionId.get().equals(sessionId)) {
            return; // we already handled this session
        }

        if (!checkIfInitialized()) {
            return;
        }

        if (keepSession && rateBasedSampler.sample()) {
            startRecording();
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
    void startRecording() {
        // Check initialization again so we don't forget to do it when this method is made public
        if (checkIfInitialized() && !isRecording.getAndSet(true)) {
            sdkCore.updateFeatureContext(getName(), new SessionReplayRecordCallback.UpdateCallBack() {

                @Override
                public void onUpdate(Map<String, Object> context) {
                    context.put(SESSION_REPLAY_ENABLED_KEY, true);
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

    private ResourcesFeature registerResourceFeature(FeatureSdkCore sdkCore) {
        ResourcesFeature resourcesFeature = new ResourcesFeature(sdkCore, customEndpointUrl);
        sdkCore.registerFeature(resourcesFeature);
        return resourcesFeature;
    }


}