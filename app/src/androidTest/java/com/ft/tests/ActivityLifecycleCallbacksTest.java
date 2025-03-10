package com.ft.tests;

import static com.ft.AllTests.hasPrepare;

import android.os.Looper;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.DebugMainActivity;
import com.ft.sdk.FTActivityLifecycleCallbacks;
import com.ft.sdk.FTAutoTrack;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ActivityLifecycleCallbacksTest {


    @BeforeClass
    public static void settingBeforeLaunch() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }

        FTAutoTrack.startApp(null);
    }

    /**
     * 测试应用前后台判断
     */
    @Test
    public void testIsAppInForeground() {
        ActivityScenario<DebugMainActivity> scenario = ActivityScenario.launch(DebugMainActivity.class);
        Assert.assertTrue(FTActivityLifecycleCallbacks.isAppInForeground());
        scenario.close();
        Assert.assertFalse(FTActivityLifecycleCallbacks.isAppInForeground());

    }
}
