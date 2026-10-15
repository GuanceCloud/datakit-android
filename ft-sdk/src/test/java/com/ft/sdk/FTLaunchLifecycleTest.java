package com.ft.sdk;

import android.app.Activity;

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

    @Before
    public void prepareStartup() {
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
}
