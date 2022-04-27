package com.ft.sdk.tests;

import static com.ft.sdk.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
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

@RunWith(AndroidJUnit4.class)
public class SDKGlobalContextTest extends FTBaseTest {


    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();

        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(TEST_FAKE_URL)
                .setDebug(true)//设置是否是 debug
                .addGlobalContext(CUSTOM_KEY, CUSTOM_VALUE)
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(TEST_FAKE_RUM_ID)
                .setEnableTraceUserAction(true)
        );

        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true));
    }

    @Test
    public void rumGlobalContextTest() throws Exception {
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();
        waitForInThreadPool();
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkValue(DataType.RUM_APP,
                new String[]{CUSTOM_KEY, CUSTOM_VALUE}, 0));
    }

    @Test
    public void logGlobalContextTest() throws Exception {
        FTLogger.getInstance().logBackground("log test", Status.INFO);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkValue(DataType.LOG,
                new String[]{CUSTOM_KEY, CUSTOM_VALUE}, 0));

    }


}
