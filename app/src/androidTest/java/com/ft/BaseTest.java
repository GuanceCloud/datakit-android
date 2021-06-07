package com.ft;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import com.ft.sdk.FTExceptionHandler;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.SyncTaskManager;

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
    protected static final String CONTENT_SIMPLE_TEST = "----simpleTest----";
    protected static final String TEST_MEASUREMENT = "testMeasurement";
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
        FTSdk.get().shutDown();
    }


    protected void removeActivityLifeCycle() throws Exception {
        Whitebox.invokeMethod(FTSdk.get(), "unregisterActivityLifeCallback");

    }

    protected static void stopSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", true);

    }

    protected void resumeSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", false);
    }

    protected void executeSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "executeSyncPoll");
    }

    protected void simpleTrackData() throws JSONException {
        JSONObject tags = new JSONObject();
        tags.put("testTag", "tagTest");
        JSONObject fields = new JSONObject();
        fields.put("testField", CONTENT_SIMPLE_TEST);
        FTTrack.getInstance().trackBackground(TEST_MEASUREMENT, tags, fields);
    }
//
//    protected void simpleRumEsData() throws Exception {
//        JSONObject tags = new JSONObject();
//        tags.put("testTag", "tagTest");
//        JSONObject fields = new JSONObject();
//        fields.put("testField", CONTENT_SIMPLE_TEST);
//        Whitebox.invokeMethod(FTTrackInner.getInstance(), "rumEs", "testMeasurement", tags, fields);
//    }
//
//    protected void simpleRumInfluxData() throws Exception {
//        JSONObject tags = new JSONObject();
//        tags.put("testTag", "tagTest");
//        JSONObject fields = new JSONObject();
//        fields.put("testField", CONTENT_SIMPLE_TEST);
//        Whitebox.invokeMethod(FTTrackInner.getInstance(), "rumInflux", "testMeasurement", tags, fields);
//    }

    protected void avoidCrash() {
        try {
            Whitebox.setInternalState(FTExceptionHandler.get(), "isAndroidTest", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
