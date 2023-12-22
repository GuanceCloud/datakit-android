package com.ft.sdk;

import static com.ft.sdk.garble.utils.Constants.FT_KEY_VALUE_NULL;

import android.util.Log;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.StringUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-11 14:48
 * Description:
 */
public class SyncDataHelper {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "SyncDataHelper";

    private final HashMap<String, Object> basePublicTags;
    private final HashMap<String, Object> logTags;
    private final HashMap<String, Object> rumTags;
    private final HashMap<String, Object> traceTags;


    public SyncDataHelper() {
        basePublicTags = FTSdk.checkInstallState() ? FTSdk.get().getBasePublicTags() : new HashMap<>();
        logTags = FTLoggerConfigManager.get().getConfig() != null ? FTLoggerConfigManager.get().getConfig().getGlobalContext() : new HashMap<>();
        rumTags = FTRUMConfigManager.get().getConfig() != null ? FTRUMConfigManager.get().getConfig().getGlobalContext() : new HashMap<>();
        traceTags = FTTraceConfigManager.get().getConfig() != null ? FTTraceConfigManager.get().getConfig().getGlobalContext() : new HashMap<>();
    }

    /**
     * 封装同步上传的数据
     *
     * @param dataType
     * @param recordDatas
     * @return
     */
    public String getBodyContent(DataType dataType, List<SyncJsonData> recordDatas) {

        String bodyContent;
        if (dataType == DataType.LOG) {
            bodyContent = getLogBodyContent(recordDatas);
        } else if (dataType == DataType.TRACE) {
            bodyContent = getTraceBodyContent(recordDatas);
        } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_WEBVIEW) {
            bodyContent = getRumBodyContent(recordDatas);
        } else {
            bodyContent = "";
        }
        return bodyContent.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION)
                .replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK);
    }


    /**
     * 获取 log 类型数据
     *
     * @param datas
     * @return
     */
    private String getLogBodyContent(List<SyncJsonData> datas) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.putAll(basePublicTags);
        hashMap.putAll(logTags);
        return convertToLineProtocolLines(datas, hashMap);
    }


    /**
     * 获取 trace 类型数据
     *
     * @param datas
     * @return
     */
    private String getTraceBodyContent(List<SyncJsonData> datas) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.putAll(basePublicTags);
        hashMap.putAll(traceTags);
        return convertToLineProtocolLines(datas, hashMap);
    }

    /**
     * 封装 RUM 数据
     *
     * @param datas
     * @return
     */
    private String getRumBodyContent(List<SyncJsonData> datas) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.putAll(basePublicTags);
        hashMap.putAll(rumTags);
        return convertToLineProtocolLines(datas, hashMap);
    }

    /**
     * 转化为行协议数据
     *
     * @param datas
     * @param extraTags
     * @return
     */
    private String convertToLineProtocolLines(List<SyncJsonData> datas, HashMap<String, Object> extraTags) {
        StringBuilder sb = new StringBuilder();
        for (SyncJsonData data : datas) {
            String jsonString = data.getDataString();
            if (jsonString != null) {
                try {
                    //========== measurement ==========
                    JSONObject opJson = new JSONObject(jsonString);
                    String measurement = opJson.optString(Constants.MEASUREMENT);
                    if (Utils.isNullOrEmpty(measurement)) {
                        measurement = FT_KEY_VALUE_NULL;
                    } else {
                        measurement = Utils.translateMeasurements(measurement);
                    }
                    sb.append(measurement);

                    //========== tags ==========
                    JSONObject tags = opJson.optJSONObject(Constants.TAGS);
                    if (extraTags != null) {
                        //合并去重
                        for (String key : extraTags.keySet()) {
                            if (!tags.has(key)) {
                                //此处空对象会被移除
                                tags.put(key, extraTags.get(key));
                            }
                        }
                    }
                    tags.put(Constants.KEY_SDK_DATA_FLAG, Utils.randomUUID());
                    StringBuilder tagSb = getCustomHash(tags, true);
                    deleteLastComma(tagSb);
                    if (tagSb.length() > 0) {
                        sb.append(",");
                        sb.append(tagSb.toString());
                    }
                    sb.append(Constants.SEPARATION_PRINT);

                    //========== field ==========
                    JSONObject fields = opJson.optJSONObject(Constants.FIELDS);
                    StringBuilder valueSb = getCustomHash(fields, false);
                    deleteLastComma(valueSb);
                    sb.append(valueSb);
                    sb.append(Constants.SEPARATION_PRINT);

                    //========= time ==========
                    sb.append(data.getTime());
                    sb.append(Constants.SEPARATION_LINE_BREAK);
                } catch (Exception e) {
                    LogUtils.e(TAG, Log.getStackTraceString(e));
                }
            }
        }
        return sb.toString();
    }


    /**
     * 获取自定义数据
     *
     * @param obj
     * @return
     */
    private static StringBuilder getCustomHash(JSONObject obj, boolean isTag) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String keyTemp = keys.next();
            Object value = obj.opt(keyTemp);
            if (value == null || "".equals(value) || JSONObject.NULL.equals(value)) {
                if (!isTag) {
                    String key = Utils.translateTagKeyValue(keyTemp);
                    sb.append(key);
                    sb.append("=");
                    sb.append("\"\"");
                } else {
                    continue;
                }
            } else {
                String key = Utils.translateTagKeyValue(keyTemp);
                sb.append(key);
                sb.append("=");
                if (value instanceof Float) {
                    sb.append(Utils.formatDouble((float) value));
                } else if (value instanceof Double) {
                    sb.append(Utils.formatDouble((double) value));
                } else if (value instanceof Boolean) {
                    sb.append(value);
                } else if (value instanceof Long || value instanceof Integer) {
                    sb.append(value).append(isTag ? "" : "i");
                } else {// String or Others
                    addQuotationMarks(sb, String.valueOf(value), !isTag);
                }
            }
            sb.append(",");
        }
        return sb;
    }

    private static void addQuotationMarks(StringBuilder sb, String value, boolean add) {
        if (add) {
            sb.append(Utils.translateFieldValue(value));
        } else {
            sb.append(Utils.translateTagKeyValue(value));
        }
    }

    /**
     * 删除最后的逗号
     *
     * @param sb
     */
    private static void deleteLastComma(StringBuilder sb) {
        StringUtils.deleteLastCharacter(sb, ",");
    }


}
