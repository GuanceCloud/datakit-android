package com.ft.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * 可以针对某一行进行判断，再决定是否需要替换某一个数值，如果只做某个字段全局替换，请使用 {@link DataModifier}
 */
public interface LineDataModifier {
    /**
     * 对某一行数据进行修改
     *
     * @param measurement 数据指标类型 {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_VIEW}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_ACTION}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_LONG_TASK}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_RESOURCE}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_ERROR}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_LOG_DEFAULT_MEASUREMENT}
     * @param data        原始数据的 key-value 对
     * @return 需要修改的 key-value，（返回 null 或空 map 均为不更改
     */
    Map<String, Object> modify(String measurement, HashMap<String, Object> data);
}
