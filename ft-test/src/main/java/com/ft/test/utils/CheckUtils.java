package com.ft.test.utils;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;

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
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);
        StringBuilder body = new StringBuilder();
        for (SyncData data : recordDataList) {
            body.append(data.getDataString());
        }
        boolean result = false;
        for (String item : checkValues) {
            boolean contain = body.toString().contains(item);
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
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, dataType);

        for (SyncData recordData : recordDataList) {
            LineProtocolData lineProtocolData = new LineProtocolData(recordData.getDataString());
            String measurement = lineProtocolData.getMeasurement();
            if (isTag) {
                if (targetMeasurement.equals(measurement)) {
                    return Objects.equals(lineProtocolData.getTagAsString(key), value);
                }
            } else {
                if (targetMeasurement.equals(measurement)) {
                    return Objects.equals(lineProtocolData.getFieldAsString(key), value);
                }
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
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(limit, dataType);
        StringBuilder body = new StringBuilder();
        for (SyncData syncData : recordDataList) {
            body.append(syncData.getDataString());
        }
        int count = 0;
        for (String item : checkValues) {
            String[] lines = body.toString().split("\n");
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
     * @param value    需要校验
     * @return 数据是否存在，是为存在
     */
    public static boolean checkValueInLineProtocol(DataType dataType, String value) {
        return checkValueInLineProtocol(dataType, new String[]{value});
    }
}
