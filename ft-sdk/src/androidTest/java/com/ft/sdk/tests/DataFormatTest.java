package com.ft.sdk.tests;

import static com.ft.sdk.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
    public void trackFloatDoubleDataTest() throws JSONException, InterruptedException {
        JSONObject fields = new JSONObject();
        fields.put("floatValue", 0f);
        fields.put("doubleValue", 0d);
        FTTrack.getInstance().trackBackground("TestLog", null, fields);
        Thread.sleep(5000);

        List<SyncJsonData> list = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.TRACK);
        Assert.assertTrue(list.size() > 0);
        String content = list.get(0).getDataString();
        JSONObject json = new JSONObject(content);
        Assert.assertTrue(json.getJSONObject("fields").optString("floatValue").contains("."));
        Assert.assertTrue(json.getJSONObject("fields").optString("doubleValue").contains("."));
    }


}
