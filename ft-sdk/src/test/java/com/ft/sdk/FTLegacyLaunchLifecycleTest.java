package com.ft.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Looper;
import com.ft.sdk.garble.utils.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.android.controller.ActivityController;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {21, 23, 28, 31})
@LooperMode(LooperMode.Mode.PAUSED)
public class FTLegacyLaunchLifecycleTest {
    private Application application;
    private FTActivityLifecycleCallbacks callbacks;
    private FTAppStartCounter counter;
    private ActivityController<Activity> controller;

    @Before public void setup() {
        application = RuntimeEnvironment.getApplication();
        counter = FTAppStartCounter.get();
        ((AtomicBoolean) Whitebox.getInternalState(counter, "firstDrawDone")).set(false);
        for (String field : new String[]{"coldStartDuration", "applicationOnCreateTimeLine",
                "applicationOnCreateCompletedTimeLine", "firstActivityPreOnActivityTimeline", "legacyStartTime"}) {
            Whitebox.setInternalState(counter, field, 0L);
        }
        for (String field : new String[]{"legacyActivityObserved", "foregroundDuringLaunch",
                "backgroundDuringLaunch", "launchFromBackground"}) Whitebox.setInternalState(counter, field, false);
        Whitebox.setInternalState(FTRUMConfigManager.get(), "config", (Object) null);
        Whitebox.setInternalState(FTActivityLifecycleCallbacks.class, "activityCount", 0);
        callbacks = new FTActivityLifecycleCallbacks();
        application.registerActivityLifecycleCallbacks(callbacks);
        long origin = System.nanoTime() - 1_000_000L;
        counter.captureLegacyStart(origin);
        Whitebox.setInternalState(counter, "applicationOnCreateTimeLine", origin);
    }

    @After public void cleanup() {
        application.unregisterActivityLifecycleCallbacks(callbacks);
        if (controller != null) {
            callbacks.onActivityPaused(controller.get());
            counter.cancelFirstFrame(controller.get());
        }
    }

    private void resume() {
        controller = Robolectric.buildActivity(Activity.class).create().start().resume();
    }

    @Test public void frameworkDrawCompletesColdStartWithoutInventingCreationTiming() {
        resume();
        controller.visible();
        controller.get().findViewById(android.R.id.content).getViewTreeObserver().dispatchOnDraw();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Assert.assertTrue(((AtomicBoolean) Whitebox.getInternalState(counter, "firstDrawDone")).get());
        Assert.assertTrue((long) Whitebox.getInternalState(counter, "coldStartDuration") > 0);
        if (Build.VERSION.SDK_INT < 29) {
            Assert.assertEquals(0L, (long) Whitebox.getInternalState(counter, "firstActivityPreOnActivityTimeline"));
            Assert.assertEquals(0L, (long) Whitebox.getInternalState(counter, "applicationOnCreateDuration"));
            Assert.assertEquals(0L, (long) Whitebox.getInternalState(counter, "firsDrawnDuration"));
        }
        if (Build.VERSION.SDK_INT < 24) {
            Assert.assertEquals(0L, (long) Whitebox.getInternalState(counter, "applicationPreOnCreateDuration"));
            Assert.assertEquals((long) Whitebox.getInternalState(counter, "legacyStartTime"),
                    (long) Whitebox.getInternalState(counter, "coldStartTimeLineForNanoDuration"));
        }
    }

    @Test public void resumedActivityIsAvailableAndHotReentryHasOneTimingOrigin() {
        resume();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Assert.assertSame(controller.get(), FTActivityManager.get().curentActivity());
        LifeCircleTraceCallback life = Whitebox.getInternalState(callbacks, "mAppRestartCallback");
        Assert.assertTrue((boolean) Whitebox.getInternalState(life, "mInited"));
        controller.pause().stop();
        Whitebox.setInternalState(life, "alreadySleep", true);
        Whitebox.setInternalState(life, "startClockTimeNano", 0L);
        controller.restart().start().resume();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Assert.assertTrue((long) Whitebox.getInternalState(life, "startClockTimeNano") > 0);
        Assert.assertFalse((boolean) Whitebox.getInternalState(life, "alreadySleep"));
        Assert.assertTrue(((Map<?, ?>) Whitebox.getInternalState(callbacks, "legacyResumes")).isEmpty());
    }

    @Test public void pauseCancelsDeferredResumeAndDestroyReleasesOwnership() {
        resume();
        controller.pause().stop().destroy();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Assert.assertTrue(((Map<?, ?>) Whitebox.getInternalState(counter, "firstDrawListeners")).isEmpty());
        Assert.assertTrue(((Map<?, ?>) Whitebox.getInternalState(callbacks, "legacyResumes")).isEmpty());
        LifeCircleTraceCallback life = Whitebox.getInternalState(callbacks, "mAppRestartCallback");
        Assert.assertTrue(((Map<?, ?>) Whitebox.getInternalState(life, "mCreateMap")).isEmpty());
    }

    @Test public void createdActivityPreventsNoActivityBackgroundCorrection() {
        controller = Robolectric.buildActivity(Activity.class).create();
        counter.appOnCreateCompleted(System.nanoTime());
        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND, counter.getColdStartLaunchType());
    }

    @Test public void repeatedStartHookRegistersOnlyOneCallback() {
        application.unregisterActivityLifecycleCallbacks(callbacks);
        Whitebox.setInternalState(FTAutoTrack.class, "lifecycleCallbacksRegistered", false);
        FTAutoTrack.startApp(application);
        FTAutoTrack.startApp(application);
        resume();
        Assert.assertEquals(1, (int) Whitebox.getInternalState(FTActivityLifecycleCallbacks.class, "activityCount"));
        controller.pause().stop().destroy();
        Assert.assertEquals(0, (int) Whitebox.getInternalState(FTActivityLifecycleCallbacks.class, "activityCount"));
    }
    @Test public void delayedRumReceivesColdOnceAndNextReentryReceivesHotOnce() {
        resume();
        controller.visible();
        controller.get().findViewById(android.R.id.content).getViewTreeObserver().dispatchOnDraw();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        java.util.List<ActionSourceType> actions = new java.util.ArrayList<>();
        Whitebox.setInternalState(FTRUMConfigManager.get(), "config", new FTRUMConfig()
                .setRumAppId("legacy-test").setEnableTraceUserAction(true)
                .setActionTrackingHandler(event -> { actions.add(event.getSourceType()); return null; }));
        counter.checkToReUpload();
        counter.checkToReUpload();
        Assert.assertEquals(java.util.Collections.singletonList(ActionSourceType.LAUNCH_COLD), actions);
        controller.pause().stop();
        LifeCircleTraceCallback life = Whitebox.getInternalState(callbacks, "mAppRestartCallback");
        Whitebox.setInternalState(life, "alreadySleep", true);
        controller.restart().start().resume();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Assert.assertEquals(java.util.Arrays.asList(ActionSourceType.LAUNCH_COLD, ActionSourceType.LAUNCH_HOT), actions);
    }

    public static class RecreatingActivity extends Activity {
        @Override public boolean isChangingConfigurations() { return true; }
    }

    @Test public void configurationRecreationDoesNotMarkColdStartBackground() {
        Activity old = Robolectric.buildActivity(RecreatingActivity.class).create().get();
        callbacks.onActivityStarted(old);
        callbacks.onActivityStopped(old);
        callbacks.onActivityDestroyed(old);
        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND, counter.getColdStartLaunchType());
        Assert.assertEquals(com.ft.sdk.garble.bean.AppState.RUN, FTActivityManager.get().getAppState());
    }

}
