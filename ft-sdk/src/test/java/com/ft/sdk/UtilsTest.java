package com.ft.sdk;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.bean.ObjectBean;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

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

    private static final String TEST_MEASUREMENT_INFLUX_DB_LINE = "TestInfluxDBLine";
    private static final String KEY_TAGS = "tags1";
    private static final String KEY_TAGS_EMPTY = "tagEmpty";
    private static final String KEY_FIELD = "field1";
    private static final String KEY_FIELD_EMPTY = "fieldEmpty";
    private static final String VALUE_TAGS = "tags1-value";
    private static final Object VALUE_TAGS_EMPTY = "";
    private static final String VALUE_FIELD = "field1-value";
    private static final Object VALUE_FIELD_EMPTY = "";
    public static final long VALUE_TIME = 1598512145640000000L;
    private static final String SINGLE_LINE_NORMAL_DATA = TEST_MEASUREMENT_INFLUX_DB_LINE + ","
            + KEY_TAGS + "=" + VALUE_TAGS + " "
            + KEY_FIELD + "=\"" + VALUE_FIELD + "\" " +
            "" + VALUE_TIME + "\n";

    private static final String SINGLE_LINE_EMPTY_DATA = TEST_MEASUREMENT_INFLUX_DB_LINE + ","
            + KEY_TAGS_EMPTY + "=" + Constants.UNKNOWN + " "
            + KEY_FIELD_EMPTY + "=\"" + Constants.UNKNOWN + "\" " +
            "" + VALUE_TIME + "\n";

    private static final String LOG_EXPECT_DATA = SINGLE_LINE_NORMAL_DATA + SINGLE_LINE_NORMAL_DATA + SINGLE_LINE_NORMAL_DATA;


    /**
     * ?????????????????????
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void multiLineProtocolFormatTest() throws Exception {

        SyncJsonData recordData = new SyncJsonData(DataType.LOG);
        recordData.setTime(VALUE_TIME);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.MEASUREMENT, TEST_MEASUREMENT_INFLUX_DB_LINE);
        JSONObject tags = new JSONObject();
        tags.put(KEY_TAGS, VALUE_TAGS);
        JSONObject fields = new JSONObject();
        fields.put(KEY_FIELD, VALUE_FIELD);
        jsonObject.put(Constants.TAGS, tags);
        jsonObject.put(Constants.FIELDS, fields);
        recordData.setDataString(jsonObject.toString());

        List<SyncJsonData> recordDataList = new ArrayList<>();
        recordDataList.add(recordData);
        recordDataList.add(recordData);
        recordDataList.add(recordData);
        String content = Whitebox.invokeMethod(new SyncDataHelper(), "convertToLineProtocolLines",
                recordDataList);
        Assert.assertEquals(LOG_EXPECT_DATA, content.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION)
                .replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK));
    }

    /**
     * ??????????????????????????????
     *
     * @throws JSONException
     */
    @Test
    public void singleLineProtocolFormatTest() throws Exception {
        JSONObject tags = new JSONObject();
        tags.put(KEY_TAGS, VALUE_TAGS);
        JSONObject fields = new JSONObject();
        fields.put(KEY_FIELD, VALUE_FIELD);
        LineProtocolBean trackBean = new LineProtocolBean(TEST_MEASUREMENT_INFLUX_DB_LINE, tags, fields, VALUE_TIME);
        SyncJsonData data = SyncJsonData.getSyncJsonData(DataType.LOG, trackBean);

        List<SyncJsonData> recordDataList = new ArrayList<>();
        recordDataList.add(data);
        String content = Whitebox.invokeMethod(new SyncDataHelper(), "convertToLineProtocolLines",
                recordDataList);

        assertEquals(content.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION)
                .replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK), SINGLE_LINE_NORMAL_DATA);


        JSONObject tagsEmpty = new JSONObject();
        tagsEmpty.put(KEY_TAGS_EMPTY, VALUE_TAGS_EMPTY);
        JSONObject fieldsEmpty = new JSONObject();
        fieldsEmpty.put(KEY_FIELD_EMPTY, VALUE_FIELD_EMPTY);
        LineProtocolBean trackBeanEmpty = new LineProtocolBean(TEST_MEASUREMENT_INFLUX_DB_LINE, tagsEmpty, fieldsEmpty, VALUE_TIME);
        SyncJsonData dataEmpty = SyncJsonData.getSyncJsonData(DataType.LOG, trackBeanEmpty);

        List<SyncJsonData> emptyRecordDataList = new ArrayList<>();
        emptyRecordDataList.add(dataEmpty);
        String contentEmpty = Whitebox.invokeMethod(new SyncDataHelper(), "convertToLineProtocolLines",
                emptyRecordDataList);

        assertEquals(SINGLE_LINE_EMPTY_DATA, contentEmpty.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION)
                .replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK));
    }


}
