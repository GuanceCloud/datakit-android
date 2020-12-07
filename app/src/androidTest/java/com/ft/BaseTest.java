package com.ft;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Rule;
import org.powermock.reflect.Whitebox;

/**
 * author: huangDianHua
 * time: 2020/9/4 17:11:40
 * description:
 */
public class BaseTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA
            , Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.READ_PHONE_STATE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.BLUETOOTH
            , Manifest.permission.BLUETOOTH_ADMIN);

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

    protected void simpleTrackData() throws JSONException {
        JSONObject tags = new JSONObject();
        tags.put("testTag", "tagTest");
        JSONObject fields = new JSONObject();
        fields.put("testField", "----simpleTest----");
        FTTrack.getInstance().trackBackground("testMeasurement", tags, fields);
    }
}
