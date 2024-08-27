package com.ft.sdk.tests;


import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.Status;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 日志 globalContext 测试
 * @author Brandon
 */
@RunWith(AndroidJUnit4.class)
public class LogGlobalContextTest extends FTBaseTest {


    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL).setDebug(true));
        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .addGlobalContext(CUSTOM_KEY, CUSTOM_VALUE)
                .setEnableCustomLog(true)
        );
    }

    /**
     * 日志输出过程中会把  globalContext 添加的数据一起输出
     */
    @Test
    public void logGlobalContextTest() throws InterruptedException {
        FTLogger.getInstance().logBackground("test Log", Status.INFO);
        Thread.sleep(2000);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.LOG,
                new String[]{CUSTOM_KEY, CUSTOM_VALUE}));

    }
}
