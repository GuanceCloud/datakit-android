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

/**
 * RUM user data binding data validation
 *
 * @author Brandon
 */
public class RUMUserBindTest extends FTBaseTest {

    public static final String USER_ID = "123456";
    public static final String IS_SIGNIN_T = "is_signin=T";

    @BeforeClass
    public static void setup() throws Exception {
        avoidCleanData();
        stopSyncTask();
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
    }

    /**
     * Bind user, data validation
     * @throws InterruptedException
     */
    @Test
    public void rumUserBindTest() throws InterruptedException {

        FTSdk.bindRumUserData(USER_ID);
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();
        waitEventConsumeInThreadPool();
        Thread.sleep(2000);
        int except2 = CheckUtils.getCount(DataType.RUM_APP, IS_SIGNIN_T, 0);
        Assert.assertTrue(except2 > 0);
    }

    /**
     * Unbind user data, verify if data is cleaned up
     * @throws InterruptedException
     */
    @Test
    public void rumUserUnBindTest() throws InterruptedException {
        FTSdk.bindRumUserData(USER_ID);
        FTSdk.unbindRumUserData();
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();
        waitEventConsumeInThreadPool();
        Thread.sleep(2000);
        int except4 = CheckUtils.getCount(DataType.RUM_APP, IS_SIGNIN_T, 0);
        Assert.assertEquals(0, except4);
    }

    @Override
    @After
    public void tearDown() {
        FTDBManager.get().delete();
    }

}
