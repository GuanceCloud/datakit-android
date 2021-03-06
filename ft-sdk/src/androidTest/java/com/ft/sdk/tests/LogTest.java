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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class LogTest extends FTBaseTest {

    @Before
    public void setUp() throws Exception {
        stopSyncTask();
    }

    @Test
    public void logSampleRateZero() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true).setSamplingRate(0));

        Thread.sleep(2000);

        FTLogger.getInstance().logBackground("test", Status.CRITICAL);

        Thread.sleep(5000);

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
        //线程池中插入，有一定的时间延迟，这里设置5秒等待时间
        Thread.sleep(5000);
        //从数据库中查询是否有插入的数据
        int except = CheckUtils.getCount(DataType.LOG, logContent, 0);
        Assert.assertEquals(1, except);
    }


    @Test
    public void logLinkRUMDataEnable() throws InterruptedException {
        Assert.assertTrue(checkLogHasLinkRUMData(true));
    }

    @Test
    public void logLinkRUMDataDisable() throws InterruptedException {
        Assert.assertFalse(checkLogHasLinkRUMData(false));
    }

    private boolean checkLogHasLinkRUMData(boolean enableLinkRumData) throws InterruptedException {
        FTSdk.initRUMWithConfig(new FTRUMConfig());

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setEnableCustomLog(true).setEnableLinkRumData(enableLinkRumData));


        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTLogger.getInstance().logBackground("test", Status.CRITICAL);
        waitForInThreadPool();

        Thread.sleep(5000);

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
                        viewId = tags.optString("view_id");
                        sessionId = tags.optString("session_id");
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
