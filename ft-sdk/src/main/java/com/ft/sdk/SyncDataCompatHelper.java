package com.ft.sdk;

import static com.ft.sdk.garble.utils.Constants.KEY_SDK_DATA_FLAG;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Used to convert old format data
 */
public class SyncDataCompatHelper {

    public final static String TAG = Constants.LOG_TAG_PREFIX + "SyncDataCompatHelper";

    protected HashMap<String, Object> logTags;
    protected HashMap<String, Object> rumTags;

    protected FTSDKConfig config;

    public SyncDataCompatHelper(HashMap<String, Object> logTags,
                                HashMap<String, Object> rumTags, FTSDKConfig config) {
        this.logTags = logTags;
        this.rumTags = rumTags;
        this.config = config;
    }


    /**
     * Encapsulate the data to be synchronously uploaded
     *
     * @return
     */
    public String getBodyContent(JSONObject json, DataType dataType, String uuid, long timeStamp) {
        HashMap<String, Object> hashMap = new LinkedHashMap<>();
        hashMap.put(KEY_SDK_DATA_FLAG, uuid);//Put uuid in the first position to save cost during string replacement
        if (dataType == DataType.LOG) {
            hashMap.putAll(logTags);
        } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_WEBVIEW) {
            hashMap.putAll(rumTags);
        }
        return convertToLineProtocolLine(json, timeStamp, hashMap);
    }

    /**
     * Convert to a single line protocol data
     *
     * @param opJson
     * @param mergeTags
     * @return
     */
    private String convertToLineProtocolLine(JSONObject opJson, long timeStamp, HashMap<String, Object> mergeTags) {
        try {
            String measurement = opJson.optString(Constants.MEASUREMENT);
            JSONObject tags = opJson.optJSONObject(Constants.TAGS);
            JSONObject fields = opJson.optJSONObject(Constants.FIELDS);

            HashMap<String, Object> jsonTags = Utils.jsonToMap(tags);
            if (jsonTags != null) {
                mergeTags.putAll(jsonTags);
            }

            return SyncDataHelper.convertToLineProtocolLine(measurement, mergeTags, Utils.jsonToMap(fields), timeStamp, config);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return "";
    }
}
