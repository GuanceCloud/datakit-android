package com.ft.sdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivityManager;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class FTAppStartCounterTest {

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
    public void coldStartDurationIncludesDelayBeforeFirstActivityDraw() throws Exception {
        long delay = TimeUnit.MINUTES.toNanos(37);
        long processStart = System.nanoTime() - delay;
        FTApplication.APP_START_TIME = processStart;

        FTAppStartCounter counter = newCounter();
        counter.coldStart(processStart + delay);

        long duration = Whitebox.getInternalState(counter, "coldStartDuration");

        Assert.assertEquals(delay, duration);
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
