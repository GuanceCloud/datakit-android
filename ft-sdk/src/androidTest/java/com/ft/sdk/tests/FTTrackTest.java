package com.ft.sdk.tests;

import com.ft.sdk.FTTrackInner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * BY huangDianHua
 * DATE:2019-12-16 10:17
 * Description:
 */
public class FTTrackTest {

    @Test
    public void isLegalValues() {
        try {
            assertFalse(FTTrackInner.getInstance().isLegalValues(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isLegalValues1() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", "1");
            assertTrue(FTTrackInner.getInstance().isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isLegalValues2() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", 2);
            assertTrue(FTTrackInner.getInstance().isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isLegalValues3() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", true);
            assertTrue(FTTrackInner.getInstance().isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isLegalValues4() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", new JSONObject());
            assertTrue(FTTrackInner.getInstance().isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isLegalValues5() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("subTest","jack");
            jsonObject.put("test", jsonObject1);
            assertTrue(FTTrackInner.getInstance().isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isLegalValues6() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("test",jsonArray);
            assertTrue(FTTrackInner.getInstance().isLegalValues(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}