package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.LineProtocolData;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * 传输数据传说过程中，数据格式验证
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
     * 数据存储过程中，浮点型是否会变为整型
     *
     * @throws JSONException
     * @throws InterruptedException
     */

    @Test
    public void trackFloatDoubleDataTest() throws Exception {
        JSONObject fields = new JSONObject();
        fields.put("floatValue", 0f);
        fields.put("doubleValue", 0d);
        invokeSyncData(DataType.LOG, "TestLog", null, fields);
        Thread.sleep(3000);

        List<SyncJsonData> list = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.LOG);
        Assert.assertFalse(list.isEmpty());
        String content = list.get(0).getDataString();
        LineProtocolData data = new LineProtocolData(content);
        Assert.assertTrue(data.getField("floatValue").toString().contains("."));
        Assert.assertTrue(data.getField("doubleValue").toString().contains("."));
    }


}
