package com.ft.sdk;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.os.Process;
import android.view.View;

import com.ft.sdk.garble.utils.Constants;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivityManager;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class FTAppStartCounterTest {

    private long originalAppStartTime;

    @Before
    public void saveAppStartTime() {
        originalAppStartTime = FTApplication.APP_START_TIME;
    }

    @After
    public void restoreAppStartTime() {
        FTApplication.APP_START_TIME = originalAppStartTime;
    }

    @Test
    public void backgroundComponentLaunchWithForegroundImportanceDoesNotBecomeLongForegroundLaunch()
            throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);

        long processStart = System.nanoTime() - TimeUnit.HOURS.toNanos(2);
        FTApplication.APP_START_TIME = processStart;

        FTAppStartCounter counter = newCounter();
        Application application = RuntimeEnvironment.getApplication();
        counter.appOnCreate(processStart, application);
        counter.appOnCreateCompleted(processStart + TimeUnit.MILLISECONDS.toNanos(100));
        counter.checkFirstActivityPreCreate(
                processStart + TimeUnit.HOURS.toNanos(2)
                        - TimeUnit.MILLISECONDS.toNanos(100));
        counter.coldStart(processStart + TimeUnit.HOURS.toNanos(2));

        boolean launchFromBackground =
                Whitebox.getInternalState(counter, "launchFromBackground");
        long duration = Whitebox.getInternalState(counter, "coldStartDuration");

        Assert.assertFalse(
                "A process started by a background component must not produce a multi-hour "
                        + "foreground cold-launch action",
                duration >= TimeUnit.HOURS.toNanos(2) && !launchFromBackground);
    }

    @Test
    public void slowApplicationInitializationRemainsForegroundLaunch() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);

        long processStart = System.nanoTime() - TimeUnit.HOURS.toNanos(2);
        FTApplication.APP_START_TIME = processStart;

        FTAppStartCounter counter = newCounter();
        Application application = RuntimeEnvironment.getApplication();
        counter.appOnCreate(processStart, application);
        counter.checkFirstActivityPreCreate(
                processStart + TimeUnit.HOURS.toNanos(2)
                        - TimeUnit.MILLISECONDS.toNanos(100));
        counter.appOnCreateCompleted(
                processStart + TimeUnit.HOURS.toNanos(2)
                        - TimeUnit.MILLISECONDS.toNanos(50));
        counter.coldStart(processStart + TimeUnit.HOURS.toNanos(2));

        boolean launchFromBackground =
                Whitebox.getInternalState(counter, "launchFromBackground");

        Assert.assertFalse(launchFromBackground);
    }

    @Test
    public void activityAfterFirstMainLoopIsBackgroundEvenWhenGapIsShort() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);

        long processStart = System.nanoTime() - TimeUnit.SECONDS.toNanos(1);
        FTApplication.APP_START_TIME = processStart;

        FTAppStartCounter counter = newCounter();
        Application application = RuntimeEnvironment.getApplication();
        counter.appOnCreate(processStart, application);
        counter.appOnCreateCompleted(processStart + TimeUnit.MILLISECONDS.toNanos(100));
        counter.checkFirstActivityPreCreate(processStart + TimeUnit.MILLISECONDS.toNanos(200));

        boolean launchFromBackground =
                Whitebox.getInternalState(counter, "launchFromBackground");

        Assert.assertTrue(launchFromBackground);
    }

    @Test
    public void activityQueuedBeforeFirstMainLoopRemainsForegroundLaunch() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);

        long processStart = System.nanoTime() - TimeUnit.SECONDS.toNanos(1);
        FTApplication.APP_START_TIME = processStart;

        FTAppStartCounter counter = newCounter();
        Application application = RuntimeEnvironment.getApplication();
        counter.appOnCreate(processStart, application);
        counter.checkFirstActivityPreCreate(processStart + TimeUnit.MILLISECONDS.toNanos(100));
        counter.appOnCreateCompleted(processStart + TimeUnit.MILLISECONDS.toNanos(200));

        boolean launchFromBackground =
                Whitebox.getInternalState(counter, "launchFromBackground");

        Assert.assertFalse(launchFromBackground);
    }

    @Test
    public void foregroundServiceImportanceIsClassifiedAsBackgroundLaunch() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE);

        FTAppStartCounter counter = newCounter();
        counter.appOnCreate(System.nanoTime(), RuntimeEnvironment.getApplication());

        boolean launchFromBackground =
                Whitebox.getInternalState(counter, "launchFromBackground");

        Assert.assertTrue(launchFromBackground);
    }

    @Test
    public void repeatedApplicationHookPreservesInitialBackgroundLaunch() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE);

        FTAppStartCounter counter = newCounter();
        Application application = RuntimeEnvironment.getApplication();
        long firstAppOnCreate = System.nanoTime();
        counter.appOnCreate(firstAppOnCreate, application);

        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        counter.appOnCreate(firstAppOnCreate + TimeUnit.HOURS.toNanos(12), application);

        boolean launchFromBackground =
                Whitebox.getInternalState(counter, "launchFromBackground");
        long applicationOnCreateTimeLine =
                Whitebox.getInternalState(counter, "applicationOnCreateTimeLine");

        Assert.assertTrue(launchFromBackground);
        Assert.assertEquals(firstAppOnCreate, applicationOnCreateTimeLine);
    }

    @Test
    public void repeatedApplicationHookPreservesBackgroundCorrectionAndStartupTiming()
            throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);

        long originalAppStartTime = FTApplication.APP_START_TIME;
        try {
            long duration = TimeUnit.HOURS.toNanos(12);
            long processStart = System.nanoTime() - duration;
            long preApplicationDuration = TimeUnit.MILLISECONDS.toNanos(20);
            long firstAppOnCreate = processStart + preApplicationDuration;
            FTApplication.APP_START_TIME = processStart;

            FTAppStartCounter counter = newCounter();
            Application application = RuntimeEnvironment.getApplication();
            counter.appOnCreate(firstAppOnCreate, application);
            counter.appOnCreateCompleted(processStart + TimeUnit.MILLISECONDS.toNanos(100));

            counter.appOnCreate(processStart + duration - TimeUnit.SECONDS.toNanos(1),
                    application);
            counter.checkFirstActivityPreCreate(
                    processStart + duration - TimeUnit.MILLISECONDS.toNanos(100));
            counter.appOnCreateCompleted(
                    processStart + duration - TimeUnit.MILLISECONDS.toNanos(50));
            counter.coldStart(processStart + duration);

            boolean launchFromBackground =
                    Whitebox.getInternalState(counter, "launchFromBackground");
            long applicationOnCreateTimeLine =
                    Whitebox.getInternalState(counter, "applicationOnCreateTimeLine");
            long applicationPreOnCreateDuration =
                    Whitebox.getInternalState(counter, "applicationPreOnCreateDuration");
            long coldStartDuration = Whitebox.getInternalState(counter, "coldStartDuration");

            Assert.assertTrue(launchFromBackground);
            Assert.assertEquals(firstAppOnCreate, applicationOnCreateTimeLine);
            Assert.assertEquals(preApplicationDuration, applicationPreOnCreateDuration);
            Assert.assertEquals(duration, coldStartDuration);
        } finally {
            FTApplication.APP_START_TIME = originalAppStartTime;
        }
    }

    @Test
    public void repeatedApplicationHookPreservesForegroundLaunch() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);

        FTAppStartCounter counter = newCounter();
        Application application = RuntimeEnvironment.getApplication();
        long firstAppOnCreate = System.nanoTime();
        counter.appOnCreate(firstAppOnCreate, application);
        counter.checkFirstActivityPreCreate(
                firstAppOnCreate + TimeUnit.MILLISECONDS.toNanos(100));

        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE);
        counter.appOnCreate(firstAppOnCreate + TimeUnit.MILLISECONDS.toNanos(150), application);
        counter.appOnCreateCompleted(firstAppOnCreate + TimeUnit.MILLISECONDS.toNanos(200));

        boolean launchFromBackground =
                Whitebox.getInternalState(counter, "launchFromBackground");
        long applicationOnCreateTimeLine =
                Whitebox.getInternalState(counter, "applicationOnCreateTimeLine");

        Assert.assertFalse(launchFromBackground);
        Assert.assertEquals(firstAppOnCreate, applicationOnCreateTimeLine);
    }

    @Test
    public void coldStartDurationIncludesDelayBeforeFirstActivityDraw() throws Exception {
        long delay = TimeUnit.MINUTES.toNanos(37);
        long processStart = System.nanoTime() - delay;
        FTApplication.APP_START_TIME = processStart;

        FTAppStartCounter counter = newCounter();
        counter.coldStart(processStart + delay);

        long duration = Whitebox.getInternalState(counter, "coldStartDuration");

        Assert.assertEquals(delay, duration);
    }

    @Test
    public void backgroundOnlyLaunchKeepsBackgroundType() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE);
        FTAppStartCounter counter = newCounter();
        counter.appOnCreate(System.nanoTime(), RuntimeEnvironment.getApplication());
        counter.onAppBackground();

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_BACKGROUND,
                counter.getColdStartLaunchType());
    }

    @Test
    public void backgroundThenForegroundLaunchKeepsBackgroundTypeAndDuration() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE);
        long duration = TimeUnit.HOURS.toNanos(2);
        long processStart = System.nanoTime() - duration;
        FTApplication.APP_START_TIME = processStart;
        FTAppStartCounter counter = newCounter();
        counter.appOnCreate(processStart, RuntimeEnvironment.getApplication());
        counter.appOnCreateCompleted(processStart + TimeUnit.MILLISECONDS.toNanos(100));
        counter.checkFirstActivityPreCreate(processStart + duration - TimeUnit.SECONDS.toNanos(1));
        counter.onAppForeground();
        counter.coldStart(processStart + duration);

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_BACKGROUND,
                counter.getColdStartLaunchType());
        long coldStartDuration = Whitebox.getInternalState(counter, "coldStartDuration");
        Assert.assertEquals(duration, coldStartDuration);
    }

    @Test
    public void noActivityCorrectionKeepsBackgroundTypeAfterForeground()
            throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        FTAppStartCounter counter = newCounter();
        long applicationStart = System.nanoTime();
        counter.appOnCreate(applicationStart, RuntimeEnvironment.getApplication());
        counter.appOnCreateCompleted(applicationStart + TimeUnit.MILLISECONDS.toNanos(100));

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_BACKGROUND,
                counter.getColdStartLaunchType());
        counter.onAppForeground();
        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_BACKGROUND,
                counter.getColdStartLaunchType());
    }

    @Test
    public void backgroundOriginRemainsBackgroundAcrossRepeatedLifecycleTransitions()
            throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE);
        FTAppStartCounter counter = newCounter();
        long applicationStart = System.nanoTime();
        counter.appOnCreate(applicationStart, RuntimeEnvironment.getApplication());
        counter.onAppForeground();
        counter.onAppBackground();
        counter.onAppForeground();
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        counter.appOnCreate(applicationStart + TimeUnit.HOURS.toNanos(2),
                RuntimeEnvironment.getApplication());

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_BACKGROUND,
                counter.getColdStartLaunchType());
        long firstApplicationStart = Whitebox.getInternalState(counter, "applicationOnCreateTimeLine");
        Assert.assertEquals(applicationStart, firstApplicationStart);
    }

    @Test
    public void observedForegroundPreventsNoActivityFallbackFromInventingBackground()
            throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        FTAppStartCounter counter = newCounter();
        long applicationStart = System.nanoTime();
        counter.appOnCreate(applicationStart, RuntimeEnvironment.getApplication());
        counter.onAppForeground();
        counter.appOnCreateCompleted(applicationStart + TimeUnit.MILLISECONDS.toNanos(100));

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND,
                counter.getColdStartLaunchType());
    }

    @Test
    public void foregroundBackgroundForegroundLaunchUsesMixedType() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        FTAppStartCounter counter = newCounter();
        long applicationStart = System.nanoTime();
        counter.appOnCreate(applicationStart, RuntimeEnvironment.getApplication());
        counter.onAppForeground();
        counter.onAppBackground();
        counter.onAppForeground();
        counter.appOnCreate(applicationStart + TimeUnit.HOURS.toNanos(2),
                RuntimeEnvironment.getApplication());

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND_AND_BACKGROUND,
                counter.getColdStartLaunchType());
        long firstApplicationStart = Whitebox.getInternalState(counter, "applicationOnCreateTimeLine");
        Assert.assertEquals(applicationStart, firstApplicationStart);
    }

    @Test
    public void foregroundLaunchWaitingInBackgroundForFirstDrawIsNotForegroundOnly()
            throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        long duration = TimeUnit.MINUTES.toNanos(95);
        long processStart = System.nanoTime() - duration;
        long activityOffset = TimeUnit.MILLISECONDS.toNanos(1400);
        FTApplication.APP_START_TIME = processStart;
        FTAppStartCounter counter = newCounter();
        counter.appOnCreate(processStart + TimeUnit.SECONDS.toNanos(1),
                RuntimeEnvironment.getApplication());
        counter.checkFirstActivityPreCreate(processStart + activityOffset);
        counter.onAppForeground();
        counter.onAppBackground();
        counter.onAppForeground();
        counter.coldStart(processStart + duration);

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND_AND_BACKGROUND,
                counter.getColdStartLaunchType());
        long coldStartDuration = Whitebox.getInternalState(counter, "coldStartDuration");
        long firstFrameDuration = Whitebox.getInternalState(counter, "firsDrawnDuration");
        Assert.assertEquals(duration, coldStartDuration);
        Assert.assertEquals(duration - activityOffset, firstFrameDuration);
    }

    @Test
    public void transitionsAfterColdStartDoNotChangeDelayedForegroundReport() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        FTAppStartCounter counter = newCounter();
        long processStart = System.nanoTime() - TimeUnit.SECONDS.toNanos(1);
        FTApplication.APP_START_TIME = processStart;
        counter.appOnCreate(processStart, RuntimeEnvironment.getApplication());
        counter.onAppForeground();
        counter.coldStart(processStart + TimeUnit.SECONDS.toNanos(1));
        counter.onAppBackground();
        counter.onAppForeground();
        counter.appOnCreateCompleted(processStart + TimeUnit.SECONDS.toNanos(2));

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND,
                counter.getColdStartLaunchType());
    }

    @Test
    public void transitionsAfterColdStartDoNotChangeDelayedBackgroundReport() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE);
        FTAppStartCounter counter = newCounter();
        long processStart = System.nanoTime() - TimeUnit.SECONDS.toNanos(1);
        FTApplication.APP_START_TIME = processStart;
        counter.appOnCreate(processStart, RuntimeEnvironment.getApplication());
        counter.coldStart(processStart + TimeUnit.SECONDS.toNanos(1));
        counter.onAppForeground();

        Assert.assertEquals(Constants.APP_LAUNCH_TYPE_BACKGROUND,
                counter.getColdStartLaunchType());
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void firstDrawListenerKeepsTrackingUntilDrawCallbackCompletes() throws Exception {
        setCurrentProcessImportance(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        FTAppStartCounter counter = newCounter();
        long processStart = System.nanoTime() - TimeUnit.SECONDS.toNanos(1);
        FTApplication.APP_START_TIME = processStart;
        counter.appOnCreate(processStart, RuntimeEnvironment.getApplication());
        counter.checkFirstActivityPreCreate(processStart + TimeUnit.MILLISECONDS.toNanos(100));
        ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)
                .create().start().resume().visible();
        try {
            Activity activity = controller.get();
            activity.setContentView(new View(activity));
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            AtomicInteger completions = new AtomicInteger();
            Runnable drawDone = () -> {
                completions.incrementAndGet();
                counter.coldStart(System.nanoTime());
            };
            counter.checkFirstFrameStart(activity, drawDone);
            counter.checkFirstFrameStart(activity, drawDone);

            AtomicBoolean firstDrawDone = Whitebox.getInternalState(counter, "firstDrawDone");
            Assert.assertFalse(firstDrawDone.get());
            counter.onAppBackground();
            Assert.assertEquals(Constants.APP_LAUNCH_TYPE_FOREGROUND_AND_BACKGROUND,
                    counter.getColdStartLaunchType());
            counter.onAppForeground();

            View root = activity.findViewById(android.R.id.content);
            root.getViewTreeObserver().dispatchOnDraw();
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            Assert.assertTrue(firstDrawDone.get());
            Assert.assertEquals(1, completions.get());
            root.getViewTreeObserver().dispatchOnDraw();
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            Assert.assertEquals(1, completions.get());
        } finally {
            controller.pause().stop().destroy();
        }
    }

    private void setCurrentProcessImportance(int importance) {
        Application application = RuntimeEnvironment.getApplication();
        ActivityManager activityManager =
                (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        ShadowActivityManager shadowActivityManager = Shadows.shadowOf(activityManager);

        ActivityManager.RunningAppProcessInfo processInfo =
                new ActivityManager.RunningAppProcessInfo();
        processInfo.pid = Process.myPid();
        processInfo.uid = Process.myUid();
        processInfo.processName = application.getPackageName();
        processInfo.pkgList = new String[]{application.getPackageName()};
        processInfo.importance = importance;
        shadowActivityManager.setProcesses(Collections.singletonList(processInfo));
    }

    private FTAppStartCounter newCounter() throws Exception {
        Constructor<FTAppStartCounter> constructor =
                FTAppStartCounter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
