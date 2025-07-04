package com.ft.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * You can judge a specific line and then decide whether to replace a value. If you only need to globally replace a certain field, please use {@link DataModifier}
 */
public interface LineDataModifier {
    /**
     * Modify a specific line of data
     *
     * @param measurement Data metric type {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_VIEW}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_ACTION}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_LONG_TASK}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_RESOURCE}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_RUM_ERROR}
     *                    {@link com.ft.sdk.garble.utils.Constants#FT_LOG_DEFAULT_MEASUREMENT}
     * @param data        Key-value pairs of the original data, if value is null, do not modify
     * @return Key-value pairs to be modified (returning null or an empty map means no modification)
     */
    Map<String, Object> modify(String measurement, HashMap<String, Object> data);
}
