package com.ft.sdk.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

/**
 * BY huangDianHua
 * DATE:2019-12-16 10:17
 * Description:非法字符校验
 */
public class StoreDataFormatTest {

    /**
     * 空数据合法性验证
     */
    @Test
    public void isLegalValues() {
        try {
            assertFalse(isLegalValues(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 字符类型数据合法性验证
     */
    @Test
    public void isLegalValues1() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", "1");
            assertTrue(isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * json 对象数据合法性验证
     */
    @Test
    public void isLegalValues2() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", 2);
            assertTrue(isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 布尔类型数据合法性验证
     */

    @Test
    public void isLegalValues3() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", true);
            assertTrue(isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isLegalValues4() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", new JSONObject());
            assertTrue(isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * json 对象数据合法性验证
     */
    @Test
    public void isLegalValues5() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("subTest", "jack");
            jsonObject.put("test", jsonObject1);
            assertTrue(isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * jsonarr 数据合法性验证
     */
    @Test
    public void isLegalValues6() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("test", jsonArray);
            assertTrue(isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断是否是合法的Values
     *
     * @param jsonObject
     * @return
     */
    public boolean isLegalValues(JSONObject jsonObject) {
        if (jsonObject == null) {
            return false;
        }
        if (jsonObject.keys().hasNext()) {
            return true;
        } else {
            return false;
        }
    }
}