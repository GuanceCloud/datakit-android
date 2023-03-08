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
 * RUM 用户数据绑定数据验证
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
        FTSdk.initRUMWithConfig(new FTRUMConfig());
    }

    /**
     * 绑定用户，数据验证
     * @throws InterruptedException
     */
    @Test
    public void rumUserBindTest() throws InterruptedException {

        FTSdk.bindRumUserData(USER_ID);
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();
        waitForInThreadPool();
        Thread.sleep(2000);
        int except2 = CheckUtils.getCount(DataType.RUM_APP, IS_SIGNIN_T, 0);
        Assert.assertTrue(except2 > 0);
    }

    /**
     * 解绑用户数据，验证是否清理数据
     * @throws InterruptedException
     */
    @Test
    public void rumUserUnBindTest() throws InterruptedException {
        FTSdk.bindRumUserData(USER_ID);
        FTSdk.unbindRumUserData();
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();
        waitForInThreadPool();
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
