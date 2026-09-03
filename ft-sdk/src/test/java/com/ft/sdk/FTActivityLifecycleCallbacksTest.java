package com.ft.sdk;

import android.app.ActivityManager;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class FTActivityLifecycleCallbacksTest {

    @Test
    public void detectsForegroundSiblingFromSameApplicationUid() {
        ActivityManager.RunningAppProcessInfo main = process(1000, 11,
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE);
        ActivityManager.RunningAppProcessInfo cocos = process(1000, 22,
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);

        Assert.assertTrue(FTActivityLifecycleCallbacks.hasForegroundSiblingProcess(
                Arrays.asList(main, cocos), 1000, 11));
    }

    @Test
    public void ignoresCurrentBackgroundAndOtherApplicationProcesses() {
        ActivityManager.RunningAppProcessInfo current = process(1000, 11,
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        ActivityManager.RunningAppProcessInfo otherApp = process(2000, 22,
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND);
        ActivityManager.RunningAppProcessInfo siblingBackground = process(1000, 33,
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE);

        Assert.assertFalse(FTActivityLifecycleCallbacks.hasForegroundSiblingProcess(
                Arrays.asList(current, otherApp, siblingBackground), 1000, 11));
        Assert.assertFalse(FTActivityLifecycleCallbacks.hasForegroundSiblingProcess(
                Collections.emptyList(), 1000, 11));
        Assert.assertFalse(FTActivityLifecycleCallbacks.hasForegroundSiblingProcess(
                null, 1000, 11));
    }

    private static ActivityManager.RunningAppProcessInfo process(int uid, int pid, int importance) {
        ActivityManager.RunningAppProcessInfo process = new ActivityManager.RunningAppProcessInfo();
        process.uid = uid;
        process.pid = pid;
        process.importance = importance;
        return process;
    }
}
