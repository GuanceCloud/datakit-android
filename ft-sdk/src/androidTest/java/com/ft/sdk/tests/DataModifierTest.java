package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.DataModifier;
import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.LineDataModifier;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.LineProtocolData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data modifier test
 */
public class DataModifierTest extends FTBaseTest {
    private final String MASK = "xxx";

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();

        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL)
                .setDebug(true).setDataModifier(new DataModifier() {

                    @Override
                    public Object modify(String key, Object value) {
                        if (key.equals(Constants.KEY_DEVICE_UUID)) {
                            return MASK;
                        }
                        return null;
                    }
                }).setLineDataModifier(new LineDataModifier() {
                    @Override
                    public Map<String, Object> modify(String measurement, HashMap<String, Object> data) {
                        data.put(Constants.KEY_RUM_USER_ID, MASK);
                        return data;
                    }
                }));

        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true)
                .setEnableLinkRumData(true));

        FTSdk.bindRumUserData(TEST_FAKE_USER_ID);
    }

    /**
     * RUM app data test
     *
     * @throws InterruptedException
     */
    @Test
    public void rumAppDataTest() throws InterruptedException {
        FTRUMGlobalManager.get().addAction("test", "test_action");

        Thread.sleep(2000);
        List<SyncData> list = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        LineProtocolData parser = new LineProtocolData(list.get(0).getDataString());

        Assert.assertEquals(parser.getTagAsString(Constants.KEY_DEVICE_UUID), MASK);
        Assert.assertEquals(parser.getTagAsString(Constants.KEY_RUM_USER_ID), MASK);
    }

    /**
     * Log data test
     *
     * @throws InterruptedException
     */
    @Test
    public void logDataTest() throws InterruptedException {
        FTLogger.getInstance().logBackground(LOG_TEST_DATA_1_KB, Status.DEBUG);
        Thread.sleep(3000);

        waitEventConsumeInThreadPool();
        List<SyncData> list = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.LOG);
        Assert.assertFalse(list.isEmpty());
        String content = list.get(0).getDataString();
        LineProtocolData parser = new LineProtocolData(content);
        Assert.assertEquals(parser.getTagAsString(Constants.KEY_DEVICE_UUID), MASK);
        Assert.assertEquals(parser.getTagAsString(Constants.KEY_RUM_USER_ID), MASK);
    }
}
