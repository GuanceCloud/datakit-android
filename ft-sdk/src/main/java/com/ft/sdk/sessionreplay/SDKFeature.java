package com.ft.sdk.sessionreplay;

import android.content.Context;
import android.os.BatteryManager;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.SyncTaskManager;
import com.ft.sdk.api.PackageIdProxy;
import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.feature.DataConsumerCallback;
import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureContextUpdateReceiver;
import com.ft.sdk.feature.FeatureEventReceiver;
import com.ft.sdk.feature.FeatureScope;
import com.ft.sdk.garble.bean.BatteryBean;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.threadpool.ThreadPoolFactory;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.ID36Generator;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.PackageIdGenerator;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.garble.utils.VersionUtils;
import com.ft.sdk.sessionreplay.internal.StorageBackedFeature;
import com.ft.sdk.sessionreplay.internal.net.BatchesToSegmentsMapper;
import com.ft.sdk.sessionreplay.internal.persistence.BatchClosedMetadata;
import com.ft.sdk.sessionreplay.internal.persistence.BatchFileOrchestrator;
import com.ft.sdk.sessionreplay.internal.persistence.BatchFileReaderWriterFactory;
import com.ft.sdk.sessionreplay.internal.persistence.BatchProcessingLevel;
import com.ft.sdk.sessionreplay.internal.persistence.DataUploadConfiguration;
import com.ft.sdk.sessionreplay.internal.persistence.EventBatchWriterCallback;
import com.ft.sdk.sessionreplay.internal.persistence.Storage;
import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;
import com.ft.sdk.sessionreplay.internal.persistence.UploadFrequency;
import com.ft.sdk.sessionreplay.internal.storage.ConsentAwareStorage;
import com.ft.sdk.sessionreplay.internal.storage.DataUploadScheduler;
import com.ft.sdk.sessionreplay.internal.storage.FileMover;
import com.ft.sdk.sessionreplay.internal.storage.FilePersistenceConfig;
import com.ft.sdk.sessionreplay.internal.storage.MetricsDispatcher;
import com.ft.sdk.sessionreplay.internal.storage.NoOpStorage;
import com.ft.sdk.sessionreplay.internal.storage.NoOpUploadScheduler;
import com.ft.sdk.sessionreplay.internal.storage.RemovalReason;
import com.ft.sdk.sessionreplay.internal.storage.UploadScheduler;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.storage.DataStoreHandler;
import com.ft.sdk.storage.EventBatchWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class SDKFeature implements FeatureScope {
    private static final String TAG = "SDKFeatureScope";
    private final AtomicReference<FeatureEventReceiver> eventReceiver = new AtomicReference<>(null);
    private final List<FeatureContextUpdateReceiver> contextUpdateListeners = new ArrayList<>();

    private static final String CONTEXT_UPDATE_LISTENER_ALREADY_EXISTS = "Context update listener already exists for feature: %s";
    private static final String NO_EVENT_RECEIVER =
            "Feature \"%s\" has no event receiver registered, ignoring event.";
    private final Feature wrappedFeature;
    private final InternalLogger internalLogger;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private SessionReplayContext sdkContext;
    private Storage storage = new NoOpStorage();
    private final ID36Generator srGenerator = new ID36Generator();

    private final BatteryPowerWatcher watcher = new BatteryPowerWatcher();

    private UploadScheduler uploadScheduler = new NoOpUploadScheduler();

    public SDKFeature(Feature feature, InternalLogger internalLogger) {
        this.wrappedFeature = feature;
        this.internalLogger = internalLogger;
    }

    public void init(Context context, String instanceId) {
        if (initialized.get()) {
            return;
        }

        DataUploadConfiguration dataUploadConfiguration;

        if (wrappedFeature instanceof StorageBackedFeature) {
            String featureName = wrappedFeature.getName();
            storage = createFileStorage(featureName, new FilePersistenceConfig());

            dataUploadConfiguration = new DataUploadConfiguration(UploadFrequency.FREQUENT,
                    BatchProcessingLevel.MEDIUM.getMaxBatchesPerUploadJob());

            String url = HttpBuilder.Builder()
                    .setModel(SessionReplayConstants.URL_MODEL_SESSION_REPLAY).getUrlWithMsPrecision();

            LogUtils.d(TAG, featureName + ":Session Replay Url:" + url);

            FTSDKConfig sdkConfig = FTSdk.get().getBaseConfig();
            FTRUMConfig rumConfig = FTRUMConfigManager.get().getConfig();

            String appId = rumConfig == null ? "" : rumConfig.getRumAppId();

            if (VersionUtils.firstVerGreaterEqual(FTSdk.SESSION_REPLAY_VERSION, "0.1.0-alpha08")) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("requestUrl", url);
                map.put("env", sdkConfig.getEnv());
                map.put("sdkVersion", FTSdk.AGENT_VERSION);
                map.put("trackingConsent", TrackingConsent.GRANTED.toString());
                map.put("appId", appId);
                map.put("userAgent", Constants.USER_AGENT);
                map.put("appVersion", Utils.getAppVersionName());
                sdkContext = SessionReplayContext.createFromMap(map);
            } else {
                sdkContext = new SessionReplayContext(url, sdkConfig.getEnv(),
                        FTSdk.AGENT_VERSION, TrackingConsent.GRANTED, appId);
            }

            setupUploader(wrappedFeature, dataUploadConfiguration);
        }
        wrappedFeature.onInitialize(context);
        initialized.set(true);
        uploadScheduler.startScheduling();
        watcher.register(context);
    }

    private void setupUploader(Feature feature, DataUploadConfiguration configuration) {
        if (Utils.isMainProcess()) {
            SessionReplayUploader uploader;
            if (VersionUtils.firstVerGreaterEqual(FTSdk.SESSION_REPLAY_VERSION, "0.1.0-alpha07")) {
                uploader = new SessionReplayUploader(sdkContext,
                        new BatchesToSegmentsMapper(internalLogger), internalLogger, new PackageIdProxy() {
                    @Override
                    public String getPackageId(long count) {
                        String pkgId = PackageIdGenerator.generatePackageId(srGenerator.getCurrentId()
                                , SyncTaskManager.pid, (int) count);
                        return String.format(Constants.SYNC_DATA_TRACE_HEADER_FORMAT, pkgId);
                    }

                    @Override
                    public void nextSeqId() {
                        srGenerator.next();
                    }
                });
            } else {
                uploader = new SessionReplayUploader(sdkContext,
                        new BatchesToSegmentsMapper(internalLogger), internalLogger);
            }

            if (feature.getName().equals(Feature.SESSION_REPLAY_FEATURE_NAME)) {
                uploadScheduler = new DataUploadScheduler(feature.getName(), internalLogger,
                        configuration, storage, uploader, sdkContext,
                        new SystemInfoProxy() {
                            @Override
                            public boolean isNetworkAvailable() {
                                return NetUtils.isNetworkAvailable();
                            }

                            @Override
                            public boolean isBatteryHealthToSync() {
                                BatteryBean batteryBean = watcher.batteryBean;
                                boolean batteryEnough = batteryBean.getLevel() > SessionReplayConstants.BATTERY_LIMIT;
                                boolean batteryPlug = batteryBean.getPlugState() == BatteryManager.BATTERY_PLUGGED_AC
                                        || batteryBean.getPlugState() == BatteryManager.BATTERY_PLUGGED_WIRELESS
                                        || batteryBean.getPlugState() == BatteryManager.BATTERY_PLUGGED_USB;
                                boolean isFullOrCharging = batteryBean.getBatteryStatue() == BatteryManager.BATTERY_STATUS_FULL ||
                                        batteryBean.getBatteryStatue() == BatteryManager.BATTERY_STATUS_CHARGING;
                                return batteryEnough || batteryPlug || !batteryBean.isBatteryPresent() || isFullOrCharging;
                            }
                        });
            } else {
                uploadScheduler = new NoOpUploadScheduler();
            }

        }
    }

    @Override
    public DataStoreHandler getDataStore() {
        return null;
    }

    @Override
    public void withWriteContext(boolean forceNewBatch, DataConsumerCallback callback) {

        storage.writeCurrentBatch(sdkContext, forceNewBatch, new EventBatchWriterCallback() {
            @Override
            public void callBack(EventBatchWriter writer) {
                callback.onConsume(sdkContext, writer);
            }
        });
    }

    private Storage createFileStorage(String featureName, FilePersistenceConfig filePersistenceConfig) {
        //fixme

        MetricsDispatcher dispatcher = new MetricsDispatcher() {
            @Override
            public void sendBatchDeletedMetric(File batchFile, RemovalReason removalReason) {

            }

            @Override
            public void sendBatchClosedMetric(File batchFile, BatchClosedMetadata batchMetadata) {

            }
        };

        return new ConsentAwareStorage(new ThreadPoolFactory(featureName).getExecutor(),
                new BatchFileOrchestrator(new File(FTApplication.getApplication().getCacheDir(),
                        SessionReplayConstants.PATH_SESSION_REPLAY), new FilePersistenceConfig(),
                        internalLogger, dispatcher),
                null,
                BatchFileReaderWriterFactory.create(internalLogger, null),
                null,
                new FileMover(internalLogger),
                internalLogger,
                filePersistenceConfig,
                dispatcher);
    }

    @Override
    public void sendEvent(Object event) {
        FeatureEventReceiver receiver = eventReceiver.get();
        if (receiver == null) {
            internalLogger.i(TAG, String.format(NO_EVENT_RECEIVER, wrappedFeature.getName()));
        } else {
            receiver.onReceive(event);
        }

    }

    /**
     * @noinspection unchecked
     */
    @Override
    public <T extends Feature> T unwrap() {
        return (T) wrappedFeature;
    }

    public AtomicReference<FeatureEventReceiver> getEventReceiver() {
        return eventReceiver;
    }


    public void notifyContextUpdated(String featureName, Map<String, Object> context) {
        for (FeatureContextUpdateReceiver listener : contextUpdateListeners) {
            listener.onContextUpdate(featureName, context);
        }
    }

    public void setContextUpdateListener(FeatureContextUpdateReceiver listener) {
        synchronized (contextUpdateListeners) {
            if (contextUpdateListeners.contains(listener)) {
                internalLogger.e(TAG, String.format(Locale.US, CONTEXT_UPDATE_LISTENER_ALREADY_EXISTS,
                        wrappedFeature.getName()));
            }
            contextUpdateListeners.add(listener);
        }
    }

    public void removeContextUpdateListener(FeatureContextUpdateReceiver listener) {
        synchronized (contextUpdateListeners) {
            contextUpdateListeners.remove(listener);
        }
    }

    public void stop() {
        if (initialized.get()) {
            wrappedFeature.onStop();

            uploadScheduler.stopScheduling();
            uploadScheduler = new NoOpUploadScheduler();
            storage = new NoOpStorage();
            watcher.unRegister(FTApplication.getApplication());
        }
    }

}
