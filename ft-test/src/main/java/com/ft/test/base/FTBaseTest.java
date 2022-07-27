package com.ft.test.base;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.FTExceptionHandler;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrackInner;
import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.SyncTaskManager;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FTBaseTest {

    protected static final String CONTENT_SIMPLE_TEST = "----simpleTest----";
    protected static final String TEST_MEASUREMENT = "testMeasurement";
    public static final String ANY_VIEW = "AnyView";
    protected static final String CUSTOM_KEY = "custom_key";
    protected static final String CUSTOM_VALUE = "custom_value";
    protected static final String TEST_FAKE_RUM_ID = "rumId";
    protected static final String TEST_FAKE_URL = "http://test.url";


    protected Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
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

    protected void invokeSyncData(DataType type, String measurement, JSONObject tags, JSONObject fileds) throws Exception {
        Whitebox.invokeMethod(FTTrackInner.getInstance(), "syncDataBackground",
                type, Utils.getCurrentNanoTime(), measurement, tags, fileds);

    }


    protected void avoidCrash() {
        try {
            Whitebox.setInternalState(FTExceptionHandler.get(), "isAndroidTest", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void invokeCheckActionClose() throws Exception {
        Thread.sleep(1000);
        Whitebox.invokeMethod(FTRUMGlobalManager.get(), "checkActionClose");

    }

    protected void invokeGenerateRumData() throws Exception {
        Thread.sleep(1000);
        Whitebox.invokeMethod(FTRUMGlobalManager.get(), "generateRumData");

    }

    protected void waitForInThreadPool() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        EventConsumerThreadPool.get().execute(() -> {
            countDownLatch.countDown();
        });
        countDownLatch.await();

    }

    protected static void avoidCleanData() {
        try {
            Whitebox.setInternalState(FTDBManager.get(), "isAndroidTest", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setSessionExpire() throws IllegalAccessException {
        Field lastActionField = Whitebox.getField(FTRUMGlobalManager.class, "lastActionTime");
        lastActionField.setAccessible(true);
        long lastActionTime = (long) lastActionField.get(FTRUMGlobalManager.get());
        Whitebox.setInternalState(FTRUMGlobalManager.get(), "lastActionTime", lastActionTime - 900000000000L);

    }

    /**
     * 上传数据测试
     *
     * @param dataType
     */
    protected void uploadData(DataType dataType) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);
        SyncDataHelper syncDataManager = new SyncDataHelper();
        String body = syncDataManager.getBodyContent(dataType, recordDataList);
        body = body.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION).replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK);

        try {
            Whitebox.invokeMethod(SyncTaskManager.get(), "requestNet", dataType, body,
                    (AsyncCallback) (code, response) -> Assert.assertEquals(200, code));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        FTDBManager.get().delete();
        FTSdk.shutDown();
    }


}
