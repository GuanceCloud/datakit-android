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
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;
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
 * 基础测试用例类，用于快速构建 Android Test
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
    protected static final String TEST_FAKE_URL = "http://www.test.url";
    protected static final String TEST_FAKE_CLIENT_TOKEN = "fake_client_token";
    protected static final String LOG_TEST_DATA_1_KB =
            "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";


    protected Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * 获取 Datakit 客户端配置
     *
     * @return
     */
    protected FTSDKConfig getDatakitConfig() {
        return FTSDKConfig.builder(TEST_FAKE_URL);
    }

    /**
     * 获取 dataway 客户端配置
     *
     * @return
     */
    protected FTSDKConfig getDatawaytConfig() {
        return FTSDKConfig.builder(TEST_FAKE_URL, TEST_FAKE_CLIENT_TOKEN);
    }

    /**
     * 停止数据同步，{@link SyncTaskManager#running} = true 变量来实现
     *
     * @throws Exception
     */
    protected static void stopSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", true);

    }


    /**
     * 恢复数据同步,{@link SyncTaskManager#running} = false 变量来实现
     *
     * @throws Exception
     */
    protected void resumeSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", false);
    }

    /**
     * 立即执行数据同步
     * <p>
     * {@link SyncTaskManager#executePoll()}
     *
     * @throws Exception
     */
    protected void executeSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "executePoll");
    }

    /**
     * 立即进行数据同步
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
     * 组织程序崩溃，保证测试用例执行不中断
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
     * 关闭当前激活 Action
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
     * 立即生成 RUM 相关数据
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
     * 等待线程池队列执行结束，目的是让线程池函数在测试用例中串行，等待操作结束
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
     * 禁止数据清理，通过 {@link FTDBManager#isAndroidTest} 逻辑跳过删除逻辑来实现
     */
    protected static void avoidCleanData() {
        try {
            Whitebox.setInternalState(FTDBManager.get(), "isAndroidTest", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使 session 立即过期，以缩短测试用例在测试过程中的耗时等待
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
     * 上传数据测试
     * <p>
     * {@link SyncTaskManager#requestNet(DataType, String, AsyncCallback)}
     *
     * @param dataType
     */
    protected void uploadData(DataType dataType) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);
        StringBuilder body = new StringBuilder();
        for (SyncJsonData syncJsonData : recordDataList) {
            body.append(syncJsonData.getDataString());
        }

        try {
            Whitebox.invokeMethod(SyncTaskManager.get(), "requestNet", dataType, body.toString(),
                    (AsyncCallback) (code, response, errorCode) -> Assert.assertEquals(200, code));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用 Whitebox 检验私有变量
     *
     * @param target    访问已创建实例
     * @param fieldName 私有变量名称
     * @param expect    预期值
     * @return fieldName 是否与 expect 相同
     */
    protected boolean checkInnerFieldValue(Object target, String fieldName, Object expect) {
        return Whitebox.getInternalState(target, fieldName).equals(expect);
    }


    /**
     * 获取当前 dataHelper 对象
     * {@link FTTrackInner#dataHelper}
     *
     * @return
     */

    public SyncDataHelper getInnerSyncDataHelper() {
        return Whitebox.getInternalState(FTTrackInner.getInstance(), "dataHelper");
    }

    /**
     * 获取当前 datahelper 中 config属性
     * {@link FTTrackInner#dataHelper} 的 config
     *
     * @return
     */
    public FTSDKConfig getSDKConfigInSyncDataHelper() {
        return Whitebox.getInternalState(getInnerSyncDataHelper(), "config");
    }

    /**
     * 获取当前 datahelper 中 config属性
     * {@link FTTrackInner#dataHelper} 的 config
     *
     * @return
     */
    public long getLongTaskBlockDurationMS() {
        return (long) Whitebox.getInternalState(FTUIBlockManager.get(), "blockDurationNS") / 1000000;
    }

    /**
     * 获取最小 longtask 判定最小限制数值
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
     * 测试完毕，删除清空数据
     */
    @After

    public void tearDown() {
        FTDBManager.get().delete();
        FTSdk.shutDown();
    }


}
