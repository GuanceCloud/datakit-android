package com.ft.sdk.sessionreplay.internal.recorder.listener;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.MainThread;
import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueRefs;
import com.ft.sdk.sessionreplay.internal.async.SnapshotRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.recorder.Debouncer;
import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.internal.recorder.SnapshotProducer;
import com.ft.sdk.sessionreplay.internal.utils.MiscUtils;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WindowsOnDrawListener implements ViewTreeObserver.OnDrawListener {
    private final List<WeakReference<View>> weakReferencedDecorViews;
    private final RecordedDataQueueHandler recordedDataQueueHandler;
    private final SnapshotProducer snapshotProducer;
    private final SessionReplayPrivacy privacy;
    private final Debouncer debouncer;
    private final MiscUtils miscUtils;
    private final InternalLogger internalLogger;
    private final float methodCallSamplingRate;

    public WindowsOnDrawListener(
            List<View> zOrderedDecorViews,
            RecordedDataQueueHandler recordedDataQueueHandler,
            SnapshotProducer snapshotProducer,
            SessionReplayPrivacy privacy,
            Debouncer debouncer,
            MiscUtils miscUtils,
            InternalLogger internalLogger,
            float methodCallSamplingRate
    ) {
        this.weakReferencedDecorViews = new ArrayList<>();
        for (View decorView : zOrderedDecorViews) {
            weakReferencedDecorViews.add(new WeakReference<>(decorView));
        }

        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.snapshotProducer = snapshotProducer;
        this.privacy = privacy;
        this.debouncer = debouncer != null ? debouncer : new Debouncer();
        this.miscUtils = miscUtils != null ? miscUtils : new MiscUtils();
        this.internalLogger = internalLogger;
        this.methodCallSamplingRate = methodCallSamplingRate;
    }

    @MainThread
    @Override
    public void onDraw() {
        debouncer.debounce(snapshotRunnable);
    }

    private final Runnable snapshotRunnable = new Runnable() {
        @UiThread
        @Override
        public void run() {
            List<View> rootViews = new ArrayList<>();

            for (WeakReference<View> weakReference : weakReferencedDecorViews) {
                View view = weakReference.get();
                if (view != null) {
                    rootViews.add(view);
                }
            }

            if (rootViews.isEmpty()) {
                return;
            }

            View contextView = rootViews.get(0);
            if (contextView == null) {
                return;
            }

            Context context = contextView.getContext();
            SystemInformation systemInformation = miscUtils.resolveSystemInformation(context);
            SnapshotRecordedDataQueueItem item = recordedDataQueueHandler.addSnapshotItem(systemInformation);
            if (item == null) {
                return;
            }

            RecordedDataQueueRefs recordedDataQueueRefs = new RecordedDataQueueRefs(recordedDataQueueHandler);
            recordedDataQueueRefs.setRecordedDataQueueItem(item);

            //fixme innerlog
            List<Node> nodes = new ArrayList<>();

            for (View view : rootViews) {
                Node node = snapshotProducer.produce(view, systemInformation, privacy, recordedDataQueueRefs);
                if (node != null) {
                    nodes.add(node);
                }
            }


            if (!nodes.isEmpty()) {
                item.setNodes(nodes);
            }

            item.setFinishedTraversal(true);

            if (item.isReady()) {
                recordedDataQueueHandler.tryToConsumeItems();
            }
        }
    };

//    private static final String METHOD_CALL_CAPTURE_RECORD = "Capture Record";
//    private static final Class<?> METHOD_CALL_CALLER_CLASS = WindowsOnDrawListener.class;
}
