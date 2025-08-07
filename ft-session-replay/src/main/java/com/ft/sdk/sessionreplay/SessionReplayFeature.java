package com.ft.sdk.sessionreplay;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ft.sdk.sessionreplay.internal.SessionReplayRumContextProvider;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;
import com.ft.sdk.sessionreplay.internal.async.SnapshotRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.async.TouchEventRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.processor.MutationResolver;
import com.ft.sdk.sessionreplay.internal.processor.RecordedDataProcessor;
import com.ft.sdk.sessionreplay.internal.processor.RecordedQueuedItemContext;
import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.internal.recorder.SessionReplayRecorder;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.model.PointerInteractionData;
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

import java.util.ArrayList;
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
    /*
    public void processExternalFullSnapshot(MobileRecord.MobileFullSnapshotRecord mobileRecord,
                                           SessionReplayRumContext rumContext) {
        if (!checkIfInitialized()) {
            return;
        }

        // 直接调用 processor 处理记录
        processor.handleExternalFullSnapshot(mobileRecord, rumContext);
    }
     */

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
     * 创建一个RUM上下文，用于外部调用 added by zzq
     * @param viewId 视图ID
     * @return SessionReplayRumContext实例
     */

    public SessionReplayRumContext createRumContextWithJustViewId(String viewId) {

        if (rumContextProvider == null) {
            return new SessionReplayRumContext(); // 返回一个空的上下文
        }

        Log.d(TAG, "zzq createRumContextWithJustViewId appid " + rumContextProvider.getRumContext().getApplicationId());
        Log.d(TAG, "zzq createRumContextWithJustViewId SessionId" + rumContextProvider.getRumContext().getSessionId());
        Log.d(TAG, "zzq createRumContextWithJustViewId viewId" + viewId);
        return new SessionReplayRumContext(
                rumContextProvider.getRumContext().getApplicationId(),
                rumContextProvider.getRumContext().getSessionId(),
                viewId
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
     * 处理外部提供的Node树数据，通过RecordedDataQueueHandler的队列机制, added by zzq
     * @param rootNode Node树的根节点
     * @param systemInformation 系统信息
     * @param viewId 视图ID
     */
    public void processExternalNodeTree(Node rootNode,
                                        SystemInformation systemInformation, String viewId) {
        if (!checkIfInitialized()) {
            return;
        }

        try {
            // 获取RecordedDataQueueHandler
            RecordedDataQueueHandler recordedDataQueueHandler = sessionReplayRecorder.getRecordedDataQueueHandler();
            if (recordedDataQueueHandler == null) {
                Log.d(TAG, "zzq RecordedDataQueueHandler is null");
                return;
            }

            // 通过RecordedDataQueueHandler创建SnapshotRecordedDataQueueItem
            SnapshotRecordedDataQueueItem snapshotItem = recordedDataQueueHandler.addSnapshotItem(systemInformation);
            if (snapshotItem == null) {
                Log.d(TAG, "zzq Failed to create SnapshotRecordedDataQueueItem");
                return;
            }

            // 创建自定义的RumContext并更新
            SessionReplayRumContext customRumContext = createRumContextWithJustViewId(viewId);
            
            // 由于RecordedQueuedItemContext是final的，我们需要通过反射或其他方式更新RumContext
            // 这里我们先设置Node数据，让队列正常处理，在processor中再处理viewId
            List<Node> nodes = java.util.Collections.singletonList(rootNode);
            snapshotItem.setNodes(nodes);
            snapshotItem.setFinishedTraversal(true);

            // 标记这是外部Node，并存储自定义viewId
            // 我们可以通过Node的metadata来传递viewId信息
            if (rootNode != null && rootNode.getMetadata() != null) {
                rootNode.getMetadata().put("external_view_id", viewId);
                rootNode.getMetadata().put("is_external_node", true);
                
                Log.d(TAG, "========== Setting External Node Metadata ==========");
                Log.d(TAG, "zzq Setting external_view_id: " + viewId);
                Log.d(TAG, "zzq Setting is_external_node: true");
                Log.d(TAG, "=================================================");
            }

            // 检查item是否准备好并触发队列处理
            if (snapshotItem.isReady()) {
                recordedDataQueueHandler.tryToConsumeItems();
            }

            Log.d(TAG, "zzq Successfully queued external Node tree with viewId: " + viewId);
        } catch (Exception e) {
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
            // 获取RecordedDataQueueHandler
            RecordedDataQueueHandler recordedDataQueueHandler = sessionReplayRecorder.getRecordedDataQueueHandler();
            if (recordedDataQueueHandler == null) {
                Log.d(TAG, "zzq RecordedDataQueueHandler is null");
                return;
            }

            // 通过RecordedDataQueueHandler创建SnapshotRecordedDataQueueItem
            SnapshotRecordedDataQueueItem snapshotItem = recordedDataQueueHandler.addSnapshotItem(systemInformation);
            if (snapshotItem == null) {
                Log.d(TAG, "zzq Failed to create SnapshotRecordedDataQueueItem");
                return;
            }

            // 设置Node数据
            List<Node> nodes = java.util.Collections.singletonList(rootNode);
            snapshotItem.setNodes(nodes);
            snapshotItem.setFinishedTraversal(true);

            // 标记这是外部Node，并存储自定义RumContext信息
            if (rootNode != null && rootNode.getMetadata() != null) {
                rootNode.getMetadata().put("external_view_id", rumContext.getViewId());
                rootNode.getMetadata().put("external_application_id", rumContext.getApplicationId());
                rootNode.getMetadata().put("external_session_id", rumContext.getSessionId());
                rootNode.getMetadata().put("is_external_node", true);
                
                Log.d(TAG, "========== Setting External Node Metadata (WithRumContext) ==========");
                Log.d(TAG, "zzq Setting external_view_id: " + rumContext.getViewId());
                Log.d(TAG, "zzq Setting external_application_id: " + rumContext.getApplicationId());
                Log.d(TAG, "zzq Setting external_session_id: " + rumContext.getSessionId());
                Log.d(TAG, "zzq Setting is_external_node: true");
                Log.d(TAG, "================================================================");
            }

            // 检查item是否准备好并触发队列处理
            if (snapshotItem.isReady()) {
                recordedDataQueueHandler.tryToConsumeItems();
            }

            Log.d(TAG, "zzq Successfully queued external Node tree with custom RumContext");
        } catch (Exception e) {
            Log.d(TAG, "zzq Error processing external Node tree with RumContext: " + e.getMessage());
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

            // 获取RecordedDataQueueHandler
            RecordedDataQueueHandler recordedDataQueueHandler = sessionReplayRecorder.getRecordedDataQueueHandler();
            if (recordedDataQueueHandler == null) {
                Log.d(TAG, "zzq RecordedDataQueueHandler is null");
                return;
            }

            // 通过RecordedDataQueueHandler创建SnapshotRecordedDataQueueItem
            SnapshotRecordedDataQueueItem snapshotItem = recordedDataQueueHandler.addSnapshotItem(systemInformation);
            if (snapshotItem == null) {
                Log.d(TAG, "zzq Failed to create SnapshotRecordedDataQueueItem");
                return;
            }

            // 设置Node数据
            List<Node> nodes = java.util.Collections.singletonList(rootNode);
            snapshotItem.setNodes(nodes);
            snapshotItem.setFinishedTraversal(true);

            // 标记这是外部Node，并存储自定义RumContext信息
            if (rootNode != null && rootNode.getMetadata() != null) {
                rootNode.getMetadata().put("external_view_id", rumContext.getViewId());
                rootNode.getMetadata().put("external_application_id", rumContext.getApplicationId());
                rootNode.getMetadata().put("external_session_id", rumContext.getSessionId());
                rootNode.getMetadata().put("is_external_node", true);
                rootNode.getMetadata().put("is_new_page", isNewPage);
                
                Log.d(TAG, "========== Setting External Node Metadata (AutoDetection) ==========");
                Log.d(TAG, "zzq Setting external_view_id: " + rumContext.getViewId());
                Log.d(TAG, "zzq Setting external_application_id: " + rumContext.getApplicationId());
                Log.d(TAG, "zzq Setting external_session_id: " + rumContext.getSessionId());
                Log.d(TAG, "zzq Setting is_external_node: true");
                Log.d(TAG, "zzq Setting is_new_page: " + isNewPage);
                Log.d(TAG, "===================================================================");
            }

            // 检查item是否准备好并触发队列处理
            if (snapshotItem.isReady()) {
                recordedDataQueueHandler.tryToConsumeItems();
            }

            Log.d(TAG, "zzq Successfully queued external Node tree (isNewPage: " + isNewPage + ", rootNodeId: " + rootNodeId + ")");
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

            // 获取RecordedDataQueueHandler
            RecordedDataQueueHandler recordedDataQueueHandler = sessionReplayRecorder.getRecordedDataQueueHandler();
            if (recordedDataQueueHandler == null) {
                Log.d(TAG, "zzq RecordedDataQueueHandler is null");
                return;
            }

            // 通过RecordedDataQueueHandler创建SnapshotRecordedDataQueueItem
            SnapshotRecordedDataQueueItem snapshotItem = recordedDataQueueHandler.addSnapshotItem(systemInformation);
            if (snapshotItem == null) {
                Log.d(TAG, "zzq Failed to create SnapshotRecordedDataQueueItem");
                return;
            }

            // 设置Node数据
            List<Node> nodes = java.util.Collections.singletonList(rootNode);
            snapshotItem.setNodes(nodes);
            snapshotItem.setFinishedTraversal(true);

            // 标记这是外部Node，并存储自定义RumContext信息
            if (rootNode != null && rootNode.getMetadata() != null) {
                rootNode.getMetadata().put("external_view_id", rumContext.getViewId());
                rootNode.getMetadata().put("external_application_id", rumContext.getApplicationId());
                rootNode.getMetadata().put("external_session_id", rumContext.getSessionId());
                rootNode.getMetadata().put("is_external_node", true);
                rootNode.getMetadata().put("is_new_page", isNewPage);
                
                Log.d(TAG, "========== Setting External Node Metadata (PageFlag) ==========");
                Log.d(TAG, "zzq Setting external_view_id: " + rumContext.getViewId());
                Log.d(TAG, "zzq Setting external_application_id: " + rumContext.getApplicationId());
                Log.d(TAG, "zzq Setting external_session_id: " + rumContext.getSessionId());
                Log.d(TAG, "zzq Setting is_external_node: true");
                Log.d(TAG, "zzq Setting is_new_page: " + isNewPage);
                Log.d(TAG, "zzq rootNodeId: " + rootNodeId);
                Log.d(TAG, "=============================================================");
            }

            // 检查item是否准备好并触发队列处理
            if (snapshotItem.isReady()) {
                recordedDataQueueHandler.tryToConsumeItems();
            }

            Log.d(TAG, "zzq Successfully queued external Node tree (isNewPage: " + isNewPage + ", rootNodeId: " + rootNodeId + ")");
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

    // Flutter触摸事件缓冲区，类似RecorderWindowCallback.pointerInteractions
    private final List<MobileRecord> flutterPointerInteractions = new ArrayList<>();
    
    /**
     * 添加Flutter触摸事件到缓冲区，类似RecorderWindowCallback.updatePositions
     * Added by zzq for Flutter touch event processing
     * 
     * @param mobileRecord Flutter触摸事件的MobileRecord
     */
    public void addFlutterTouchEvent(MobileRecord.MobileIncrementalSnapshotRecord mobileRecord) {

        Log.d(TAG, "addFlutterTouchEvent touchData step 2" );
        if (!checkIfInitialized()) {
            Log.d(TAG, "SessionReplay not initialized, skipping Flutter touch event touchData  ");
            return;
        }

        // 🔥 修改：Flutter触摸事件不检查录制状态，因为页面切换时录制状态会变化
        // 但Flutter的触摸事件仍然需要被处理
        // 注释掉录制状态检查，确保Flutter触摸事件始终被处理
        /*
        if (!isRecording.get()) {
            Log.d(TAG, "SessionReplay not recording, skipping Flutter touch event touchData  ");
            return;
        }
        */

        synchronized (flutterPointerInteractions) {
            flutterPointerInteractions.add(mobileRecord);
            
            Log.d(TAG, "🔥 Added Flutter touch event to buffer - eventType: touchData  " +
                (mobileRecord.data instanceof PointerInteractionData ? 
                    ((PointerInteractionData) mobileRecord.data).pointerEventType.toString() : "unknown") +
                ", buffer size: " + flutterPointerInteractions.size());
        }
    }

    /**
     * 刷新Flutter触摸事件缓冲区，类似RecorderWindowCallback.flushPositions
     * 最终调用RecordedDataQueueHandler.addTouchEventItem
     * Added by zzq for Flutter touch event processing
     */
    public void flushFlutterTouchEvents() {

        Log.d(TAG, "flushFlutterTouchEvents touchData step 3" );
        if (!checkIfInitialized()) {
            Log.d(TAG, "SessionReplay not initialized, skipping Flutter touch flush, touchData");
            return;
        }

        synchronized (flutterPointerInteractions) {
            if (flutterPointerInteractions.isEmpty()) {
                Log.d(TAG, "Flutter touch buffer is empty, nothing to flush ,touchData ");
                return;
            }

            try {
                // 获取RecordedDataQueueHandler - 完全按照RecorderWindowCallback的方式
                RecordedDataQueueHandler recordedDataQueueHandler = sessionReplayRecorder.getRecordedDataQueueHandler();
                if (recordedDataQueueHandler == null) {
                    Log.d(TAG, "RecordedDataQueueHandler is null, cannot flush Flutter touch events touchData ");
                    return;
                }

                Log.d(TAG, "🚀 Flushing Flutter touch events buffer touchData - count: " + flutterPointerInteractions.size());

                // 调用RecordedDataQueueHandler.addTouchEventItem - 完全按照RecorderWindowCallback.flushPositions()的逻辑
                TouchEventRecordedDataQueueItem item = recordedDataQueueHandler.addTouchEventItem(
                        new ArrayList<>(flutterPointerInteractions)
                );
                
                if (item != null && item.isReady()) {
                    // 触发队列处理，完全按照RecorderWindowCallback.flushPositions()
                    recordedDataQueueHandler.tryToConsumeItems();
                    Log.d(TAG, "✅touchData  Flutter touch events flushed and queued successfully");
                } else {
                    Log.d(TAG, "❌ touchData Failed to create TouchEventRecordedDataQueueItem for Flutter touch events");
                }

                // 清空缓冲区，完全按照RecorderWindowCallback.flushPositions()
                flutterPointerInteractions.clear();

            } catch (Exception e) {
                Log.e(TAG, "Error flushing Flutter touchData  touch events: touchData " + e.getMessage(), e);
                // 即使出错也要清空缓冲区，避免内存泄漏
                flutterPointerInteractions.clear();
            }
        }
    }

    /**
     * 处理单个Flutter触摸事件 - 组合了add和flush的操作
     * Added by zzq for convenience
     * 
     * @param mobileRecord Flutter触摸事件的MobileRecord
     * @param rumContext RUM上下文
     */
    public void processFlutterTouchEvent(MobileRecord.MobileIncrementalSnapshotRecord mobileRecord,
                                        SessionReplayRumContext rumContext) {

        Log.d(TAG, "processFlutterTouchEvent touchData step 1" );
        // 添加到缓冲区
        addFlutterTouchEvent(mobileRecord);
        
        // 根据触摸事件类型决定是否立即刷新，模拟RecorderWindowCallback的逻辑
        if (mobileRecord.data instanceof PointerInteractionData) {
            PointerInteractionData pointerData = (PointerInteractionData) mobileRecord.data;
            String eventType = pointerData.pointerEventType.toString().toLowerCase();
            
            // ACTION_UP时立即刷新，模拟RecorderWindowCallback.handleEvent()中的逻辑
            if ("up".equals(eventType)) {
                flushFlutterTouchEvents();
            }
            // ACTION_DOWN时重置（这里不需要特殊处理，因为我们没有时间阈值逻辑）
            // ACTION_MOVE时可以选择性刷新（这里简化处理，每次move都刷新）
            else if ("move".equals(eventType)) {
                flushFlutterTouchEvents();
            }
        }
     }

    /**
     * 手动刷新Flutter触摸事件缓冲区的公开方法
     * Added by zzq for manual flush control
     */
    public void manualFlushFlutterTouchEvents() {
        flushFlutterTouchEvents();
    }

    /**
     * 获取当前Flutter触摸事件缓冲区大小
     * Added by zzq for debugging
     */
    public int getFlutterTouchBufferSize() {
        synchronized (flutterPointerInteractions) {
            return flutterPointerInteractions.size();
        }
    }

}
