package com.ft.sdk;

import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * 对行协议传输数据进行特殊字符转化校验
 */
public class LineProtocolEscapeTest {

    private final static String NEED_ESCAPE_STRING = "\"Test log\\,\\\\=";

    private final static String FIELD_RESULT = "\"\\\"Test log\\\\,\\\\\\\\=\"";
    private final static String MEASUREMENT_RESULT = "\"Test\\ log\\\\,\\\\=";
    private final static String TAG_RESULT = "\"Test\\ log\\\\,\\\\\\=";

    private final static String JSON_STRING = "{\"key\":\"value\",\"jsonString\":\"{\\\"key\\\":\\\"value\\\"}\"}";
    private final static String MIX_FIELD = JSON_STRING + NEED_ESCAPE_STRING;
    private final static String MIXED_RESULT = "\"{\\\"key\\\":\\\"value\\\",\\\"jsonString\\\":\\\"{\\\\\\\"key\\\\\\\":\\\\\\\"value\\\\\\\"}\\\"}\\\"Test log\\\\,\\\\\\\\=\"";


    @Test
    public void fieldEscape() {
        String fieldValue = Utils.translateFieldValue(NEED_ESCAPE_STRING);
        Assert.assertEquals(FIELD_RESULT, fieldValue);
    }

    @Test
    public void fieldEscapeJson() {
        String fieldValue = Utils.translateFieldValue(JSON_STRING);
        Assert.assertEquals(JSONObject.quote(JSON_STRING), fieldValue);
    }

    @Test
    public void fieldEscapeMixString() {
        String fieldValue = Utils.translateFieldValue(MIX_FIELD);
        Assert.assertEquals(MIXED_RESULT, fieldValue);
        System.out.println(MIX_FIELD);
        System.out.println(MIXED_RESULT);
    }

    @Test
    public void measurementEscape() {
        String measureValue = Utils.translateMeasurements(NEED_ESCAPE_STRING);
        Assert.assertEquals(MEASUREMENT_RESULT, measureValue);
    }

    @Test
    public void tagEscape() {
        String tagValue = Utils.translateTagKeyValue(NEED_ESCAPE_STRING);
        Assert.assertEquals(TAG_RESULT, tagValue);
    }


}
