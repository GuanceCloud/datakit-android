package com.ft.sdk.tests;


import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.test.base.FTBaseTest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SDKDataClearTest extends FTBaseTest {
    @BeforeClass
    public static void setup() throws Exception {
        stopSyncTask();
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true));
    }

    @Test
    public void dataClearTest() throws InterruptedException {
        FTLogger.getInstance().logBackground("log data", Status.ERROR);
        FTRUMGlobalManager.get().addError("log count", "", "custom");
        Thread.sleep(1000);

        int logCount, rumCount;
        logCount = FTDBManager.get().queryTotalCount(DataType.LOG);
        Assert.assertEquals(1, logCount);

        rumCount = FTDBManager.get().queryTotalCount(DataType.RUM_APP);
        Assert.assertEquals(1, rumCount);

        FTSdk.clearAllData();

        logCount = FTDBManager.get().queryTotalCount(DataType.LOG);
        Assert.assertEquals(0, logCount);

        rumCount = FTDBManager.get().queryTotalCount(DataType.RUM_APP);
        Assert.assertEquals(0, rumCount);

    }

}
