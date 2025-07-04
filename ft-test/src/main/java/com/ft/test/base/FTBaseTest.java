package com.ft.test.base;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.FTExceptionHandler;
import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrackInner;
import com.ft.sdk.FTUIBlockManager;
import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.SyncTaskManager;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.RequestCallback;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base test case class for quickly building Android Tests
 */
public class FTBaseTest {

    public static final String ANY_VIEW = "AnyView";
    protected static final String CUSTOM_KEY = "custom_key";
    protected static final String CUSTOM_VALUE = "custom_value";
    protected static final String DYNAMIC_CUSTOM_KEY = "dynamic_custom_key";
    protected static final String DYNAMIC_CUSTOM_VALUE = "dynamic_custom_value";
    protected static final String DYNAMIC_SINGLE_CUSTOM_KEY = "dynamic_custom_single_key";
    protected static final String DYNAMIC_SINGLE_CUSTOM_VALUE = "dynamic_custom_single_value";
    protected static final String TEST_FAKE_RUM_ID = "rumId";
    protected static final String TEST_FAKE_USER_ID = "fakeUserid";
    protected static final String TEST_FAKE_URL = "http://www.test.url";
    protected static final String TEST_FAKE_CLIENT_TOKEN = "fake_client_token";
    protected static final String LOG_TEST_DATA_1_KB =
            "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";


    protected Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * Get Datakit client configuration
     *
     * @return
     */
    protected FTSDKConfig getDatakitConfig() {
        return FTSDKConfig.builder(TEST_FAKE_URL);
    }

    /**
     * Get dataway client configuration
     *
     * @return
     */
    protected FTSDKConfig getDatawaytConfig() {
        return FTSDKConfig.builder(TEST_FAKE_URL, TEST_FAKE_CLIENT_TOKEN);
    }

    /**
     * Stop data synchronization, implemented by setting {@link SyncTaskManager#running} = true
     *
     * @throws Exception
     */
    protected static void stopSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", true);

    }


    /**
     * Resume data synchronization, implemented by setting {@link SyncTaskManager#running} = false
     *
     * @throws Exception
     */
    protected void resumeSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", false);
    }

    /**
     * Execute data synchronization immediately
     * <p>
     * {@link SyncTaskManager#executePoll()}
     *
     * @throws Exception
     */
    protected void executeSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "executePoll");
    }

    /**
     * Perform data synchronization immediately
     * <p>
     * {@link FTTrackInner#syncRUMDataBackground(DataType, long, String, JSONObject, JSONObject)}
     *
     * @param type
     * @param measurement
     * @param tags
     * @param fileds
     * @throws Exception
     */
    protected void invokeSyncData(DataType type, String measurement, HashMap<String, Object> tags, HashMap<String, Object> fileds) throws Exception {
        Whitebox.invokeMethod(FTTrackInner.getInstance(), "syncRUMDataBackground",
                type, Utils.getCurrentNanoTime(), measurement, tags, fileds);

    }


    /**
     * Prevent program crashes to ensure test case execution is not interrupted
     * <p>
     * {@link FTExceptionHandler#isAndroidTest}
     */
    protected void avoidCrash() {
        try {
            Whitebox.setInternalState(FTExceptionHandler.get(), "isAndroidTest", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the currently active Action
     * <p>
     * {@link FTRUMInnerManager#checkActionClose()}
     *
     * @throws Exception
     */
    protected void invokeCheckActionClose() throws Exception {
        Thread.sleep(1000);
        Whitebox.invokeMethod(FTRUMInnerManager.get(), "checkActionClose");

    }

    /**
     * Generate RUM related data immediately
     * <p>
     * {@link FTRUMInnerManager#generateRumData()}
     *
     * @throws Exception
     */
    protected void invokeGenerateRumData() throws Exception {
        Thread.sleep(1000);
        Whitebox.invokeMethod(FTRUMInnerManager.get(), "generateRumData");

    }

    /**
     * Wait for the thread pool queue execution to end, the purpose is to make thread pool functions serial in test cases, wait for operation to end
     * <p>
     * {@link EventConsumerThreadPool}
     *
     * @throws InterruptedException
     */
    protected void waitEventConsumeInThreadPool() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        EventConsumerThreadPool.get().execute(() -> {
            countDownLatch.countDown();
        });
        countDownLatch.await();

    }

    /**
     * Disable data cleanup, implemented by skipping deletion logic through {@link FTDBManager#isAndroidTest} logic
     */
    protected static void avoidCleanData() {
        try {
            Whitebox.setInternalState(FTDBManager.get(), "isAndroidTest", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Make session expire immediately to reduce time-consuming waits during test case execution
     * <p>
     * {@link FTRUMInnerManager#lastUserActiveTime}
     *
     * @throws IllegalAccessException
     */
    protected void setSessionExpire() throws IllegalAccessException {
        Field lastActionField = Whitebox.getField(FTRUMInnerManager.class, "lastUserActiveTime");
        lastActionField.setAccessible(true);
        long lastActionTime = (long) lastActionField.get(FTRUMInnerManager.get());
        Whitebox.setInternalState(FTRUMInnerManager.get(), "lastUserActiveTime", lastActionTime - 900000000000L);

    }

    /**
     * Upload data test
     * <p>
     * {@link SyncTaskManager#requestNet(DataType, String, RequestCallback)}
     *
     * @param dataType
     */
    protected void uploadData(DataType dataType) {
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);
        StringBuilder body = new StringBuilder();
        for (SyncData syncData : recordDataList) {
            body.append(syncData.getDataString());
        }

        try {
            Whitebox.invokeMethod(SyncTaskManager.get(), "requestNet", dataType, body.toString(),
                    (RequestCallback) (code, response, errorCode) -> Assert.assertEquals(200, code));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Use Whitebox to check private variables
     *
     * @param target    Access created instance
     * @param fieldName Private variable name
     * @param expect    Expected value
     * @return Whether fieldName is the same as expect
     */
    protected boolean checkInnerFieldValue(Object target, String fieldName, Object expect) {
        return Whitebox.getInternalState(target, fieldName).equals(expect);
    }


    /**
     * Get current dataHelper object
     * {@link FTTrackInner#dataHelper}
     *
     * @return
     */

    public SyncDataHelper getInnerSyncDataHelper() {
        return Whitebox.getInternalState(FTTrackInner.getInstance(), "dataHelper");
    }

    /**
     * Get config property in current datahelper
     * config of {@link FTTrackInner#dataHelper}
     *
     * @return
     */
    public FTSDKConfig getSDKConfigInSyncDataHelper() {
        return Whitebox.getInternalState(getInnerSyncDataHelper(), "config");
    }

    /**
     * Get config property in current datahelper
     * config of {@link FTTrackInner#dataHelper}
     *
     * @return
     */
    public long getLongTaskBlockDurationMS() {
        return (long) Whitebox.getInternalState(FTUIBlockManager.get(), "blockDurationNS") / 1000000;
    }

    /**
     * Get the minimum longtask judgment minimum limit value
     *
     * @return
     */
    public long getUIBlockMiniBlockDurationMS() {
        return (long) Whitebox.getInternalState(FTUIBlockManager.class, "MINI_TIME_BLOCK_NS") / 1000000;
    }

    /**
     * get current size
     *
     * @return
     */
    public long getCurrentDBSize() {
        return ((AtomicLong) Whitebox.getInternalState(FTDBCachePolicy.get(), "currentDbSize")).get();
    }

    /**
     * set db limit size
     *
     * @param
     */
    public void setDBLimit(long dbLimit) throws IllegalAccessException {
        Whitebox.setInternalState(FTDBCachePolicy.get(), "dbLimitSize", dbLimit);
    }


    /**
     * Test completed, delete and clear data
     */
    @After

    public void tearDown() {
        FTDBManager.get().delete();
        FTSdk.shutDown();
    }


}
