package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.View;
import android.view.ViewTreeObserver;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.MethodCallSamplingRate;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;
import com.ft.sdk.sessionreplay.internal.recorder.listener.WindowsOnDrawListener;

import java.util.List;

public class DefaultOnDrawListenerProducer implements OnDrawListenerProducer {


    private final SnapshotProducer snapshotProducer;
    private final RecordedDataQueueHandler recordedDataQueueHandler;
    private final FeatureSdkCore sdkCore;

    public DefaultOnDrawListenerProducer(SnapshotProducer snapshotProducer,
                                         RecordedDataQueueHandler recordedDataQueueHandler,
                                         FeatureSdkCore sdkCore) {
        this.snapshotProducer = snapshotProducer;
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.sdkCore = sdkCore;
    }

    @Override
    public ViewTreeObserver.OnDrawListener create(List<View> decorViews, SessionReplayPrivacy privacy) {
        return new WindowsOnDrawListener(
                decorViews,
                recordedDataQueueHandler,
                snapshotProducer,
                privacy,
                null,
                null,
                sdkCore.getInternalLogger(),
                MethodCallSamplingRate.DEFAULT.getRate()
        );
    }
}
