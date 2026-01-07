package com.ft.sdk.tests;

import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Special character conversion verification for line protocol transmission data
 *
 * @author Brandon
 */
public class LineProtocolEscapeTest {

    private final static String NEED_ESCAPE_STRING = "\"Test log\\,\\\\=";

    private final static String FIELD_RESULT = "\"\\\"Test log\\\\,\\\\\\\\=\"";
    private final static String MEASUREMENT_RESULT = "\"Test\\ log\\,=";
    private final static String TAG_RESULT = "\"Test\\ log\\,\\=";

    private final static String JSON_STRING = "{\"key\":\"value\",\"jsonString\":\"{\\\"key\\\":\\\"value\\\"}\"}";
    private final static String MIX_FIELD = JSON_STRING + NEED_ESCAPE_STRING;
    private final static String MIXED_RESULT = "\"{\\\"key\\\":\\\"value\\\",\\\"jsonString\\\":\\\"{\\\\\\\"key\\\\\\\":\\\\\\\"value\\\\\\\"}\\\"}\\\"Test log\\\\,\\\\\\\\=\"";

    /**
     * Whether special characters in field are correctly escaped in line protocol
     */
    @Test
    public void fieldEscape() {
        String fieldValue = Utils.translateFieldValue(NEED_ESCAPE_STRING);
        Assert.assertEquals(FIELD_RESULT, fieldValue);
    }

    /**
     * Whether field in json format is correctly escaped in line protocol
     */
    @Test
    public void fieldEscapeJson() {
        String fieldValue = Utils.translateFieldValue(JSON_STRING);
        Assert.assertEquals(JSONObject.quote(JSON_STRING), fieldValue);
    }

    /**
     * Whether field is correctly escaped in mixed data in line protocol
     */
    @Test
    public void fieldEscapeMixString() {
        String fieldValue = Utils.translateFieldValue(MIX_FIELD);
        Assert.assertEquals(MIXED_RESULT, fieldValue);
        System.out.println(MIX_FIELD);
        System.out.println(MIXED_RESULT);
    }

    /**
     * Whether special characters in measurement are correctly escaped in line protocol
     */
    @Test
    public void measurementEscape() {
        String measureValue = Utils.translateMeasurements(NEED_ESCAPE_STRING);
        Assert.assertEquals(MEASUREMENT_RESULT, measureValue);
    }
    /**
     * Whether special characters in tag are correctly escaped in line protocol
     */
    @Test
    public void tagEscape() {
        String tagValue = Utils.translateTagKeyValue(NEED_ESCAPE_STRING);
        Assert.assertEquals(TAG_RESULT, tagValue);
    }


}
