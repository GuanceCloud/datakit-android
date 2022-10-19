package com.ft.sdk.tests;

import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RUMUserBindTest extends FTBaseTest {

    @BeforeClass
    public static void setup() throws Exception {
        avoidCleanData();
        stopSyncTask();
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());
    }

    @Test
    public void rumUserBindTest() throws InterruptedException {

        FTSdk.bindRumUserData("123456");
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();
        waitForInThreadPool();
        Thread.sleep(5000);
        int except2 = CheckUtils.getCount(DataType.RUM_APP, "is_signin=T", 0);
        Assert.assertTrue(except2 > 0);
    }

    @Test
    public void rumUserUnBindTest() throws InterruptedException {
        FTSdk.bindRumUserData("123456");
        FTSdk.unbindRumUserData();
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();
        waitForInThreadPool();
        Thread.sleep(5000);
        int except4 = CheckUtils.getCount(DataType.RUM_APP, "is_signin=T", 0);
        Assert.assertEquals(0, except4);
    }

    @Override
    @After
    public void tearDown() {
        FTDBManager.get().delete();
    }

}
