package com.ft.sdk;

import static com.ft.sdk.garble.utils.Constants.KEY_SDK_DATA_FLAG;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * 用于转化旧格式数据
 */
public class SyncDataCompatHelper  {

    public final static String TAG = Constants.LOG_TAG_PREFIX + "SyncDataCompatHelper";

    protected HashMap<String, Object> logTags;
    protected HashMap<String, Object> rumTags;
    protected HashMap<String, Object> traceTags;

    protected FTSDKConfig config;

    public SyncDataCompatHelper(HashMap<String, Object> logTags,
                                HashMap<String, Object> traceTags,
                                HashMap<String, Object> rumTags, FTSDKConfig config) {
        this.logTags = logTags;
        this.traceTags = traceTags;
        this.rumTags = rumTags;
        this.config = config;
    }


    /**
     * 封装同步上传的数据
     *
     * @return
     */
    public String getBodyContent(JSONObject json, DataType dataType, String uuid, long timeStamp) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(KEY_SDK_DATA_FLAG, uuid);
        if (dataType == DataType.LOG) {
            hashMap.putAll(logTags);
        } else if (dataType == DataType.TRACE) {
            hashMap.putAll(traceTags);
        } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_WEBVIEW) {
            hashMap.putAll(rumTags);
        }
        return convertToLineProtocolLine(json, timeStamp, hashMap);
    }

    /**
     * 转化为单条行协议数据
     *
     * @param opJson
     * @param extraTags
     * @return
     */
    private String convertToLineProtocolLine(JSONObject opJson, long timeStamp, HashMap<String, Object> extraTags) {
        try {
            String measurement = opJson.optString(Constants.MEASUREMENT);
            JSONObject tags = opJson.optJSONObject(Constants.TAGS);
            JSONObject fields = opJson.optJSONObject(Constants.FIELDS);
            return SyncDataHelper.convertToLineProtocolLine(measurement, tags, fields, extraTags, timeStamp,config);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return "";
    }
}
