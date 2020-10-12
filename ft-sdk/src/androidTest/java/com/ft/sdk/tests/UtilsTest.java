package com.ft.sdk.tests;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.manager.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * BY huangDianHua
 * DATE:2019-12-16 18:06
 * Description:
 */
public class UtilsTest {
    private Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void isNetworkAvailable() {
        assertTrue(Utils.isNetworkAvailable(getContext()));
    }

    @Test
    public void contentMD5Encode() {
        assertEquals("M1QEWjl2Ic2SQG8fmM3ikg==", Utils.contentMD5Encode("1122334455"));
    }

    @Test
    public void getHMacSha1() {
        //String value = Utils.getHMacSha1("screct", "POST" + "\n" + "xrKOMvb4g+/lSVHoW8XcaA==" + "\n" + "application/json" + "\n" + "Wed, 02 Sep 2020 09:41:24 GMT");
        assertEquals("4me5NXJallTGFmZiO3csizbWI90=", Utils.getHMacSha1("screct", "123456"));
    }

    private String expectData = "TestInfluxDBLine,tags1=tags1-value field1=\"field1-value\" 1598512145640000000\nTestInfluxDBLine,tags1=tags1-value field1=\"field1-value\" 1598512145640000000\nTestInfluxDBLine,tags1=tags1-value field1=\"field1-value\" 1598512145640000000\n";

    /**
     * 验证influx 行协议数据格式是否正确
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void influxDBTest() throws JSONException, InterruptedException {

        SyncJsonData recordData = new SyncJsonData(DataType.LOG);
        recordData.setTime(1598512145640L);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.MEASUREMENT, "TestInfluxDBLine");
        JSONObject tags = new JSONObject();
        tags.put("tags1", "tags1-value");
        JSONObject fields = new JSONObject();
        fields.put("field1", "field1-value");
        jsonObject.put(Constants.TAGS, tags);
        jsonObject.put(Constants.FIELDS, fields);
        recordData.setDataString(jsonObject.toString());
        List<SyncJsonData> recordDataList = new ArrayList<>();
        recordDataList.add(recordData);
        recordDataList.add(recordData);
        recordDataList.add(recordData);
        String content = new SyncDataHelper().getBodyContent(DataType.LOG, recordDataList);
        String realData = content.replaceAll(Constants.SEPARATION_PRINT, " ");
        realData = realData.replaceAll(Constants.SEPARATION_LINE_BREAK, "\n");
        Assert.assertEquals(expectData, realData);
    }
}
