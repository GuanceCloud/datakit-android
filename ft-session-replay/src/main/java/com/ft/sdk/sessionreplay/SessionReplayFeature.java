package com.ft.sdk.sessionreplay;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ft.sdk.sessionreplay.internal.SessionReplayRumContextProvider;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;
import com.ft.sdk.sessionreplay.internal.async.SnapshotRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.processor.MutationResolver;
import com.ft.sdk.sessionreplay.internal.processor.RecordedDataProcessor;
import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;
import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.internal.recorder.SessionReplayRecorder;
import com.ft.sdk.sessionreplay.model.MobileRecord;
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
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;
import com.ft.sdk.sessionreplay.internal.resources.ResourceHashesEntryDeserializer;
import com.ft.sdk.sessionreplay.internal.resources.ResourceHashesEntrySerializer;
import com.ft.sdk.sessionreplay.internal.storage.NoOpRecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.SessionReplayRecordWriter;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.RumContextProvider;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
    private RecordedDataQueueHandler recordedDataQueueHandler;

    private final AtomicReference<String> currentRumSessionId = new AtomicReference<>();

    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    Recorder
            sessionReplayRecorder = new NoOpRecorder();
    RecordWriter
            dataWriter = new NoOpRecordWriter();
    final AtomicBoolean initialized = new AtomicBoolean(false);
    private RecordedDataProcessor processor; //added by zzq
    private RumContextProvider rumContextProvider; //added by zzq

    // 用于跟踪Flutter页面的映射：rootNode.id -> viewId, added by zzq
    private final Map<Long, String> flutterPageViewIdMap = new ConcurrentHashMap<>();

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
                config.getInternalCallback());

    }

    public SessionReplayFeature(FeatureSdkCore sdkCore, String customEndpointUrl,
                                TextAndInputPrivacy textAndInputPrivacy,
                                TouchPrivacy touchPrivacy,
                                ImagePrivacy imagePrivacy,
                                Sampler sessionReplaySampler,
                                Sampler sessionReplayErrorSampler,
                                RecorderProvider recorderProvider) {
        this.sdkCore = sdkCore;
        this.customEndpointUrl = customEndpointUrl;
        this.textAndInputPrivacy = textAndInputPrivacy;
        this.touchPrivacy = touchPrivacy;
        this.imagePrivacy = imagePrivacy;
        this.sessionRelaySampler = sessionReplaySampler;
        this.sessionRelayErrorSampler = sessionReplayErrorSampler;
        this.recorderProvider = recorderProvider;
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
                                SessionReplayInternalCallback internalCallback) {
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
        recordedDataQueueHandler = ((SessionReplayRecorder) sessionReplayRecorder).getRecordedDataQueueHandler(); //added by zzq
        rumContextProvider = new SessionReplayRumContextProvider(sdkCore);//added by zzq
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
        // 初始化 processor zzq
        this.processor = new RecordedDataProcessor(
                sdkCore,
                resourceDataStoreManager,
                resourcesFeature.getDataWriter(),
                dataWriter,
                new MutationResolver(sdkCore.getInternalLogger())
        );

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
    public void startRecording(boolean isErrorSampled) {
        // Check initialization again so we don't forget to do it when this method is made public
        if (checkIfInitialized() && !isRecording.getAndSet(true)) {
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
   public void stopRecording() {
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

    /**
     * 处理外部提供的完整屏幕快照, added by zzq
     * @param mobileRecord 完整屏幕快照记录
     * @param rumContext RUM上下文
     */
    public void processExternalFullSnapshot(MobileRecord.MobileFullSnapshotRecord mobileRecord,
                                           SessionReplayRumContext rumContext) {
        if (!checkIfInitialized()) {
            return;
        }

        // 直接调用 processor 处理记录
        processor.handleExternalFullSnapshot(mobileRecord, rumContext);
    }

    /**
     * 处理外部提供的增量更新 , added by zzq
     * @param mobileRecord 增量更新记录
     * @param rumContext RUM上下文
     */
    public void processExternalIncrementalUpdate(MobileRecord.MobileIncrementalSnapshotRecord mobileRecord,
                                               SessionReplayRumContext rumContext) {
        if (!checkIfInitialized()) {
            return;
        }

        // 直接调用 processor 处理增量更新
        processor.handleExternalIncrementalUpdate(mobileRecord, rumContext);
    }

    /**
     * 创建一个空的RUM上下文，用于外部调用 added by zzq
     * @return SessionReplayRumContext实例
     */
    public SessionReplayRumContext createEmptyRumContext() {

        if (rumContextProvider == null) {
            return new SessionReplayRumContext(); // 返回一个空的上下文
        }
        return new SessionReplayRumContext(
                rumContextProvider.getRumContext().getApplicationId(),
                rumContextProvider.getRumContext().getSessionId(),
                rumContextProvider.getRumContext().getViewId()
        );
    }

    /**
     * 配置是否使用Flutter提供的UI数据, added by zzq
     * @param useFlutterUIData 是否使用Flutter UI数据
     */
    public void setUseFlutterUIData(boolean useFlutterUIData) {
        if (recorderProvider instanceof DefaultRecorderProvider) {
            ((DefaultRecorderProvider) recorderProvider).setUseFlutterUIData(useFlutterUIData);
        }
    }
    /**
     * 处理外部提供的Node树数据，通过processor的processScreenSnapshots方法, added by zzq
     * @param rootNode Node树的根节点
     * @param systemInformation 系统信息
     */
    public void processExternalNodeTree(Node rootNode,
                                        SystemInformation systemInformation) {
        if (!checkIfInitialized()) {
            return;
        }

        try {
            // 创建RumContext
            SessionReplayRumContext rumContext = createEmptyRumContext();

            // 创建RecordedQueuedItemContext
            RecordedQueuedItemContext recordedQueuedItemContext =
                    new RecordedQueuedItemContext(
                            System.currentTimeMillis(),
                            rumContext
                    );

            // 创建SnapshotRecordedDataQueueItem
            SnapshotRecordedDataQueueItem snapshotItem =
                    new SnapshotRecordedDataQueueItem(
                            recordedQueuedItemContext,
                            systemInformation
                    );

            // 设置Node列表
            List<Node> nodes =
                    java.util.Collections.singletonList(rootNode);
            snapshotItem.setNodes(nodes);
            snapshotItem.setFinishedTraversal(true);

            // 通过processor处理
            processor.processScreenSnapshots(snapshotItem);
            //sdkCore.getInternalLogger().w(TAG, "Successfully processed external Node tree");
            Log.d(TAG, "zzq Successfully processed external Node tree");
        } catch (Exception e) {
            //sdkCore.getInternalLogger().w(TAG, "Error processing external Node tree: " + e.getMessage());
            Log.d(TAG, "zzq Error processing external Node tree: " + e.getMessage());
        }
    }

    /**
     * 处理外部提供的Node树数据，使用指定的RUM上下文, added by zzq
     * @param rootNode Node树的根节点
     * @param systemInformation 系统信息
     * @param rumContext 指定的RUM上下文（包含新的viewId）
     */
    public void processExternalNodeTreeWithRumContext(Node rootNode,
                                                      SystemInformation systemInformation,
                                                      SessionReplayRumContext rumContext) {
        if (!checkIfInitialized()) {
            return;
        }

        try {
            // 使用传入的RumContext
            RecordedQueuedItemContext recordedQueuedItemContext =
                    new RecordedQueuedItemContext(
                            System.currentTimeMillis(),
                            rumContext
                    );

            // 创建SnapshotRecordedDataQueueItem
            SnapshotRecordedDataQueueItem snapshotItem =
                    new SnapshotRecordedDataQueueItem(
                            recordedQueuedItemContext,
                            systemInformation
                    );

            // 设置Node列表
            List<Node> nodes =
                    java.util.Collections.singletonList(rootNode);
            snapshotItem.setNodes(nodes);
            snapshotItem.setFinishedTraversal(true);

            // 通过processor处理
            processor.processScreenSnapshots(snapshotItem);
            Log.d(TAG, "zzq Successfully processed external Node tree with RUM context: " + rumContext.getViewId());
        } catch (Exception e) {
            Log.d(TAG, "zzq Error processing external Node tree with RUM context: " + e.getMessage());
        }
    }

    /**
     * 处理外部提供的Node树数据，自动检测新页面（用于Flutter）, added by zzq
     * @param rootNode Node树的根节点
     * @param systemInformation 系统信息
     */
    public void processExternalNodeTreeWithAutoDetection(Node rootNode,
                                                         SystemInformation systemInformation) {
        if (!checkIfInitialized()) {
            return;
        }

        try {
            long rootNodeId = 0;
            if(rootNode.getWireframes() != null && !rootNode.getWireframes().isEmpty()) {
                rootNodeId = rootNode.getWireframes().get(0).getId();
            }
            SessionReplayRumContext rumContext;
            boolean isNewPage = false;

            // 检查是否是已知的页面
            String existingViewId = flutterPageViewIdMap.get(rootNodeId);

            if (existingViewId == null) {
                // 新的rootNode.id，说明是新页面
                isNewPage = true;
                String newViewId = generateNewViewId();

                // 记录映射关系
                flutterPageViewIdMap.put(rootNodeId, newViewId);

                // 获取当前的applicationId和sessionId
                SessionReplayRumContext currentContext = createEmptyRumContext();

                // 创建新的RUM上下文
                rumContext = new SessionReplayRumContext(
                    currentContext.getApplicationId(),
                    currentContext.getSessionId(),
                    newViewId
                );

                Log.d(TAG, "zzq Detected new page, generated viewId: " + newViewId + " for rootNodeId: " + rootNodeId);
            } else {
                // 已知的rootNode.id，说明是页面更新
                // 获取当前的applicationId和sessionId，但使用已存在的viewId
                SessionReplayRumContext currentContext = createEmptyRumContext();

                rumContext = new SessionReplayRumContext(
                    currentContext.getApplicationId(),
                    currentContext.getSessionId(),
                    existingViewId  // 使用已存在的viewId
                );

                Log.d(TAG, "zzq Detected page update, using existing viewId: " + existingViewId + " for rootNodeId: " + rootNodeId);
            }

            // 创建RecordedQueuedItemContext
            RecordedQueuedItemContext recordedQueuedItemContext =
                    new RecordedQueuedItemContext(
                            System.currentTimeMillis(),
                            rumContext
                    );

            // 创建SnapshotRecordedDataQueueItem
            SnapshotRecordedDataQueueItem snapshotItem =
                    new SnapshotRecordedDataQueueItem(
                            recordedQueuedItemContext,
                            systemInformation
                    );

            // 设置Node列表
            List<Node> nodes = java.util.Collections.singletonList(rootNode);
            snapshotItem.setNodes(nodes);
            snapshotItem.setFinishedTraversal(true);

            // 通过processor处理
            processor.processScreenSnapshots(snapshotItem);

            Log.d(TAG, "zzq Successfully processed external Node tree (isNewPage: " + isNewPage + ", rootNodeId: " + rootNodeId + ")");
        } catch (Exception e) {
            Log.d(TAG, "zzq Error processing external Node tree with auto detection: " + e.getMessage());
        }
    }

    /**
     * 处理外部提供的Node树数据，支持新页面检测（用于Flutter）, added by zzq
     * @param rootNode Node树的根节点
     * @param systemInformation 系统信息
     * @param isNewPage 是否是新页面（由Flutter端判断）
     */
    public void processExternalNodeTreeWithPageFlag(Node rootNode,
                                                    SystemInformation systemInformation,
                                                    boolean isNewPage) {
        if (!checkIfInitialized()) {
            return;
        }

        try {
            long rootNodeId = 0;
            if(rootNode.getWireframes() != null && !rootNode.getWireframes().isEmpty()) {
                rootNodeId = rootNode.getWireframes().get(0).getId();
            }
            SessionReplayRumContext rumContext;

            if (isNewPage) {
                // 是新页面，生成新的viewId并创建新的RUM上下文
                String newViewId = generateNewViewId();

                // 记录映射关系
                flutterPageViewIdMap.put(rootNodeId, newViewId);

                // 获取当前的applicationId和sessionId
                SessionReplayRumContext currentContext = createEmptyRumContext();

                // 创建新的RUM上下文
                rumContext = new SessionReplayRumContext(
                    currentContext.getApplicationId(),
                    currentContext.getSessionId(),
                    newViewId
                );

                Log.d(TAG, "zzq Generated new viewId for new page: " + newViewId + " for rootNodeId: " + rootNodeId);
            } else {
                // 不是新页面，使用已存在的viewId或当前的RUM上下文
                String existingViewId = flutterPageViewIdMap.get(rootNodeId);

                if (existingViewId != null) {
                    // 使用已存在的viewId
                    SessionReplayRumContext currentContext = createEmptyRumContext();
                    rumContext = new SessionReplayRumContext(
                        currentContext.getApplicationId(),
                        currentContext.getSessionId(),
                        existingViewId
                    );
                    Log.d(TAG, "zzq Using mapped viewId for page update: " + existingViewId + " for rootNodeId: " + rootNodeId);
                } else {
                    // 使用现有的RUM上下文
                    rumContext = createEmptyRumContext();
                    Log.d(TAG, "zzq Using current viewId for page update: " + rumContext.getViewId() + " for rootNodeId: " + rootNodeId);
                }
            }

            // 创建RecordedQueuedItemContext
            RecordedQueuedItemContext recordedQueuedItemContext =
                    new RecordedQueuedItemContext(
                            System.currentTimeMillis(),
                            rumContext
                    );

            // 创建SnapshotRecordedDataQueueItem
            SnapshotRecordedDataQueueItem snapshotItem =
                    new SnapshotRecordedDataQueueItem(
                            recordedQueuedItemContext,
                            systemInformation
                    );

            // 设置Node列表
            List<Node> nodes = java.util.Collections.singletonList(rootNode);
            snapshotItem.setNodes(nodes);
            snapshotItem.setFinishedTraversal(true);

            // 通过processor处理
            processor.processScreenSnapshots(snapshotItem);

            Log.d(TAG, "zzq Successfully processed external Node tree (isNewPage: " + isNewPage + ", rootNodeId: " + rootNodeId + ")");
        } catch (Exception e) {
            Log.d(TAG, "zzq Error processing external Node tree with page flag: " + e.getMessage());
        }
    }

    /**
     * 生成新的viewId, added by zzq
     * @return 新的viewId
     */
    private String generateNewViewId() {
        // 使用与ViewBean相同的生成方式
        java.util.UUID uuid = java.util.UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    /**
     * 清理Flutter页面映射关系, added by zzq
     */
    public void clearFlutterPageMappings() {
        flutterPageViewIdMap.clear();
        Log.d(TAG, "zzq Cleared Flutter page mappings");
    }

    /**
     * 移除特定的Flutter页面映射, added by zzq
     * @param rootNodeId 要移除的rootNode ID
     */
    public void removeFlutterPageMapping(long rootNodeId) {
        String removedViewId = flutterPageViewIdMap.remove(rootNodeId);
        if (removedViewId != null) {
            Log.d(TAG, "zzq Removed Flutter page mapping: rootNodeId=" + rootNodeId + ", viewId=" + removedViewId);
        }
    }

    /**
     * 创建一个新的RUM上下文，包含指定的viewId, added by zzq
     * @param applicationId 应用ID
     * @param sessionId 会话ID
     * @param viewId 视图ID
     * @return SessionReplayRumContext实例
     */
    public SessionReplayRumContext createRumContextWithViewId(String applicationId,
                                                              String sessionId,
                                                              String viewId) {
        return new SessionReplayRumContext(applicationId, sessionId, viewId);
    }

}