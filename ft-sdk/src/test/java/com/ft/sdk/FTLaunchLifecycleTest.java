package com.ft.sdk;

import android.app.Activity;
import android.os.Looper;

import org.robolectric.Shadows;
import org.robolectric.annotation.LooperMode;

import com.ft.sdk.garble.utils.Constants;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class FTLaunchLifecycleTest {
    private FTAppStartCounter counter;
    private final Map<String, Object> originalState = new HashMap<>();
    private int originalActivityCount;
    private boolean originalFirstDrawDone;
    private long originalAppStartTime;

    @Before
    public void prepareStartup() {
        // API 23 uses this process origin. Initialize it before simulating the first draw,
        // rather than letting lazy class initialization create an origin after that draw.
        originalAppStartTime = FTApplication.APP_START_TIME;
        FTApplication.APP_START_TIME = System.nanoTime() - 1_000_000L;
        counter = FTAppStartCounter.get();
        for (String field : new String[]{"applicationOnCreateTimeLine", "launchFromBackground",
                "foregroundDuringLaunch", "backgroundDuringLaunch"}) {
            originalState.put(field, Whitebox.getInternalState(counter, field));
        }
        AtomicBoolean firstDrawDone = Whitebox.getInternalState(counter, "firstDrawDone");
        originalFirstDrawDone = firstDrawDone.getAndSet(false);
        Whitebox.setInternalState(counter, "applicationOnCreateTimeLine", System.nanoTime());
        Whitebox.setInternalState(counter, "launchFromBackground", false);
        Whitebox.setInternalState(counter, "foregroundDuringLaunch", false);
        Whitebox.setInternalState(counter, "backgroundDuringLaunch", false);
        originalActivityCount = Whitebox.getInternalState(
                FTActivityLifecycleCallbacks.class, "activityCount");
        Whitebox.setInternalState(FTActivityLifecycleCallbacks.class, "activityCount", 0);
    }

    @After
    public void restoreStartup() {
        FTApplication.APP_START_TIME = originalAppStartTime;
        Map<Activity, ?> pending = Whitebox.getInternalState(counter, "firstDrawListeners");
        for (Activity activity : new java.util.ArrayList<>(pending.keySet())) {
            counter.cancelFirstFrame(activity);
        }
        for (Map.Entry<String, Object> entry : originalState.entrySet()) {
            Whitebox.setInternalState(counter, entry.getKey(), entry.getValue());
        }
        AtomicBoolean firstDrawDone = Whitebox.getInternalState(counter, "firstDrawDone");
        firstDrawDone.set(originalFirstDrawDone);
        Whitebox.setInternalState(FTActivityLifecycleCallbacks.class, "activityCount",
                originalActivityCount);
    }

    @Test
    public void onlyStoppingLastActivityAddsBackgroundToStartup() {
        FTActivityLifecycleCallbacks callbacks = new FTActivityLifecycleCallbacks();
        Activity first = Robolectric.buildActivity(Activity.class).create().get();
        Activity second = Robolectric.buildActivity(Activity.class).create().get();

        callbacks.onActivityStarted(first);
        callbacks.onActivityStarted(second);
        callbacks.onActivityStopped(first);
        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND,
                counter.getColdStartLaunchType());

        callbacks.onActivityStopped(second);
        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND_AND_BACKGROUND,
                counter.getColdStartLaunchType());
        callbacks.onActivityStarted(second);
        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND_AND_BACKGROUND,
                counter.getColdStartLaunchType());
        callbacks.onActivityStopped(second);
    }
    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void resumedRouterDoesNotPreventLaterActivityFromProvidingFirstDraw() {
        Activity router = Robolectric.buildActivity(Activity.class).create().get();
        Activity target = resumedActivity();
        LifeCircleTraceCallback callbacks = new LifeCircleTraceCallback();
        callbacks.onPreOnCreate(router);
        callbacks.onPostResume(router);
        callbacks.onPreOnCreate(target);
        callbacks.onPostResume(target);
        target.findViewById(android.R.id.content).getViewTreeObserver().dispatchOnDraw();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        AtomicBoolean firstDrawDone = Whitebox.getInternalState(counter, "firstDrawDone");
        Assert.assertTrue(firstDrawDone.get());
        Assert.assertTrue((long) Whitebox.getInternalState(counter, "coldStartDuration") > 0);
        callbacks.onPostDestroy(router);
        callbacks.onPostDestroy(target);
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void pausedAndDestroyedUndrawnActivitiesReleaseTheirCandidates() {
        Activity first = resumedActivity();
        Activity second = resumedActivity();
        FTActivityLifecycleCallbacks callbacks = new FTActivityLifecycleCallbacks();
        callbacks.onActivityPostResumed(first);
        callbacks.onActivityPostResumed(second);
        callbacks.onActivityPaused(first);
        callbacks.onActivityPostDestroyed(second);
        first.findViewById(android.R.id.content).getViewTreeObserver().dispatchOnDraw();
        second.findViewById(android.R.id.content).getViewTreeObserver().dispatchOnDraw();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        AtomicBoolean firstDrawDone = Whitebox.getInternalState(counter, "firstDrawDone");
        Assert.assertFalse(firstDrawDone.get());
        Assert.assertTrue(((Map<?, ?>) Whitebox.getInternalState(
                counter, "firstDrawListeners")).isEmpty());

        callbacks.onActivityPostResumed(first);
        first.findViewById(android.R.id.content).getViewTreeObserver().dispatchOnDraw();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Assert.assertTrue(firstDrawDone.get());
        callbacks.onActivityPostDestroyed(first);
    }
    private Activity resumedActivity() {
        Activity activity = Robolectric.buildActivity(Activity.class)
                .create().start().resume().visible().get();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        return activity;
    }


}
