package com.ft.sdk.sessionreplay.internal.recorder.callback;

import android.content.Context;
import android.os.Build;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;
import com.ft.sdk.sessionreplay.internal.async.TouchEventRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.recorder.ViewOnDrawInterceptor;
import com.ft.sdk.sessionreplay.internal.recorder.WindowInspector;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.model.PointerEventType;
import com.ft.sdk.sessionreplay.model.PointerInteractionData;
import com.ft.sdk.sessionreplay.model.PointerType;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.TimeProvider;
import com.ft.sdk.sessionreplay.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecorderWindowCallback implements Window.Callback {

    private static final String TAG = "RecorderWindowCallback";
    private final Context appContext;
    private final RecordedDataQueueHandler recordedDataQueueHandler;
    private final Window.Callback wrappedCallback;
    private final TimeProvider timeProvider;
    private final ViewOnDrawInterceptor viewOnDrawInterceptor;
    private final InternalLogger internalLogger;
    private final SessionReplayPrivacy privacy;
    //    private final MotionEventUtils motionEventUtils;
    private final long motionUpdateThresholdInNs;
    private final long flushPositionBufferThresholdInNs;
//    private final WindowInspector windowInspector;

    private final float pixelsDensity;
    private final List<MobileRecord> pointerInteractions = new LinkedList<>();
    private long lastOnMoveUpdateTimeInNs = 0L;
    private long lastPerformedFlushTimeInNs = System.nanoTime();
    private final WindowInspector windowInspector;

    public RecorderWindowCallback(
            Context appContext,
            RecordedDataQueueHandler recordedDataQueueHandler,
            Window.Callback wrappedCallback,
            TimeProvider timeProvider,
            ViewOnDrawInterceptor viewOnDrawInterceptor,
            InternalLogger internalLogger,
            SessionReplayPrivacy privacy,
            long motionUpdateThresholdInNs,
            long flushPositionBufferThresholdInNs,
            WindowInspector windowInspector
    ) {
        this.appContext = appContext;
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.wrappedCallback = wrappedCallback;
        this.timeProvider = timeProvider;
        this.viewOnDrawInterceptor = viewOnDrawInterceptor;
        this.internalLogger = internalLogger;
        this.privacy = privacy;
        this.motionUpdateThresholdInNs = motionUpdateThresholdInNs;
        this.flushPositionBufferThresholdInNs = flushPositionBufferThresholdInNs;
        this.pixelsDensity = appContext.getResources().getDisplayMetrics().density;
        this.windowInspector = windowInspector == null ? new WindowInspector() : windowInspector;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return wrappedCallback.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return wrappedCallback.dispatchKeyShortcutEvent(event);
    }

    @Override
    @MainThread
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event != null) {
            // Copy the event and delegate it to the gesture detector for analysis
            MotionEvent copy = MotionEvent.obtain(event);
            try {
                handleEvent(copy);
            } finally {
                copy.recycle();
            }
        } else {
            internalLogger.e(TAG, MOTION_EVENT_WAS_NULL_ERROR_MESSAGE);
        }

        try {
            return wrappedCallback.dispatchTouchEvent(event);
        } catch (NullPointerException e) {
            logOrRethrowWrappedCallbackException(e);
            return EVENT_CONSUMED;
        }
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return wrappedCallback.dispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return wrappedCallback.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return wrappedCallback.dispatchPopulateAccessibilityEvent(event);
    }

    @Nullable
    @Override
    public View onCreatePanelView(int featureId) {
        return wrappedCallback.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        return wrappedCallback.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {
        return wrappedCallback.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
        return wrappedCallback.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        return wrappedCallback.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        wrappedCallback.onWindowAttributesChanged(attrs);
    }

    @Override
    public void onContentChanged() {
        wrappedCallback.onContentChanged();
    }

    @MainThread
    private void handleEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // Reset the flush time to avoid flush in the next event
                lastPerformedFlushTimeInNs = System.nanoTime();
                updatePositions(event, PointerEventType.DOWN);
                // Reset the on move update time to take into account the first move event
                lastOnMoveUpdateTimeInNs = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (System.nanoTime() - lastOnMoveUpdateTimeInNs >= motionUpdateThresholdInNs) {
                    updatePositions(event, PointerEventType.MOVE);
                    lastOnMoveUpdateTimeInNs = System.nanoTime();
                }
                // Flush from time to time to avoid glitches in the player
                if (System.nanoTime() - lastPerformedFlushTimeInNs >= flushPositionBufferThresholdInNs) {
                    flushPositions();
                }
                break;
            case MotionEvent.ACTION_UP:
                updatePositions(event, PointerEventType.UP);
                flushPositions();
                lastOnMoveUpdateTimeInNs = 0;
                break;
        }
    }

    private void updatePositions(MotionEvent event, PointerEventType eventType) {
        for (int pointerIndex = 0; pointerIndex < event.getPointerCount(); pointerIndex++) {
            long pointerId = event.getPointerId(pointerIndex);
            float pointerAbsoluteX = MotionEventUtils.getPointerAbsoluteX(event, pointerIndex);
            float pointerAbsoluteY = MotionEventUtils.getPointerAbsoluteY(event, pointerIndex);
            pointerInteractions.add(new MobileRecord.MobileIncrementalSnapshotRecord(
                    timeProvider.getDeviceTimestamp(),
                    new PointerInteractionData(
                            eventType,
                            PointerType.TOUCH,
                            pointerId,
                            Utils.densityNormalized(pointerAbsoluteX, pixelsDensity),
                            Utils.densityNormalized(pointerAbsoluteY, pixelsDensity)
                    )
            ));
        }
    }

    @MainThread
    private void flushPositions() {
        if (pointerInteractions.isEmpty()) {
            return;
        }

        TouchEventRecordedDataQueueItem item = recordedDataQueueHandler.addTouchEventItem(
                new ArrayList<>(pointerInteractions)
        );
        if (item != null && item.isReady()) {
            recordedDataQueueHandler.tryToConsumeItems();
        }

        pointerInteractions.clear();
        lastPerformedFlushTimeInNs = System.nanoTime();
    }

    private void logOrRethrowWrappedCallbackException(NullPointerException e) {
        // When calling delegate callback, we may have something like
        // java.lang.NullPointerException: Parameter specified as non-null is null:
        // method xxx, parameter xxx
        // This happens because Kotlin delegate expects non-null value incorrectly inferring
        // non-null type from Java interface definition (seems to be solved in Kotlin 1.8 though)
        if (e.getMessage() != null && e.getMessage().contains("Parameter specified as non-null is null")) {
            internalLogger.e(TAG, FAIL_TO_PROCESS_MOTION_EVENT_ERROR_MESSAGE, e);
        } else {
            throw e;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        List<View> rootViews = windowInspector.getGlobalWindowViews(internalLogger);
        if (!rootViews.isEmpty()) {
            // A new window was added or removed, so we stop recording the previous root views
            // and start recording the new ones.
            viewOnDrawInterceptor.stopIntercepting();
            viewOnDrawInterceptor.intercept(rootViews, privacy);
        }
    }

    @Override
    public void onAttachedToWindow() {
        wrappedCallback.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        wrappedCallback.onDetachedFromWindow();
    }

    @Override
    public void onPanelClosed(int featureId, @NonNull Menu menu) {
        wrappedCallback.onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onSearchRequested() {
        return wrappedCallback.onSearchRequested();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        return wrappedCallback.onSearchRequested(searchEvent);
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return wrappedCallback.onWindowStartingActionMode(callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return wrappedCallback.onWindowStartingActionMode(callback, type);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        wrappedCallback.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        wrappedCallback.onActionModeFinished(mode);
    }

    public Window.Callback getWrappedCallback() {
        return wrappedCallback;
    }

    private static final boolean EVENT_CONSUMED = true;

    // Every frame we collect the move event positions
    public static final long MOTION_UPDATE_DELAY_THRESHOLD_NS = TimeUnit.MILLISECONDS.toNanos(16);

    // Every 10 frames we flush the buffer
    public static final long FLUSH_BUFFER_THRESHOLD_NS = MOTION_UPDATE_DELAY_THRESHOLD_NS * 10;

    private static final String MOTION_EVENT_WAS_NULL_ERROR_MESSAGE =
            "RecorderWindowCallback: intercepted null motion event";

    private static final String FAIL_TO_PROCESS_MOTION_EVENT_ERROR_MESSAGE =
            "RecorderWindowCallback: wrapped callback failed to handle the motion event";
}
