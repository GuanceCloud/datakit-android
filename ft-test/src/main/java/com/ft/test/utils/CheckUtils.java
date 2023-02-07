package com.ft.test.utils;

import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class CheckUtils {

    /**
     * @param dataType
     * @param checkValues
     * @return
     */
    public static boolean checkValueInLineProtocol(DataType dataType, String[] checkValues) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);
        SyncDataHelper syncDataManager = new SyncDataHelper();
        String body = syncDataManager.getBodyContent(dataType, recordDataList);
        boolean result = false;
        for (String item : checkValues) {
            boolean contain = body.contains(item);
            if (!contain) {
                result = false;
                break;
            } else {
                result = true;
            }
        }
        return result;
    }



    /**
     * @param key
     * @param value
     * @param targetMeasurement
     * @param isTag
     * @return
     * @throws NullPointerException
     */
    public static boolean checkDynamicValue(String key, String value, String targetMeasurement,
                                            DataType dataType, boolean isTag) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);

        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                if (isTag) {
                    JSONObject tags = json.optJSONObject("tags");
                    String measurement = json.optString("measurement");
                    if (targetMeasurement.equals(measurement)) {
                        if (tags != null) {
                            return Objects.equals(tags.opt(key), value);
                        }
                    }
                } else {
                    JSONObject fields = json.optJSONObject("fields");
                    String measurement = json.optString("measurement");
                    if (targetMeasurement.equals(measurement)) {
                        if (fields != null) {
                            return Objects.equals(fields.opt(key), value);
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取数据数量
     * @param dataType
     * @param checkValues
     * @param limit
     * @return
     */
    public static int getCount(DataType dataType, String[] checkValues, int limit) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(limit, dataType);
        SyncDataHelper syncDataManager = new SyncDataHelper();
        String body = syncDataManager.getBodyContent(dataType, recordDataList);
        int count = 0;
        for (String item : checkValues) {
            String[] lines = body.split("\n");
            for (String line : lines) {
                if (line.contains(item)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 获取数据数量
     * @param dataType
     * @param value
     * @param limit
     * @return
     */
    public static int getCount(DataType dataType, String value, int limit) {
        return getCount(dataType, new String[]{value}, limit);
    }


    /**
     *
     * 检验行协议中的数据值
     * @param dataType
     * @param value
     * @return
     */
    public static boolean checkValueInLineProtocol(DataType dataType, String value) {
        return checkValueInLineProtocol(dataType, new String[]{value});
    }
}
