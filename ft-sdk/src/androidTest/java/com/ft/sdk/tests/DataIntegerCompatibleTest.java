package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

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

import java.util.HashMap;

/**
 * Test the effect of enabling/disabling {@link com.ft.sdk.FTSDKConfig#enableDataIntegerCompatible()}
 */
public class DataIntegerCompatibleTest extends FTBaseTest {

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
    }

    /**
     * Default, expected int type, displayed as 1i
     * @throws InterruptedException
     */

    @Test
    public void defaultConfig() throws InterruptedException {
        FTSdk.install(getDatakitConfig());
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true));
        HashMap<String, Object> property = new HashMap<>();
        property.put("integer_value", 1);
        FTLogger.getInstance().logBackground("content", Status.ERROR, property);
        Thread.sleep(2000);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.LOG, "1i"));

    }

    /**
     * After enabling, int type is displayed as
     * @throws InterruptedException
     */
    @Test
    public void enableCompatible() throws InterruptedException {
        FTSdk.install(getDatakitConfig().enableDataIntegerCompatible());
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true));
        HashMap<String, Object> property = new HashMap<>();
        property.put("integer_value", 1);
        FTLogger.getInstance().logBackground("content", Status.ERROR, property);
        Thread.sleep(2000);
        Assert.assertFalse(CheckUtils.checkValueInLineProtocol(DataType.LOG, "1i"));

    }


}
