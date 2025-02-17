package com.ft.sdk;

import static com.ft.sdk.garble.utils.Constants.FT_KEY_VALUE_NULL;
import static com.ft.sdk.garble.utils.Constants.KEY_SDK_DATA_FLAG;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.StringUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.internal.exception.FTInvalidParameterException;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * 数据组装类，把采集数据从存储数据序列化行协议数据
 *
 * tag:覆盖逻辑 SDK inner tag > user tag > static GlobalContext > dynamic GlobalContext
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

    private final HashMap<String, Object> dynamicBaseTags;
    private final HashMap<String, Object> dynamicLogTags;
    private final HashMap<String, Object> dynamicLRumTags;

    protected FTSDKConfig config;


    protected SyncDataHelper() {
        basePublicTags = new HashMap<>();
        logTags = new HashMap<>();
        rumTags = new HashMap<>();
        traceTags = new HashMap<>();

        dynamicBaseTags = new HashMap<>();
        dynamicLogTags = new HashMap<>();
        dynamicLRumTags = new HashMap<>();
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
     * 动态设置全局 tag
     *
     * @param globalContext
     */
    void appendGlobalContext(HashMap<String, Object> globalContext) {
        if (globalContext != null) {
            dynamicBaseTags.putAll(globalContext);
        }
    }

    /**
     * 动态设置全局 tag
     *
     * @param key
     * @param value
     */
    void appendGlobalContext(String key, String value) {
        if (!Utils.isNullOrEmpty(key) && !Utils.isNullOrEmpty(value)) {
            dynamicBaseTags.put(key, value);
        }
    }


    /**
     * 动态设置 RUM 全局 tag
     *
     * @param globalContext
     */
    void appendRUMGlobalContext(HashMap<String, Object> globalContext) {
        if (globalContext != null) {
            dynamicLRumTags.putAll(globalContext);
        }
    }

    /**
     * 动态设置 RUM 全局 tag
     *
     * @param key
     * @param value
     *
     */
    void appendRUMGlobalContext(String key, String value) {
        if (!Utils.isNullOrEmpty(key) && !Utils.isNullOrEmpty(value)) {
            dynamicLRumTags.put(key, value);
        }
    }

    /**
     * 动态设置 log 全局 tag
     *
     * @param globalContext
     */
    void appendLogGlobalContext(HashMap<String, Object> globalContext) {
        if (globalContext != null) {
            dynamicLogTags.putAll(globalContext);
        }
    }

    /**
     * 动态设置 log 全局 tag
     *
     * @param key
     * @param value
     *
     */
    void appendLogGlobalContext(String key, String value) {
        if (!Utils.isNullOrEmpty(key) && !Utils.isNullOrEmpty(value)) {
            dynamicLogTags.put(key, value);
        }
    }

    /**
     * 数据转行协议存储
     *
     * @param measurement
     * @param tags
     * @param fields
     * @param timeStamp
     * @param dataType
     * @param uuid
     * @return
     */

    public String getBodyContent(String measurement, HashMap<String, Object> tags,
                                 HashMap<String, Object> fields, long timeStamp, DataType dataType, String uuid) {
        HashMap<String, Object> mergeTags = new LinkedHashMap<>();
        mergeTags.put(KEY_SDK_DATA_FLAG, uuid);//让 uuid 放置第一位，字符替换时可以节省损耗
        if (tags != null) {
            mergeTags.putAll(tags);
        }
        String bodyContent;
        if (dataType == DataType.LOG) {
            // log 数据
            mergeTags.putAll(dynamicBaseTags);
            mergeTags.putAll(dynamicLogTags);
            mergeTags.putAll(logTags);
            bodyContent = convertToLineProtocolLine(measurement, mergeTags, fields,
                    timeStamp, config);

        } else if (dataType == DataType.TRACE) {
            // trace 数据
            mergeTags.putAll(traceTags);
            bodyContent = convertToLineProtocolLine(measurement, mergeTags, fields,
                    timeStamp, config);
        } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_WEBVIEW) {
            //rum 数据
            mergeTags.putAll(dynamicBaseTags);
            mergeTags.putAll(dynamicLRumTags);
            if (dataType == DataType.RUM_APP) {
                mergeTags.putAll(rumTags);
            }
            bodyContent = convertToLineProtocolLine(measurement, mergeTags, fields,
                    timeStamp, config);
        } else {
            bodyContent = "";
        }
        return bodyContent;

    }

    /**
     * 动态和静态 rum tags
     *
     * @return
     */
    HashMap<String, Object> getCurrentRumTags() {
        HashMap<String, Object> mergeTags = new HashMap<>();
        mergeTags.putAll(dynamicLRumTags);
        mergeTags.putAll(rumTags);
        return mergeTags;
    }

    static String convertToLineProtocolLine(String measurement, HashMap<String, Object> tags,
                                            HashMap<String, Object> fields,
                                            long timeStamp,
                                            FTSDKConfig config) {


        if (measurement == null) {
            throw new FTInvalidParameterException("指标集 measurement 不能为空");
        }

        if (fields == null) {
            throw new FTInvalidParameterException("指标集 fields 不能为空");
        }

        boolean integerCompatible = false;
        if (config != null) {
            integerCompatible = config.isEnableDataIntegerCompatible();
        }
        StringBuilder sb = new StringBuilder();

        try {
            //========== measurement ==========
            if (Utils.isNullOrEmpty(measurement)) {
                measurement = FT_KEY_VALUE_NULL;
            } else {
                measurement = Utils.translateMeasurements(measurement);
            }
            sb.append(measurement);

            //========== tags ==========
            StringBuilder tagSb = getCustomHash(tags, true, integerCompatible);
            deleteLastComma(tagSb);
            if (tagSb.length() > 0) {
                sb.append(",");
                sb.append(tagSb);
            }
            sb.append(Constants.SEPARATION);

            //========== field ==========
            StringBuilder valueSb = getCustomHash(fields, false, integerCompatible);
            deleteLastComma(valueSb);
            sb.append(valueSb);
            sb.append(Constants.SEPARATION);

            //========= time ==========
            sb.append(timeStamp);
            sb.append(Constants.SEPARATION_REAL_LINE_BREAK);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return sb.toString();
    }


    /**
     * 获取自定义数据
     *
     * @param obj
     * @return
     */
    private static StringBuilder getCustomHash(HashMap<String, Object> obj, boolean isTag, boolean integerCompatible) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> keys = obj.keySet().iterator();
        while (keys.hasNext()) {
            String keyTemp = keys.next();
            Object value = obj.get(keyTemp);
            if (value == null || String.valueOf(value).isEmpty() || JSONObject.NULL.equals(value)) {
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

    /**
     * 获取兼容迁移方法
     *
     * @return
     */
    SyncDataCompatHelper getCompat() {
        return new SyncDataCompatHelper(logTags, traceTags, rumTags, config);
    }


}
