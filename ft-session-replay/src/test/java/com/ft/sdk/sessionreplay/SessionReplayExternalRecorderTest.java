package com.ft.sdk.sessionreplay;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureContextUpdateReceiver;
import com.ft.sdk.feature.FeatureEventReceiver;
import com.ft.sdk.feature.FeatureScope;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.internal.RecorderProvider;
import com.ft.sdk.sessionreplay.internal.SessionReplayRecordCallback;
import com.ft.sdk.sessionreplay.internal.SessionReplayRumContextProvider;
import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;
import com.ft.sdk.sessionreplay.internal.recorder.Recorder;
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SessionReplayExternalRecorderTest {

    @Test
    public void externalOwners_pauseUntilLastOwnerReleases_thenForceFullSnapshot() {
        TestRecorder recorder = new TestRecorder();
        SessionReplayFeature feature = newFeature(recorder);

        feature.startRecording(false);
        assertEquals(1, recorder.resumeCount);

        feature.setExternalRecorderActive("flutter-engine", true);
        feature.setExternalRecorderActive("flutter-engine", true);
        assertEquals(1, recorder.closeCurrentViewCount);
        assertEquals(1, recorder.stopCount);

        feature.setExternalRecorderActive("unity-player", true);
        feature.setExternalRecorderActive("flutter-engine", false);
        assertEquals(1, recorder.closeCurrentViewCount);
        assertEquals(1, recorder.stopCount);
        assertEquals(1, recorder.resumeCount);

        feature.setExternalRecorderActive("unity-player", false);
        feature.setExternalRecorderActive("unity-player", false);
        assertEquals(1, recorder.forceFullSnapshotCount);
        assertEquals(2, recorder.resumeCount);
    }

    @Test
    public void externalOwnerActiveBeforeSampling_defersNativeRecorderStart() {
        TestRecorder recorder = new TestRecorder();
        SessionReplayFeature feature = newFeature(recorder);

        feature.setExternalRecorderActive(true);
        feature.startRecording(false);
        assertEquals(0, recorder.resumeCount);

        feature.setExternalRecorderActive(false);
        assertEquals(1, recorder.forceFullSnapshotCount);
        assertEquals(1, recorder.resumeCount);
    }

    @Test
    public void rumContextProvider_preservesClosedViewStateDuringRecorderHandoff() {
        TestFeatureSdkCore sdkCore = new TestFeatureSdkCore();
        sdkCore.context.put("application_id", "app-id");
        sdkCore.context.put("session_id", "session-id");
        sdkCore.context.put("view_id", "cocos-view-id");
        sdkCore.context.put("is_active", false);

        assertFalse(new SessionReplayRumContextProvider(sdkCore)
                .getRumContext()
                .isViewActive());

        sdkCore.context.remove("is_active");
        assertTrue(new SessionReplayRumContextProvider(sdkCore)
                .getRumContext()
                .isViewActive());
    }

    private SessionReplayFeature newFeature(TestRecorder recorder) {
        TestFeatureSdkCore sdkCore = new TestFeatureSdkCore();
        SessionReplayFeature feature = new SessionReplayFeature(
                sdkCore,
                null,
                TextAndInputPrivacy.MASK_ALL,
                TouchPrivacy.HIDE,
                ImagePrivacy.MASK_ALL,
                new RateBasedSampler(1f),
                new RateBasedSampler(0f),
                new TestRecorderProvider(recorder),
                new String[]{},
                new SlotIdWebviewBinder(sdkCore.getInternalLogger())
        );
        feature.sessionReplayRecorder = recorder;
        feature.initialized.set(true);
        return feature;
    }

    private static final class TestRecorderProvider implements RecorderProvider {
        private final Recorder recorder;

        private TestRecorderProvider(Recorder recorder) {
            this.recorder = recorder;
        }

        @Override
        public Recorder provideSessionReplayRecorder(
                ResourceDataStoreManager resourceDataStoreManager,
                ResourcesWriter resourceWriter,
                RecordWriter recordWriter,
                Application application
        ) {
            return recorder;
        }
    }

    private static final class TestRecorder implements Recorder {
        private int resumeCount;
        private int stopCount;
        private int forceFullSnapshotCount;
        private int closeCurrentViewCount;

        @Override
        public void registerCallbacks() {
        }

        @Override
        public void unregisterCallbacks() {
        }

        @Override
        public void stopProcessingRecords() {
        }

        @Override
        public void resumeRecorders() {
            resumeCount += 1;
        }

        @Override
        public void stopRecorders() {
            stopCount += 1;
        }

        @Override
        public void stopRecordersAndCloseCurrentView() {
            closeCurrentViewCount += 1;
            stopCount += 1;
        }

        @Override
        public void forceFullSnapshot() {
            forceFullSnapshotCount += 1;
        }
    }

    private static final class TestFeatureSdkCore implements FeatureSdkCore {
        private final InternalLogger logger = new NoOpInternalLogger();
        private final Map<String, Object> context = new HashMap<>();
        private TrackingConsent trackingConsent = TrackingConsent.GRANTED;

        @Override
        public void init(Context context) {
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public String getService() {
            return "test";
        }

        @Override
        public InternalLogger getInternalLogger() {
            return logger;
        }

        @Override
        public void registerFeature(Feature feature) {
        }

        @Override
        public FeatureScope getFeature(String featureName) {
            return null;
        }

        @Override
        public void updateFeatureContext(String featureName, SessionReplayRecordCallback.UpdateCallBack updateCallback) {
            updateCallback.onUpdate(context);
        }

        @Override
        public Map<String, Object> getFeatureContext(String featureName) {
            return context;
        }

        @Override
        public void setEventReceiver(String featureName, FeatureEventReceiver receiver) {
        }

        @Override
        public void setContextUpdateReceiver(String featureName, FeatureContextUpdateReceiver listener) {
        }

        @Override
        public void removeContextUpdateReceiver(String featureName, FeatureContextUpdateReceiver listener) {
        }

        @Override
        public void removeEventReceiver(String featureName) {
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
        public Activity curentActivity() {
            return null;
        }

        @Override
        public void setConsentProvider(TrackingConsent consentProvider) {
            trackingConsent = consentProvider;
        }

        @Override
        public TrackingConsent getConsentProvider() {
            return trackingConsent;
        }

        @Override
        public long getErrorTimeLine() {
            return 0;
        }
    }
}
