package com.ft.sdk;

import static com.ft.sdk.garble.utils.Constants.FT_KEY_VALUE_NULL;
import static com.ft.sdk.garble.utils.Constants.KEY_SDK_DATA_FLAG;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.StringUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.internal.exception.FTInvalidParameterException;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data assembly class, serializes collected data from stored data to line protocol data
 * <p>
 * tag: Override logic SDK inner tag > user tag > static GlobalContext > dynamic GlobalContext
 */
public class SyncDataHelper {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "SyncDataHelper";

    /**
     * Base data tags
     */
    private final HashMap<String, Object> basePublicTags;
    private final HashMap<String, Object> logTags;
    private final HashMap<String, Object> rumTags;


    private final HashMap<String, Object> dynamicBaseTags;
    private final HashMap<String, Object> dynamicLogTags;
    private final HashMap<String, Object> dynamicLRumTags;
    private final HashMap<String, Object> rumStaticFields;

    protected FTSDKConfig config;
    private FTLoggerConfig logConfig;

    protected DataModifier dataModifier;
    protected LineDataModifier lineDataModifier;


    protected SyncDataHelper() {
        basePublicTags = new HashMap<>();
        logTags = new HashMap<>();
        rumTags = new HashMap<>();

        dynamicBaseTags = new HashMap<>();
        dynamicLogTags = new HashMap<>();
        dynamicLRumTags = new HashMap<>();
        rumStaticFields = new HashMap<>();
    }

    void initBaseConfig(FTSDKConfig config) {
        this.config = config;
        this.dataModifier = config.getDataModifier();
        this.lineDataModifier = config.getLineDataModifier();

        basePublicTags.putAll(applyModifier(config.getGlobalContext()));
    }

    void initLogConfig(FTLoggerConfig config) {
        this.logConfig = config;
        logTags.putAll(basePublicTags);
        logTags.putAll(applyModifier(config.getGlobalContext()));
    }

    void initRUMConfig(FTRUMConfig config) {
        rumTags.putAll(basePublicTags);
        rumStaticFields.put(Constants.KEY_SESSION_SAMPLE_RATE,
                applyModifier(Constants.KEY_SESSION_SAMPLE_RATE, config.getSamplingRate()));
        rumStaticFields.put(Constants.KEY_SESSION_ON_ERROR_SAMPLE_RATE,
                applyModifier(Constants.KEY_SESSION_ON_ERROR_SAMPLE_RATE, config.getSessionErrorSampleRate()));
        rumTags.putAll(applyModifier(config.getGlobalContext()));
    }

    /**
     * Replace the entire dictionary
     *
     * @param map
     * @return
     */
    private Map<String, Object> applyModifier(HashMap<String, Object> map) {
        if (dataModifier == null || map == null) return map;
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object oldValue = entry.getValue();
            Object newValue = dataModifier.modify(key, oldValue);
            if (newValue != oldValue) {
                if (newValue != null && !newValue.equals(oldValue)) {
                    entry.setValue(newValue);
                }
            }
        }
        return map;
    }


    /**
     * Replace a single entry
     *
     * @param key
     * @param value
     * @return
     */
    Object applyModifier(String key, Object value) {
        if (dataModifier == null) return value;
        Object changeValue = dataModifier.modify(key, value);
        if (changeValue == null) {
            return value;
        }
        return changeValue;
    }


    /**
     * Replace a single data entry
     *
     * @param measurement
     * @param tags
     * @param fields
     */
    void appLineModifier(String measurement, HashMap<String, Object> tags, HashMap<String, Object> fields) {
        if (lineDataModifier == null) return;
        HashMap<String, Object> mergedValues = new HashMap<>();
        mergedValues.putAll(tags);
        mergedValues.putAll(fields);
        Map<String, Object> changedValues = lineDataModifier.modify(measurement, mergedValues);
        if (changedValues != null) {
            for (Map.Entry<String, Object> entry : changedValues.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (tags.containsKey(key)) {
                    if (value != null) {
                        tags.put(key, value);
                    }
                } else if (fields.containsKey(key)) {
                    if (value != null) {
                        fields.put(key, value);
                    }
                }
            }
        }
    }

    /**
     * Dynamically set global tag
     *
     * @param globalContext
     */
    void appendGlobalContext(HashMap<String, Object> globalContext) {
        if (globalContext != null) {
            applyModifier(globalContext);
            dynamicBaseTags.putAll(globalContext);
        }
    }

    /**
     * Dynamically set global tag
     *
     * @param key
     * @param value
     */
    void appendGlobalContext(String key, String value) {
        if (!Utils.isNullOrEmpty(key) && !Utils.isNullOrEmpty(value)) {
            dynamicBaseTags.put(key, applyModifier(key, value));
        }
    }


    /**
     * Dynamically set RUM global tag
     *
     * @param globalContext
     */
    void appendRUMGlobalContext(HashMap<String, Object> globalContext) {
        if (globalContext != null) {
            applyModifier(globalContext);
            dynamicLRumTags.putAll(globalContext);
            Object keyArr = rumTags.get(Constants.KEY_RUM_CUSTOM_KEYS);
            if (keyArr == null) {
                keyArr = "[]";
            }
            String keyArrString = (String) keyArr;
            rumTags.put(Constants.KEY_RUM_CUSTOM_KEYS,
                    Utils.addItemsToJsonArray(keyArrString, globalContext.keySet()));
        }
    }

    /**
     * Dynamically set RUM global tag
     *
     * @param key
     * @param value
     */
    void appendRUMGlobalContext(String key, String value) {
        if (!Utils.isNullOrEmpty(key) && !Utils.isNullOrEmpty(value)) {
            dynamicLRumTags.put(key, applyModifier(key, value));

            Object keyArr = rumTags.get(Constants.KEY_RUM_CUSTOM_KEYS);
            if (keyArr == null) {
                keyArr = "[]";
            }
            String keyArrString = (String) keyArr;
            rumTags.put(Constants.KEY_RUM_CUSTOM_KEYS, Utils.addItemToJsonArray(keyArrString, key));

        }
    }

    /**
     * Dynamically set log global tag
     *
     * @param globalContext
     */
    void appendLogGlobalContext(HashMap<String, Object> globalContext) {
        if (globalContext != null) {
            applyModifier(globalContext);
            dynamicLogTags.putAll(globalContext);
        }
    }

    /**
     * Dynamically set log global tag
     *
     * @param key
     * @param value
     */
    void appendLogGlobalContext(String key, String value) {
        if (!Utils.isNullOrEmpty(key) && !Utils.isNullOrEmpty(value)) {
            dynamicLogTags.put(key, applyModifier(key, value));
        }
    }

    /**
     * Convert data to line protocol storage
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
        applyModifier(tags);
        applyModifier(fields);
        HashMap<String, Object> mergeTags = new LinkedHashMap<>();
        mergeTags.put(KEY_SDK_DATA_FLAG, uuid);//Put uuid in the first position to save cost during string replacement
        if (tags != null) {
            mergeTags.putAll(tags);
        }
        String bodyContent;
        if (dataType == DataType.LOG) {
            // log data
            mergeTags.putAll(dynamicBaseTags);
            mergeTags.putAll(dynamicLogTags);
            mergeTags.putAll(logTags);
            if (logConfig != null) {
                if (logConfig.isEnableLinkRumData()) {
                    mergeTags.putAll(rumTags);
                }
            }
            appLineModifier(measurement, mergeTags, fields);
            bodyContent = convertToLineProtocolLine(measurement, mergeTags, fields,
                    timeStamp, config);
        } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_APP_ERROR_SAMPLED
                || dataType == DataType.RUM_WEBVIEW || dataType == DataType.RUM_WEBVIEW_ERROR_SAMPLED) {
            //rum data
            mergeTags.putAll(dynamicBaseTags);
            mergeTags.putAll(dynamicLRumTags);
            if (dataType == DataType.RUM_APP || dataType == DataType.RUM_APP_ERROR_SAMPLED) {
                mergeTags.putAll(rumTags);
                fields.putAll(rumStaticFields);
            } else {
                Object webSDKVersion = mergeTags.get(Constants.KEY_SDK_VERSION);
                Iterator<String> keys = rumTags.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (!key.equals(Constants.KEY_SERVICE)) {
                        if (key.equals(Constants.KEY_RUM_SDK_PACKAGE_INFO)) {
                            Object pkgInfo = rumTags.get(Constants.KEY_RUM_SDK_PACKAGE_INFO);
                            if (pkgInfo != null) {
                                String replacePkgInfo = PackageUtils.appendPackageVersion(pkgInfo.toString(),
                                        Constants.KEY_RUM_SDK_PACKAGE_WEB, webSDKVersion + "");
                                mergeTags.put(Constants.KEY_RUM_SDK_PACKAGE_INFO,
                                        applyModifier(Constants.KEY_RUM_SDK_PACKAGE_INFO, replacePkgInfo));
                            }
                        } else {
                            mergeTags.put(key, rumTags.get(key));
                        }
                    }
                }
            }
            appLineModifier(measurement, mergeTags, fields);
            bodyContent = convertToLineProtocolLine(measurement, mergeTags, fields, timeStamp, config);
        } else {
            bodyContent = "";
        }
        return bodyContent;

    }


    static String convertToLineProtocolLine(String measurement, HashMap<String, Object> tags,
                                            HashMap<String, Object> fields,
                                            long timeStamp,
                                            FTSDKConfig config) {
        if (measurement == null) {
            throw new FTInvalidParameterException("Measurement set 'measurement' cannot be null");
        }

        if (fields == null || fields.isEmpty()) {
            throw new FTInvalidParameterException("Measurement set 'fields' cannot be null");
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
     * Get custom data
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
     * Add quotation marks
     *
     * @param sb
     * @param value Original data
     * @param add   Whether to add
     */
    private static void addQuotationMarks(StringBuilder sb, String value, boolean add) {
        if (add) {
            sb.append(Utils.translateFieldValue(value));
        } else {
            sb.append(Utils.translateTagKeyValue(value));
        }
    }

    /**
     * Delete the last comma
     *
     * @param sb
     */
    private static void deleteLastComma(StringBuilder sb) {
        StringUtils.deleteLastCharacter(sb, ",");
    }

    /**
     * Get compatibility migration method
     *
     * @return
     */
    SyncDataCompatHelper getCompat() {
        return new SyncDataCompatHelper(logTags, rumTags, config);
    }


}
