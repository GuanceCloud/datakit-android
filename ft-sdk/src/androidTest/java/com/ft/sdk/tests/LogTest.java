package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.LogCacheDiscard;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LogData;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;
import com.ft.test.utils.LineProtocolData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Log output test
 */
public class LogTest extends FTBaseTest {

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL).setDebug(true));
    }

    /**
     * Verify that it is correct without setting {@link FTLoggerConfig}
     *
     * @throws InterruptedException
     */
    @Test
    public void withOutLogConfig() throws InterruptedException {
        List<SyncData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.LOG);

        FTLogger.getInstance().logBackground("test", Status.CRITICAL);
        Thread.sleep(2000);
        Assert.assertEquals(0, recordDataList.size());
    }

    /**
     * Test the situation when the sampling rate is 0
     *
     * @throws InterruptedException
     */
    @Test
    public void logSampleRateZero() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true).setSamplingRate(0));

        Thread.sleep(1000);

        FTLogger.getInstance().logBackground("test", Status.CRITICAL);

        Thread.sleep(2000);

        List<SyncData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.LOG);

        Assert.assertEquals(0, recordDataList.size());

    }

    /**
     * Insert a log data test
     *
     * @throws InterruptedException
     */
    @Test
    public void logInsertDataTest() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true));

        //Generate a log data
        String logContent = "----logInsertDataTest----";
        FTLogger.getInstance().logBackground(logContent, Status.CRITICAL);
        FTLogger.getInstance().logBackground(logContent, Status.WARNING, true);
        HashMap<String, Object> property = new HashMap<>();
        property.put("fakeKey", "fakeValue");
        FTLogger.getInstance().logBackground(logContent, Status.ERROR, property);

        ArrayList<LogData> list = new ArrayList<>();
        list.add(new LogData(logContent, Status.OK));
        list.add(new LogData(logContent, Status.INFO));
        list.add(new LogData(logContent, Status.DEBUG));
        list.add(new LogData(logContent, "custom_status_type"));
        FTLogger.getInstance().logBackground(list);

//        waitLogConsumeInThreadPool();
        //Insert into thread pool, there is a time delay, set 3 seconds waiting time
        Thread.sleep(1000);
        //Query whether there is inserted data from the database
        int except = CheckUtils.getCount(DataType.LOG, logContent, 0);
        Assert.assertEquals(7, except);
    }


    /**
     * Verify that the log output contains rum related data when {@link FTLoggerConfig#enableLinkRumData} is true
     *
     * @throws InterruptedException
     */
    @Test
    public void logLinkRUMDataEnable() throws InterruptedException {
        Assert.assertTrue(checkLogHasLinkRUMData(true));
    }

    /**
     * Verify that the log output contains rum related data when {@link FTLoggerConfig#enableLinkRumData} is false
     *
     * @throws InterruptedException
     */
    @Test
    public void logLinkRUMDataDisable() throws InterruptedException {
        Assert.assertFalse(checkLogHasLinkRUMData(false));
    }

    /**
     * Check whether the log has been associated with RUM data by the {@link Constants#KEY_RUM_VIEW_ID}, {@link Constants#KEY_RUM_SESSION_ID} fields
     *
     * @param enableLinkRumData
     * @return
     * @throws InterruptedException
     */

    private boolean checkLogHasLinkRUMData(boolean enableLinkRumData) throws InterruptedException {
        FTSdk.initRUMWithConfig(new FTRUMConfig());

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setEnableCustomLog(true).setEnableLinkRumData(enableLinkRumData));


        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTLogger.getInstance().logBackground("test", Status.CRITICAL);
        waitEventConsumeInThreadPool();

        Thread.sleep(2000);

        List<SyncData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.LOG);

        String viewId = "";
        String sessionId = "";
        for (SyncData recordData : recordDataList) {
            LineProtocolData lineProtocolData = new LineProtocolData(recordData.getDataString());
            String measurement = lineProtocolData.getMeasurement();
            if (Constants.FT_LOG_DEFAULT_MEASUREMENT.equals(measurement)) {
                viewId = lineProtocolData.getTagAsString(Constants.KEY_RUM_VIEW_ID, "");
                sessionId = lineProtocolData.getTagAsString(Constants.KEY_RUM_SESSION_ID, "");
                break;
            }
        }

        return !viewId.isEmpty() && !sessionId.isEmpty();
    }


    /**
     * Test inserting a large amount of data, whether it triggers the discard policy
     *
     * @throws InterruptedException
     */
    @Test
    public void triggerLogDiscardPolicyTest() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true));
        batchLog(10);

    }

    /**
     * Test inserting a large amount of data, whether it triggers the discard policy
     *
     * @throws InterruptedException
     */
    @Test
    public void triggerLogDiscardOldPolicyTest() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true)
                .setLogCacheDiscardStrategy(LogCacheDiscard.DISCARD_OLDEST));
        batchLog(20);
    }

    /**
     * Batch log
     *
     * @param expectCount Expected number
     * @throws InterruptedException
     */
    private void batchLog(int expectCount) throws InterruptedException {
        String logContent = "custom log";
        FTDBCachePolicy.get().optLogCount(4990);
        for (int i = 0; i < 20; i++) {
            FTLogger.getInstance().logBackground(i + "-" + logContent, Status.CRITICAL);
            Thread.sleep(10);
        }
        Thread.sleep(2000);
        int count = CheckUtils.getCount(DataType.LOG, logContent, 0);

        System.out.println("count=" + count);
        //Thread.sleep(300000);
        Assert.assertTrue(expectCount >= count);
    }


}
