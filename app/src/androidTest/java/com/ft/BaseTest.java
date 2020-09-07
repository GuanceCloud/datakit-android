package com.ft;

import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.junit.After;
import org.powermock.reflect.Whitebox;

/**
 * author: huangDianHua
 * time: 2020/9/4 17:11:40
 * description:
 */
public class BaseTest {
    @After
    public void tearDown() {
        FTDBManager.get().delete();
        try {
            FTSdk.get().shutDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void removeActivityLifeCycle() {
        try {
            Whitebox.invokeMethod(FTSdk.get(), "unregisterActivityLifeCallback");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void stopSyncTask() {
        try {
            Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void startSyncTask() {
        try {
            Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
