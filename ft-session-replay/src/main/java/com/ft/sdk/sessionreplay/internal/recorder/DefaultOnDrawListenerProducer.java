package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.View;
import android.view.ViewTreeObserver;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.MethodCallSamplingRate;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;
import com.ft.sdk.sessionreplay.internal.recorder.listener.WindowsOnDrawListener;

import java.util.List;

public class DefaultOnDrawListenerProducer implements OnDrawListenerProducer {

    private final SnapshotProducer snapshotProducer;
    private final RecordedDataQueueHandler recordedDataQueueHandler;
    private final FeatureSdkCore sdkCore;
    private final boolean dynamicOptimizationEnabled;

    public DefaultOnDrawListenerProducer(
            SnapshotProducer snapshotProducer,
            RecordedDataQueueHandler recordedDataQueueHandler,
            FeatureSdkCore sdkCore,
            boolean dynamicOptimizationEnabled
    ) {
        this.snapshotProducer = snapshotProducer;
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.sdkCore = sdkCore;
        this.dynamicOptimizationEnabled = dynamicOptimizationEnabled;
    }

    @Override
    public ViewTreeObserver.OnDrawListener create(
            List<View> decorViews,
            TextAndInputPrivacy textAndInputPrivacy,
            ImagePrivacy imagePrivacy,
            TouchPrivacyManager touchPrivacyManager
    ) {
        return new WindowsOnDrawListener(
                decorViews,
                recordedDataQueueHandler,
                snapshotProducer,
                null,
                null,
                sdkCore.getInternalLogger(),
                MethodCallSamplingRate.LOW.getRate(),
                imagePrivacy, textAndInputPrivacy, touchPrivacyManager
        );
    }
}
