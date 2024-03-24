package com.ft.test.utils;

import com.ft.sdk.FTTrackInner;
import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.powermock.reflect.Whitebox;

import java.util.List;
import java.util.Objects;

/**
 * 测试用例数据校验类
 *
 * @author Brandon
 */
public class CheckUtils {

    /**
     * 检验行协议数据是否存在
     *
     * @param dataType
     * @param checkValues 需要检验数据集
     * @return 数据是否存在，是为存在
     */
    public static boolean checkValueInLineProtocol(DataType dataType, String[] checkValues) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);
        SyncDataHelper syncDataManager = Whitebox.getInternalState(FTTrackInner.getInstance(),"dataHelper");;
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
     * 检验动态参数，针对校验 action view，resource，error， longtask 提交的 property 动态参数
     *
     * @param key
     * @param value
     * @param targetMeasurement 指定指标，{@link com.ft.sdk.garble.utils.Constants#MEASUREMENT}
     * @param isTag             是否是 tag
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
     *
     * @param dataType
     * @param checkValues 需要检验数据集
     * @param limit
     * @return
     */
    public static int getCount(DataType dataType, String[] checkValues, int limit) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(limit, dataType);
        SyncDataHelper syncDataManager = Whitebox.getInternalState(FTTrackInner.getInstance(),"dataHelper");;
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
     *
     * @param dataType
     * @param value    需要
     * @param limit
     * @return 数据是否存在，是为存在
     */
    public static int getCount(DataType dataType, String value, int limit) {
        return getCount(dataType, new String[]{value}, limit);
    }


    /**
     * 检验行协议中的单个数据值
     *
     * @param dataType
     * @param value 需要校验
     * @return 数据是否存在，是为存在
     */
    public static boolean checkValueInLineProtocol(DataType dataType, String value) {
        return checkValueInLineProtocol(dataType, new String[]{value});
    }
}
