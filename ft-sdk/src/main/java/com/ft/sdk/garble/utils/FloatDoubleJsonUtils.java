package com.ft.sdk.garble.utils;

import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * 做 Json 中 float 数值保护使用
 *
 * @author Brandon
 */
public class FloatDoubleJsonUtils {

    private static final String TAG = "[FT-SDK]FloatDoubleJsonUtils";

    /**
     * 转化在 json中 float 的数值，避免在转化过程中被简化,例如 1.0 会转化成 "1.0"，避免 "1.0" 被简化为 1，
     * 这样会导致行协议上报错误
     */
    public static String protectValueFormat(JSONObject json) {

        try {
            return new GsonBuilder().serializeNulls().create().toJson(toMap(json));
        } catch (JSONException e) {
            LogUtils.e(TAG, e.getMessage());
            return "{}";
        }

    }


    /**
     *  JSON 单个对象类型转化
     * @param jsonobj
     * @return
     * @throws JSONException
     */
    private static Map<String, Object> toMap(JSONObject jsonobj) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonobj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    /**
     * JSON 数组类型转化
     * @param array
     * @return
     * @throws JSONException
     */
    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
