package com.ft.sdk.sessionreplay.internal.recorder.callback;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.TouchPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.internal.recorder.ViewOnDrawInterceptor;
import com.ft.sdk.sessionreplay.internal.recorder.WindowCallbackInterceptor;
import com.ft.sdk.sessionreplay.internal.recorder.WindowInspector;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class RecorderWindowCallbackTest {
    private final List<String> events = new ArrayList<>();
    private final HostCallback host = new HostCallback();
    private final Window.Callback windowHost = (Window.Callback) java.lang.reflect.Proxy.newProxyInstance(
            Window.Callback.class.getClassLoader(), new Class<?>[]{Window.Callback.class},
            (proxy, method, args) -> method.invoke(host, args));
    private final CountingDrawInterceptor draws = new CountingDrawInterceptor();
    private final CountingTouchPrivacy touchPrivacy = new CountingTouchPrivacy();
    private final TestWindowInspector inspector = new TestWindowInspector();

    @Test
    public void focusGainAndLoss_delegateOnceBeforeRefreshingRecording() {
        RecorderWindowCallback callback = newCallback(host);
        callback.onWindowFocusChanged(true);
        callback.onWindowFocusChanged(false);

        assertEquals(Arrays.asList(true, false), host.focusChanges);
        assertEquals(Arrays.asList("host:true", "roots", "stop", "intercept",
                "host:false", "roots", "stop", "intercept"), events);
        assertSame(inspector.roots, draws.lastRoots);
        assertSame(TextAndInputPrivacy.MASK_ALL, draws.lastTextPrivacy);
        assertSame(ImagePrivacy.MASK_ALL, draws.lastImagePrivacy);
    }

    @Test
    public void emptyRoots_stillDelegateWithoutRestartingDrawing() {
        inspector.roots = Collections.emptyList();
        RecorderWindowCallback callback = newCallback(host);
        callback.onWindowFocusChanged(true);
        callback.onWindowFocusChanged(false);

        assertEquals(Arrays.asList(true, false), host.focusChanges);
        assertEquals(0, draws.intercepts);
        assertEquals(0, draws.stops);
    }

    @Test
    public void recorderFailure_doesNotSwallowHostFocus() {
        inspector.failure = new IllegalStateException("root lookup failed");
        try {
            newCallback(host).onWindowFocusChanged(true);
            fail("Expected recorder failure");
        } catch (IllegalStateException e) {
            assertSame(inspector.failure, e);
        }
        assertEquals(Collections.singletonList(true), host.focusChanges);
    }

    @Test
    public void hostFailure_propagatesWithoutCallingHostTwice() {
        host.failure = new IllegalStateException("host failed");
        try {
            newCallback(host).onWindowFocusChanged(false);
            fail("Expected host failure");
        } catch (IllegalStateException e) {
            assertSame(host.failure, e);
        }
        assertEquals(Collections.singletonList(false), host.focusChanges);
        assertEquals(Collections.singletonList("host:false"), events);
    }

    @Test
    public void stopUnderOuterWrapper_preservesWrapperAndDisablesRecording() {
        Window window = newWindow();
        WindowCallbackInterceptor interceptor = newInterceptor();
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        RecorderWindowCallback recorder = (RecorderWindowCallback) window.getCallback();
        OuterCallback outer = new OuterCallback(recorder);
        window.setCallback(outer);

        interceptor.stopIntercepting();
        assertSame(outer, window.getCallback());
        recorder = withInspector(recorder);
        recorder.onWindowFocusChanged(true);
        recorder.onWindowFocusChanged(false);
        dispatchDown(outer);

        assertEquals(Arrays.asList(true, false), host.focusChanges);
        assertEquals(0, draws.intercepts);
        assertEquals(0, touchPrivacy.checks);
        assertEquals(1, host.touches);
    }

    @Test
    public void stopSpecificWindow_deactivatesNestedCallback() {
        Window window = newWindow();
        WindowCallbackInterceptor interceptor = newInterceptor();
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        RecorderWindowCallback recorder = withInspector((RecorderWindowCallback) window.getCallback());
        OuterCallback outer = new OuterCallback(recorder);
        window.setCallback(outer);

        interceptor.stopIntercepting(Collections.singletonList(window));
        outer.onWindowFocusChanged(true);
        dispatchDown(outer);

        assertSame(outer, window.getCallback());
        assertEquals(Collections.singletonList(true), host.focusChanges);
        assertEquals(0, draws.intercepts);
        assertEquals(0, touchPrivacy.checks);
    }

    @Test
    public void stopTopLevel_restoresHostAndDeactivatesRetainedReference() {
        Window window = newWindow();
        WindowCallbackInterceptor interceptor = newInterceptor();
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        RecorderWindowCallback recorder = withInspector((RecorderWindowCallback) window.getCallback());

        interceptor.stopIntercepting();
        assertSame(windowHost, window.getCallback());
        recorder.onWindowFocusChanged(true);
        assertEquals(Collections.singletonList(true), host.focusChanges);
        assertEquals(0, draws.intercepts);
    }

    @Test
    public void repeatedIntercept_sameWindowDoesNotDuplicateRecording() {
        Window window = newWindow();
        WindowCallbackInterceptor interceptor = newInterceptor();
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        Window.Callback first = window.getCallback();
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        assertSame(first, window.getCallback());
    }

    @Test
    public void outerWrapperAddedWhileRecording_retiresPreviousRecorderOnRewrap() {
        Window window = newWindow();
        WindowCallbackInterceptor interceptor = newInterceptor();
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        RecorderWindowCallback first = withInspector((RecorderWindowCallback) window.getCallback());
        window.setCallback(new OuterCallback(first));
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        withInspector((RecorderWindowCallback) window.getCallback());

        window.getCallback().onWindowFocusChanged(true);
        dispatchDown(window.getCallback());
        assertEquals(Collections.singletonList(true), host.focusChanges);
        assertEquals(1, draws.intercepts);
        assertEquals(1, touchPrivacy.checks);
    }

    @Test
    public void repeatedOwnershipHandoffs_onlyCurrentRecorderObservesEvents() {
        Window window = newWindow();
        WindowCallbackInterceptor interceptor = newInterceptor();
        for (int i = 0; i < 3; i++) {
            interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
            RecorderWindowCallback recorder = withInspector((RecorderWindowCallback) window.getCallback());
            OuterCallback outer = new OuterCallback(recorder);
            window.setCallback(outer);
            outer.onWindowFocusChanged(true);
            dispatchDown(outer);
            assertEquals(i + 1, draws.intercepts);
            assertEquals(i + 1, touchPrivacy.checks);

            interceptor.stopIntercepting();
            outer.onWindowFocusChanged(false);
            dispatchDown(outer);
            assertSame(outer, window.getCallback());
            assertEquals(i + 1, draws.intercepts);
            assertEquals(i + 1, touchPrivacy.checks);
        }
        assertEquals(Arrays.asList(true, false, true, false, true, false), host.focusChanges);
        assertEquals(6, host.touches);
    }

    @Test
    public void stoppingDoesNotRemoveAnotherInterceptorsWrapper() {
        Window window = newWindow();
        WindowCallbackInterceptor first = newInterceptor();
        WindowCallbackInterceptor second = newInterceptor();
        first.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        RecorderWindowCallback inner = withInspector((RecorderWindowCallback) window.getCallback());
        second.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        Window.Callback outer = window.getCallback();
        first.stopIntercepting();
        assertSame(outer, window.getCallback());
        inner.onWindowFocusChanged(true);
        assertEquals(0, draws.intercepts);
        second.stopIntercepting();
    }

    @Test
    public void hostCanReleaseOwnershipBeforeRecorderRefresh() {
        Window window = newWindow();
        WindowCallbackInterceptor interceptor = newInterceptor();
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        withInspector((RecorderWindowCallback) window.getCallback());
        host.focusAction = interceptor::stopIntercepting;

        window.getCallback().onWindowFocusChanged(true);

        assertEquals(Collections.singletonList(true), host.focusChanges);
        assertEquals(Collections.singletonList("host:true"), events);
        assertSame(windowHost, window.getCallback());
        assertEquals(0, draws.intercepts);
    }

    @Test
    public void stoppingOneWindowKeepsOtherWindowRecording() {
        Window first = newWindow();
        Window second = newWindow();
        WindowCallbackInterceptor interceptor = newInterceptor();
        interceptor.intercept(Arrays.asList(first, second), RuntimeEnvironment.application);
        RecorderWindowCallback stopped = withInspector((RecorderWindowCallback) first.getCallback());
        withInspector((RecorderWindowCallback) second.getCallback());
        interceptor.stopIntercepting(Collections.singletonList(first));

        stopped.onWindowFocusChanged(false);
        second.getCallback().onWindowFocusChanged(true);

        assertEquals(Arrays.asList(false, true), host.focusChanges);
        assertEquals(1, draws.intercepts);
        assertSame(windowHost, first.getCallback());
        interceptor.stopIntercepting();
        assertSame(windowHost, second.getCallback());
    }

    @Test
    public void nullOriginalCallback_isRestored() {
        Window window = newWindow();
        window.setCallback(null);
        WindowCallbackInterceptor interceptor = newInterceptor();
        interceptor.intercept(Collections.singletonList(window), RuntimeEnvironment.application);
        interceptor.stopIntercepting();
        assertNull(window.getCallback());
    }

    private RecorderWindowCallback newCallback(Window.Callback delegate) {
        return new RecorderWindowCallback(RuntimeEnvironment.application, null, delegate, null,
                draws, new NoOpInternalLogger(), TextAndInputPrivacy.MASK_ALL,
                ImagePrivacy.MASK_ALL, touchPrivacy, 0, 0, inspector);
    }

    private WindowCallbackInterceptor newInterceptor() {
        return new WindowCallbackInterceptor(null, draws, null, new NoOpInternalLogger(),
                ImagePrivacy.MASK_ALL, TextAndInputPrivacy.MASK_ALL, touchPrivacy);
    }

    private Window newWindow() {
        try {
            Window window = (Window) Class.forName("com.android.internal.policy.PhoneWindow")
                    .getConstructor(Context.class).newInstance(RuntimeEnvironment.application);
            window.setCallback(windowHost);
            return window;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private RecorderWindowCallback withInspector(RecorderWindowCallback callback) {
        // Keep the production Window callback installation path; replace only root discovery.
        try {
            java.lang.reflect.Field field = RecorderWindowCallback.class.getDeclaredField("windowInspector");
            field.setAccessible(true);
            field.set(callback, inspector);
            return callback;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private void dispatchDown(Window.Callback callback) {
        MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 1, 1, 0);
        try {
            assertTrue(callback.dispatchTouchEvent(event));
        } finally {
            event.recycle();
        }
    }

    private class HostCallback extends NoOpWindowCallback {
        final List<Boolean> focusChanges = new ArrayList<>();
        int touches;
        RuntimeException failure;
        Runnable focusAction;
        @Override public void onWindowFocusChanged(boolean focus) {
            focusChanges.add(focus);
            events.add("host:" + focus);
            if (failure != null) throw failure;
            if (focusAction != null) focusAction.run();
        }
        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            touches++;
            return true;
        }
    }

    // Models Action/AppCompat/third-party callback ownership without inspecting its internals.
    private static class OuterCallback extends NoOpWindowCallback {
        final Window.Callback delegate;
        OuterCallback(Window.Callback delegate) { this.delegate = delegate; }
        @Override public void onWindowFocusChanged(boolean focus) { delegate.onWindowFocusChanged(focus); }
        @Override public boolean dispatchTouchEvent(MotionEvent event) { return delegate.dispatchTouchEvent(event); }
    }

    private class TestWindowInspector extends WindowInspector {
        List<View> roots = Collections.singletonList(new View(RuntimeEnvironment.application));
        RuntimeException failure;
        @Override public List<View> getGlobalWindowViews(InternalLogger logger) {
            events.add("roots");
            if (failure != null) throw failure;
            return roots;
        }
    }

    private class CountingDrawInterceptor extends ViewOnDrawInterceptor {
        int intercepts, stops;
        List<View> lastRoots;
        TextAndInputPrivacy lastTextPrivacy;
        ImagePrivacy lastImagePrivacy;
        CountingDrawInterceptor() { super(new NoOpInternalLogger(), null, null); }
        @Override public void stopIntercepting() { stops++; events.add("stop"); }
        @Override public void intercept(List<View> roots, TextAndInputPrivacy textPrivacy, ImagePrivacy imagePrivacy) {
            intercepts++;
            events.add("intercept");
            lastRoots = roots;
            lastTextPrivacy = textPrivacy;
            lastImagePrivacy = imagePrivacy;
        }
    }

    private static class CountingTouchPrivacy extends TouchPrivacyManager {
        int checks;
        CountingTouchPrivacy() { super(TouchPrivacy.HIDE); }
        @Override public boolean shouldRecordTouch(Point location) { checks++; return false; }
    }
}
