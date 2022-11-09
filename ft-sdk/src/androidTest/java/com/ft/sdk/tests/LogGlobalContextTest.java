package com.ft.sdk.tests;


import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
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
public class LogGlobalContextTest extends FTBaseTest {


    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .addGlobalContext(CUSTOM_KEY, CUSTOM_VALUE)
                .setEnableCustomLog(true)
        );
    }

    @Test
    public void logGlobalContextTest() {
        FTLogger.getInstance().logBackground("test Log", Status.INFO);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.LOG,
                new String[]{CUSTOM_KEY, CUSTOM_VALUE}));

    }
}
