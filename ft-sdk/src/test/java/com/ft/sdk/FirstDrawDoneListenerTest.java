package com.ft.sdk;

import android.app.Activity;
import android.os.Looper;
import android.view.View;

import com.ft.sdk.garble.utils.FirstDrawDoneListener;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {23, 28})
@LooperMode(LooperMode.Mode.PAUSED)
public class FirstDrawDoneListenerTest {
    @Test
    public void cancellingBeforeAttachPreventsLaterDrawCallback() {
        ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();
        View view = new View(controller.get());
        AtomicInteger completions = new AtomicInteger();
        FirstDrawDoneListener listener = FirstDrawDoneListener.registerForNextDrawCancellable(
                view, completions::incrementAndGet);
        listener.cancel();
        controller.get().setContentView(view);
        view.getViewTreeObserver().dispatchOnDraw();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Assert.assertEquals(0, completions.get());
        controller.pause().stop().destroy();
    }

    @Test
    public void cancellationAfterObservedDrawKeepsItsCompletion() {
        View view = new View(RuntimeEnvironment.getApplication());
        AtomicInteger completions = new AtomicInteger();
        FirstDrawDoneListener listener = FirstDrawDoneListener.registerForNextDrawCancellable(
                view, completions::incrementAndGet);
        listener.onDraw();
        listener.cancel();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        listener.onDraw();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Assert.assertEquals(1, completions.get());
    }
}
