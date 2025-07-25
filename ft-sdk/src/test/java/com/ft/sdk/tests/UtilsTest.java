package com.ft.sdk.tests;

import static org.junit.Assert.assertEquals;

import com.ft.sdk.FTTrackInner;
import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-16 18:06
 * Description: Miscellaneous tests
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

    /**
     * Normal line protocol data
     */
    private static final String SINGLE_LINE_NORMAL_DATA = TEST_MEASUREMENT_INFLUX_DB_LINE + ","
            + KEY_TAGS + "=" + VALUE_TAGS + " "
            + KEY_FIELD + "=\"" + VALUE_FIELD + "\" " +
            "" + VALUE_TIME + "\n";

    /**
     * Empty line protocol data
     */
    private static final String SINGLE_LINE_EMPTY_DATA = TEST_MEASUREMENT_INFLUX_DB_LINE + " "
            + KEY_FIELD_EMPTY + "=\"\" " +
            "" + VALUE_TIME + "\n";

    /**
     * Merge normal line protocol and empty line protocol data to form three lines of data
     */
    private static final String LOG_EXPECT_DATA = SINGLE_LINE_NORMAL_DATA + SINGLE_LINE_NORMAL_DATA + SINGLE_LINE_NORMAL_DATA;


    /**
     * Multi-line data validation, verify whether data errors occur during the data conversion
     * process of {@link SyncDataHelper#convertToLineProtocolLines(List)}
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void multiLineProtocolFormatTest() throws Exception {

        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.MEASUREMENT, TEST_MEASUREMENT_INFLUX_DB_LINE);
        HashMap<String, Object> tags = new HashMap<>();
        tags.put(KEY_TAGS, VALUE_TAGS);
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(KEY_FIELD, VALUE_FIELD);
        map.put(Constants.TAGS, tags);
        map.put(Constants.FIELDS, fields);

        SyncDataHelper helper = Whitebox.getInternalState(FTTrackInner.getInstance(), "dataHelper");
        LineProtocolBean trackBean = new LineProtocolBean(TEST_MEASUREMENT_INFLUX_DB_LINE, tags, fields, VALUE_TIME);
        SyncData recordData = SyncData.getSyncData(helper, DataType.LOG, trackBean, Utils.getCurrentNanoTime());

        List<SyncData> recordDataList = new ArrayList<>();
        recordDataList.add(recordData);
        recordDataList.add(recordData);
        recordDataList.add(recordData);
        StringBuilder content = new StringBuilder();
        for (SyncData syncData : recordDataList) {
            content.append(syncData.getDataString());
        }
        Assert.assertEquals(LOG_EXPECT_DATA, content.toString().replaceAll("(" +
                Constants.KEY_SDK_DATA_FLAG + "=)(.*),", ""));
    }

    /**
     * Single data validation, whether data errors occur during the data conversion process
     *
     * @throws JSONException
     */
    @Test
    public void singleLineProtocolFormatTest() throws Exception {
        SyncDataHelper helper = Whitebox.getInternalState(FTTrackInner.getInstance(), "dataHelper");
        HashMap<String, Object> tags = new HashMap<>();
        tags.put(KEY_TAGS, VALUE_TAGS);
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(KEY_FIELD, VALUE_FIELD);
        LineProtocolBean trackBean = new LineProtocolBean(TEST_MEASUREMENT_INFLUX_DB_LINE, tags, fields, VALUE_TIME);
        SyncData data = SyncData.getSyncData(helper, DataType.LOG, trackBean, Utils.getCurrentNanoTime());

        String content = data.getDataString();
        content = content.replaceFirst("(" +
                Constants.KEY_SDK_DATA_FLAG + "=)(.*),", "");

        assertEquals(content, SINGLE_LINE_NORMAL_DATA);


        HashMap<String, Object> tagsEmpty = new HashMap<>();
        tagsEmpty.put(KEY_TAGS_EMPTY, VALUE_TAGS_EMPTY);
        HashMap<String, Object> fieldsEmpty = new HashMap<>();
        fieldsEmpty.put(KEY_FIELD_EMPTY, VALUE_FIELD_EMPTY);
        LineProtocolBean trackBeanEmpty = new LineProtocolBean(TEST_MEASUREMENT_INFLUX_DB_LINE, tagsEmpty, fieldsEmpty, VALUE_TIME);
        SyncData dataEmpty = SyncData.getSyncData(helper, DataType.LOG, trackBeanEmpty, Utils.getCurrentNanoTime());

        String contentEmpty = dataEmpty.getDataString();
        contentEmpty = contentEmpty.replaceFirst(",\\s?" + Constants.KEY_SDK_DATA_FLAG + "=[\\w\\d]+", "");
        assertEquals(SINGLE_LINE_EMPTY_DATA, contentEmpty);
    }


}