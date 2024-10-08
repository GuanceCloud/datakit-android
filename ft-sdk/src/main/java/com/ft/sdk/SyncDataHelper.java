package com.ft.sdk;

import static com.ft.sdk.garble.utils.Constants.FT_KEY_VALUE_NULL;

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
 * 数据组装类，把采集数据从存储数据序列化行协议数据
 */
public class SyncDataHelper {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "SyncDataHelper";

    /**
     * 基础数据标签
     */
    private final HashMap<String, Object> basePublicTags;
    private final HashMap<String, Object> logTags;
    private final HashMap<String, Object> rumTags;
    private final HashMap<String, Object> traceTags;

    private FTSDKConfig config;


    protected SyncDataHelper() {
        basePublicTags = new HashMap<>();
        logTags = new HashMap<>();
        rumTags = new HashMap<>();
        traceTags = new HashMap<>();
    }

    void initBaseConfig(FTSDKConfig config) {
        this.config = config;
        basePublicTags.putAll(config.getGlobalContext());
    }

    void initLogConfig(FTLoggerConfig config) {
        logTags.putAll(basePublicTags);
        logTags.putAll(config.getGlobalContext());
    }

    void initRUMConfig(FTRUMConfig config) {
        rumTags.putAll(basePublicTags);
        rumTags.putAll(config.getGlobalContext());
    }

    void initTraceConfig(FTTraceConfig config) {
        traceTags.putAll(basePublicTags);
        traceTags.putAll(config.getGlobalContext());
    }


    /**
     * 封装同步上传的数据
     *
     * @param dataType
     * @param recordDatas
     * @return
     */
    public String getBodyContent(DataType dataType, List<SyncJsonData> recordDatas, String packageId) {
        String bodyContent;
        if (dataType == DataType.LOG) {
            // log 数据
            bodyContent = convertToLineProtocolLines(recordDatas, new HashMap<>(logTags), packageId);
        } else if (dataType == DataType.TRACE) {
            // trace 数据
            bodyContent = convertToLineProtocolLines(recordDatas, new HashMap<>(traceTags), packageId);
        } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_WEBVIEW) {
            //rum 数据
            bodyContent = convertToLineProtocolLines(recordDatas, new HashMap<>(rumTags), packageId);
        } else {
            bodyContent = "";
        }
        return bodyContent.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION)
                .replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK);
    }

    /**
     * 封装同步上传的数据，主要用于测试用例使用
     *
     * @param dataType
     * @param recordDatas
     * @return
     */
    public String getBodyContent(DataType dataType, List<SyncJsonData> recordDatas) {
        return getBodyContent(dataType, recordDatas, null);
    }


    /**
     * 转化为行协议数据
     *
     * @param datas
     * @param extraTags
     * @return
     */
    private String convertToLineProtocolLines(List<SyncJsonData> datas, HashMap<String, Object> extraTags,
                                              String packageId) {

        boolean integerCompatible = false;
        if (config != null) {
            integerCompatible = config.isEnableDataIntegerCompatible();
        }
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
                    if (Utils.isNullOrEmpty(packageId)) {
                        tags.put(Constants.KEY_SDK_DATA_FLAG, Utils.randomUUID());
                    } else {
                        tags.put(Constants.KEY_SDK_DATA_FLAG, packageId + "." + Utils.randomUUID());
                    }
                    StringBuilder tagSb = getCustomHash(tags, true, integerCompatible);
                    deleteLastComma(tagSb);
                    if (tagSb.length() > 0) {
                        sb.append(",");
                        sb.append(tagSb);
                    }
                    sb.append(Constants.SEPARATION_PRINT);

                    //========== field ==========
                    JSONObject fields = opJson.optJSONObject(Constants.FIELDS);
                    StringBuilder valueSb = getCustomHash(fields, false, integerCompatible);
                    deleteLastComma(valueSb);
                    sb.append(valueSb);
                    sb.append(Constants.SEPARATION_PRINT);

                    //========= time ==========
                    sb.append(data.getTime());
                    sb.append(Constants.SEPARATION_LINE_BREAK);
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
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
    private static StringBuilder getCustomHash(JSONObject obj, boolean isTag, boolean integerCompatible) {
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
                    sb.append(value).append(isTag || integerCompatible ? "" : "i");
                } else {// String or Others
                    addQuotationMarks(sb, String.valueOf(value), !isTag);
                }
            }
            sb.append(",");
        }
        return sb;
    }

    /**
     * 添加引号标记
     *
     * @param sb
     * @param value 愿数据
     * @param add   是否需要添加
     */
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
