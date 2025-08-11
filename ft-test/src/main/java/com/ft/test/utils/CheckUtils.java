package com.ft.test.utils;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;

import java.util.List;
import java.util.Objects;

/**
 * Test case data validation class
 *
 * @author Brandon
 */
public class CheckUtils {

    /**
     * Check if line protocol data exists
     *
     * @param dataType
     * @param checkValues Dataset to be validated
     * @return Whether data exists, true if exists
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
     * Validate dynamic parameters, specifically for validating dynamic property 
     * parameters submitted by action view, resource, error, longtask
     *
     * @param key
     * @param value
     * @param targetMeasurement Specified measurement, {@link com.ft.sdk.garble.utils.Constants#MEASUREMENT}
     * @param isTag             Whether it is a tag
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
     * Get data count
     *
     * @param dataType
     * @param checkValues Dataset to be validated
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
     * Get data count
     *
     * @param dataType
     * @param value    Required
     * @param limit
     * @return Whether data exists, true if exists
     */
    public static int getCount(DataType dataType, String value, int limit) {
        return getCount(dataType, new String[]{value}, limit);
    }


    /**
     * Validate single data value in line protocol
     *
     * @param dataType
     * @param value    Value to be validated
     * @return Whether data exists, true if exists
     */
    public static boolean checkValueInLineProtocol(DataType dataType, String value) {
        return checkValueInLineProtocol(dataType, new String[]{value});
    }
}
