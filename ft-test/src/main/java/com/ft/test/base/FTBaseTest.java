package com.ft.test.base;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.FTExceptionHandler;
import com.ft.sdk.FTRUMInnerManager;
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

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 基础测试用例类，用于快速构建 Android Test
 */
public class FTBaseTest {

    public static final String ANY_VIEW = "AnyView";
    protected static final String CUSTOM_KEY = "custom_key";
    protected static final String CUSTOM_VALUE = "custom_value";
    protected static final String TEST_FAKE_RUM_ID = "rumId";
    protected static final String TEST_FAKE_URL = "http://www.test.url";
    protected static final String TEST_FAKE_CLIENT_TOKEN = "fake_client_token";


    protected Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
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
     * {@link FTTrackInner#syncDataBackground(DataType, long, String, JSONObject, JSONObject)}
     *
     * @param type
     * @param measurement
     * @param tags
     * @param fileds
     * @throws Exception
     */
    protected void invokeSyncData(DataType type, String measurement, JSONObject tags, JSONObject fileds) throws Exception {
        Whitebox.invokeMethod(FTTrackInner.getInstance(), "syncDataBackground",
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
     * {@link FTRUMInnerManager#lastActionTime}
     *
     * @throws IllegalAccessException
     */
    protected void setSessionExpire() throws IllegalAccessException {
        Field lastActionField = Whitebox.getField(FTRUMInnerManager.class, "lastActionTime");
        lastActionField.setAccessible(true);
        long lastActionTime = (long) lastActionField.get(FTRUMInnerManager.get());
        Whitebox.setInternalState(FTRUMInnerManager.get(), "lastActionTime", lastActionTime - 900000000000L);

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
        SyncDataHelper syncDataManager = getInnerSyncDataHelper();
        String body = syncDataManager.getBodyContent(dataType, recordDataList);
        body = body.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION).replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK);

        try {
            Whitebox.invokeMethod(SyncTaskManager.get(), "requestNet", dataType, body,
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

    public static SyncDataHelper getInnerSyncDataHelper() {
        return Whitebox.getInternalState(FTTrackInner.getInstance(), "dataHelper");
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
