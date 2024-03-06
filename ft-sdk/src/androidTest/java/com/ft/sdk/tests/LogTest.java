package com.ft.sdk.tests;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * 日志输出测试
 */
public class LogTest extends FTBaseTest {

    @Before
    public void setUp() throws Exception {
        stopSyncTask();
    }

    /**
     * 测试采样率为 0 时，数据采集的情况
     *
     * @throws InterruptedException
     */
    @Test
    public void logSampleRateZero() throws InterruptedException {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true).setSamplingRate(0));

        Thread.sleep(1000);

        FTLogger.getInstance().logBackground("test", Status.CRITICAL);

        Thread.sleep(2000);

        List<SyncJsonData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.LOG);

        Assert.assertEquals(0, recordDataList.size());

    }

    /**
     * 插入一条 log 数据测试
     *
     * @throws InterruptedException
     */
    @Test
    public void logInsertDataTest() throws InterruptedException {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true));

        //产生一条日志数据
        String logContent = "----logInsertDataTest----";
        FTLogger.getInstance().logBackground(logContent, Status.CRITICAL);
        //线程池中插入，有一定的时间延迟，这里设置3秒等待时间
        Thread.sleep(2000);
        //从数据库中查询是否有插入的数据
        int except = CheckUtils.getCount(DataType.LOG, logContent, 0);
        Assert.assertEquals(1, except);
    }


    /**
     * 验证 {@link FTLoggerConfig#enableLinkRumData}为 true 时，日志输出是否包含 rum 相关数据
     *
     * @throws InterruptedException
     */
    @Test
    public void logLinkRUMDataEnable() throws InterruptedException {
        Assert.assertTrue(checkLogHasLinkRUMData(true));
    }

    /**
     * 验证 {@link FTLoggerConfig#enableLinkRumData}为 false 时，日志输出是否包含 rum 相关数据
     *
     * @throws InterruptedException
     */
    @Test
    public void logLinkRUMDataDisable() throws InterruptedException {
        Assert.assertFalse(checkLogHasLinkRUMData(false));
    }

    /**
     * 检验 {@link Constants#KEY_RUM_VIEW_ID}, {@link Constants#KEY_RUM_SESSION_ID} 字段来判断是否已经和 RUM 数据进行关联
     *
     * @param enableLinkRumData
     * @return
     * @throws InterruptedException
     */

    private boolean checkLogHasLinkRUMData(boolean enableLinkRumData) throws InterruptedException {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setEnableCustomLog(true).setEnableLinkRumData(enableLinkRumData));


        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTLogger.getInstance().logBackground("test", Status.CRITICAL);
        waitEventConsumeInThreadPool();

        Thread.sleep(2000);

        List<SyncJsonData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.LOG);

        String viewId = "";
        String sessionId = "";
        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject tags = json.optJSONObject("tags");
                String measurement = json.optString("measurement");
                if (Constants.FT_LOG_DEFAULT_MEASUREMENT.equals(measurement)) {
                    if (tags != null) {
                        viewId = tags.optString(Constants.KEY_RUM_VIEW_ID);
                        sessionId = tags.optString(Constants.KEY_RUM_SESSION_ID);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return !viewId.isEmpty() && !sessionId.isEmpty();
    }


}
