package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.LineProtocolData;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

/**
 * Data format verification during data transmission
 *
 * @author Brandon
 */
public class DataFormatTest extends FTBaseTest {


    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
    }

    /**
     * During data storage, whether the floating point will become an integer
     *
     * @throws JSONException
     * @throws InterruptedException
     */

    @Test
    public void trackFloatDoubleDataTest() throws Exception {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("floatValue", 0f);
        fields.put("doubleValue", 0d);
        invokeSyncData(DataType.LOG, "TestLog", null, fields);
        Thread.sleep(3000);

        List<SyncData> list = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.LOG);
        Assert.assertFalse(list.isEmpty());
        String content = list.get(0).getDataString();
        LineProtocolData data = new LineProtocolData(content);
        Assert.assertTrue(data.getField("floatValue").toString().contains("."));
        Assert.assertTrue(data.getField("doubleValue").toString().contains("."));
    }


}
