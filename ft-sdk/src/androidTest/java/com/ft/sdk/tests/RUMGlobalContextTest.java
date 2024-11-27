package com.ft.sdk.tests;


import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

/**
 * RUM globalContext 参数检测
 *
 * @author Brandon
 */
@RunWith(AndroidJUnit4.class)
public class RUMGlobalContextTest extends FTBaseTest {


    @BeforeClass
    public static void settingBeforeLaunch() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }

        stopSyncTask();

        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(TEST_FAKE_URL)
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(TEST_FAKE_RUM_ID)
                .addGlobalContext(FTBaseTest.CUSTOM_KEY, FTBaseTest.CUSTOM_VALUE)
                .setEnableTraceUserAction(true)
        );

        HashMap<String, Object> map = new HashMap<>();
        map.put(DYNAMIC_CUSTOM_KEY, DYNAMIC_CUSTOM_VALUE);
        FTSdk.appendRUMGlobalContext(map);
        FTSdk.appendRUMGlobalContext(DYNAMIC_SINGLE_CUSTOM_KEY, DYNAMIC_SINGLE_CUSTOM_VALUE);

    }

    /**
     * 生成 View 数据时，会把  globalContext 添加的数据一起输出
     *
     * @throws Exception
     */
    @Test
    public void globalContextTest() throws Exception {
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();
        waitEventConsumeInThreadPool();
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.RUM_APP,
                new String[]{CUSTOM_KEY, CUSTOM_VALUE, DYNAMIC_CUSTOM_KEY, DYNAMIC_CUSTOM_VALUE,
                        DYNAMIC_SINGLE_CUSTOM_KEY, DYNAMIC_SINGLE_CUSTOM_VALUE}));
    }


}
