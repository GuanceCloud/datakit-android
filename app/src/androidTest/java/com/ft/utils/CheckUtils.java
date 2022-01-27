package com.ft.utils;

import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import java.util.List;

public class CheckUtils {

    /**
     * @param dataType
     * @param checkValues
     * @param limit
     * @return
     */
    public static boolean checkValue(DataType dataType, String[] checkValues, int limit) {
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(limit, dataType);
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
     *
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
     *
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
     * @param dataType
     * @param value
     * @param limit
     * @return
     */
    public static boolean checkValue(DataType dataType, String value, int limit) {
        return checkValue(dataType, new String[]{value}, limit);
    }
}
