package com.ft.sdk.tests;

import static org.junit.Assert.assertEquals;

import com.ft.sdk.FTTrackInner;
import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.utils.Constants;

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
 * Description:杂项测试
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
     * 正常行协议数据
     */
    private static final String SINGLE_LINE_NORMAL_DATA = TEST_MEASUREMENT_INFLUX_DB_LINE + ","
            + KEY_TAGS + "=" + VALUE_TAGS + " "
            + KEY_FIELD + "=\"" + VALUE_FIELD + "\" " +
            "" + VALUE_TIME + "\n";

    /**
     * 空行协议数据
     */
    private static final String SINGLE_LINE_EMPTY_DATA = TEST_MEASUREMENT_INFLUX_DB_LINE + " "
            + KEY_FIELD_EMPTY + "=\"\" " +
            "" + VALUE_TIME + "\n";

    /**
     * 合并正常行协议和空行协议数据，形成三行数据
     */
    private static final String LOG_EXPECT_DATA = SINGLE_LINE_NORMAL_DATA + SINGLE_LINE_NORMAL_DATA + SINGLE_LINE_NORMAL_DATA;


    /**
     * 多行行数据验证，验证 {@link SyncDataHelper#convertToLineProtocolLines(List)} 数据转化过程中是否会发生数据错误
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void multiLineProtocolFormatTest() throws Exception {

        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.MEASUREMENT, TEST_MEASUREMENT_INFLUX_DB_LINE);
        HashMap<String,Object> tags = new HashMap<>();
        tags.put(KEY_TAGS, VALUE_TAGS);
        HashMap<String,Object> fields = new HashMap<>();
        fields.put(KEY_FIELD, VALUE_FIELD);
        map.put(Constants.TAGS, tags);
        map.put(Constants.FIELDS, fields);

        SyncDataHelper helper = Whitebox.getInternalState(FTTrackInner.getInstance(), "dataHelper");
        LineProtocolBean trackBean = new LineProtocolBean(TEST_MEASUREMENT_INFLUX_DB_LINE, tags, fields, VALUE_TIME);
        SyncJsonData recordData = SyncJsonData.getSyncJsonData(helper, DataType.LOG, trackBean);

        List<SyncJsonData> recordDataList = new ArrayList<>();
        recordDataList.add(recordData);
        recordDataList.add(recordData);
        recordDataList.add(recordData);
        StringBuilder content = new StringBuilder();
        for (SyncJsonData syncJsonData : recordDataList) {
            content.append(syncJsonData.getDataString());
        }
        Assert.assertEquals(LOG_EXPECT_DATA, content.toString());
    }

    /**
     * 单数据验证, 数据转化过程中是否会发生数据错误
     *
     * @throws JSONException
     */
    @Test
    public void singleLineProtocolFormatTest() throws Exception {
        SyncDataHelper helper = Whitebox.getInternalState(FTTrackInner.getInstance(), "dataHelper");
        HashMap<String,Object> tags = new HashMap<>();
        tags.put(KEY_TAGS, VALUE_TAGS);
        HashMap<String,Object> fields = new HashMap<>();
        fields.put(KEY_FIELD, VALUE_FIELD);
        LineProtocolBean trackBean = new LineProtocolBean(TEST_MEASUREMENT_INFLUX_DB_LINE, tags, fields, VALUE_TIME);
        SyncJsonData data = SyncJsonData.getSyncJsonData(helper, DataType.LOG, trackBean);

        String content = data.getDataString();

        assertEquals(content, SINGLE_LINE_NORMAL_DATA);


        HashMap<String,Object> tagsEmpty = new HashMap<>();
        tagsEmpty.put(KEY_TAGS_EMPTY, VALUE_TAGS_EMPTY);
        HashMap<String,Object> fieldsEmpty = new HashMap<>();
        fieldsEmpty.put(KEY_FIELD_EMPTY, VALUE_FIELD_EMPTY);
        LineProtocolBean trackBeanEmpty = new LineProtocolBean(TEST_MEASUREMENT_INFLUX_DB_LINE, tagsEmpty, fieldsEmpty, VALUE_TIME);
        SyncJsonData dataEmpty = SyncJsonData.getSyncJsonData(helper, DataType.LOG, trackBeanEmpty);

        String contentEmpty = dataEmpty.getDataString();

        assertEquals(SINGLE_LINE_EMPTY_DATA, contentEmpty);
    }


}
