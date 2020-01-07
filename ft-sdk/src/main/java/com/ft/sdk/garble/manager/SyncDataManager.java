package com.ft.sdk.garble.manager;

import android.content.Context;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.OaidUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.ft.sdk.garble.bean.OP.CSTM;
import static com.ft.sdk.garble.utils.Constants.FT_DEFAULT_MEASUREMENT;
import static com.ft.sdk.garble.utils.Constants.FT_KEY_VALUE_NULL;

/**
 * BY huangDianHua
 * DATE:2019-12-11 14:48
 * Description:
 */
public class SyncDataManager {
    public String getBodyContent(List<RecordData> recordDatas) {
        StringBuffer sb = new StringBuffer();
        String device = parseHashToString(getDeviceInfo());
        for (RecordData recordData : recordDatas) {
            sb.append(getMeasurement(recordData));
            sb.append(",");
            sb.append(device.replaceAll(" ", "\\\\ "));
            sb.append(getUpdateData(recordData));
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 获得数据头
     * (当{@link RecordData#getOp()}等于
     * {@link com.ft.sdk.garble.bean.OP#CSTM}时用field字段，其他情况用
     * {@link com.ft.sdk.garble.utils.Constants#FT_DEFAULT_MEASUREMENT}
     *
     * @return
     */
    private String getMeasurement(RecordData recordData) {
        String measurement;
        if (CSTM.value.equals(recordData.getOp())) {
            try {
                JSONObject jsonObject = new JSONObject(recordData.getOpdata());
                String field = jsonObject.optString("field");
                if (Utils.isNullOrEmpty(field)) {
                    measurement = FT_KEY_VALUE_NULL;
                } else {
                    measurement = field;
                }
            } catch (Exception e) {
                e.printStackTrace();
                measurement = FT_KEY_VALUE_NULL;
            }
        } else {
            measurement = FT_DEFAULT_MEASUREMENT;
        }
        return measurement;
    }

    /**
     * 获取同步数据
     *
     * @param recordData
     * @return
     */
    private String getUpdateData(RecordData recordData) {
        if (CSTM.value.equals(recordData.getOp())) {
            return composeCustomUpdateData(recordData);
        } else {
            return composeAutoUpdateData(recordData);
        }
    }

    /**
     * 获取手动埋点的数据
     *
     * @return
     */
    private String composeCustomUpdateData(RecordData recordData) {
        StringBuffer sb = new StringBuffer();
        if (recordData.getOpdata() != null) {
            try {
                JSONObject opJson = new JSONObject(recordData.getOpdata());
                JSONObject tags = opJson.optJSONObject("tags");
                JSONObject values = opJson.optJSONObject("values");
                StringBuffer tagSb = getCustomHash(tags, true);
                StringBuffer valueSb = getCustomHash(values, false);
                addUserData(tagSb);
                deleteLastComma(tagSb);
                if (tagSb.length() > 0) {
                    sb.append(",");
                    sb.append(tagSb.toString().replaceAll(" ", "\\\\ "));
                }
                sb.append(" ");
                deleteLastComma(valueSb);
                sb.append(valueSb);
                sb.append(" ");
                sb.append(recordData.getTime() * 1000 * 1000);
            } catch (Exception e) {
            }
        }
        return sb.toString();
    }

    /**
     * 获取自定义数据
     *
     * @param tags
     * @return
     */
    private StringBuffer getCustomHash(JSONObject tags, boolean isTag) {
        StringBuffer sb = new StringBuffer();
        Iterator<String> keys = tags.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = tags.opt(key);
            sb.append(key);
            sb.append("=");
            if (value == null) {
                addQuotationMarks(sb,FT_KEY_VALUE_NULL,!isTag);
            } else {
                if ("".equals(value)) {
                    addQuotationMarks(sb,FT_KEY_VALUE_NULL,!isTag);
                } else {
                    if (value instanceof String) {
                        addQuotationMarks(sb,(String) value,!isTag);
                    } else {
                        sb.append(value);
                    }
                }
            }
            sb.append(",");
        }
        return sb;
    }

    private void addQuotationMarks(StringBuffer sb, String value, boolean add){
        if(add){
            sb.append("\"").append(value).append("\"");
        }else{
            sb.append(value);
        }
    }

    /**
     * 获得自动埋点的数据
     *
     * @return
     */
    private String composeAutoUpdateData(RecordData recordData) {
        StringBuffer sb = new StringBuffer();
        if (!Utils.isNullOrEmpty(recordData.getCpn())) {
            sb.append("current_page_name=" + recordData.getCpn() + ",");
        }
        if (!Utils.isNullOrEmpty(recordData.getRpn())) {
            sb.append("root_page_name=" + recordData.getRpn() + ",");
        }
        if (recordData.getOpdata() != null) {
            try {
                JSONObject opJson = new JSONObject(recordData.getOpdata());
                String vtp = opJson.optString("vtp");
                if (!Utils.isNullOrEmpty(vtp)) {
                    sb.append("vtp=" + vtp + ",");
                }
            } catch (Exception e) {
            }
        }
        addUserData(sb);
        deleteLastComma(sb);
        if (sb.length() > 0) {
            sb.insert(0, ",");
            String temp = sb.toString().replaceAll(" ", "\\\\ ");
            sb.delete(0, sb.length());
            sb.append(temp);
        }
        sb.append(" ");
        sb.append("event=\"" + getEventName(recordData.getOp()) + "\"");
        sb.append(" ");
        sb.append(recordData.getTime() * 1000000);
        return sb.toString();
    }

    /**
     * 添加用户信息
     * @param sb
     */
    private void addUserData(StringBuffer sb){
        if(FTUserConfig.get().isNeedBindUser() && FTUserConfig.get().isUserDataBinded()){
            UserData userData = FTUserConfig.get().getUserData();
            if(userData != null) {
                sb.append("ud_name=").append(userData.getName()).append(",");
                sb.append("ud_id=").append(userData.getId()).append(",");
                JSONObject js = userData.getExts();
                Iterator<String> iterator = js.keys();
                while (iterator.hasNext()){
                    String key = iterator.next();
                    try {
                        sb.append("ud_").append(key).append("=").append(js.getString(key)).append(",");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getEventName(String op){
        if(OP.LANC.value.equals(op)){
            return "launch";
        }else if(OP.CLK.value.equals(op)){
            return "click";
        }else if(OP.CLS.value.equals(op)){
            return "close";
        }else if(OP.OPEN.value.equals(op)){
            return "open";
        }
        return op;
    }

    /**
     * 删除最后的逗号
     *
     * @param sb
     */
    private void deleteLastComma(StringBuffer sb) {
        if (sb == null) {
            return;
        }
        int index = sb.lastIndexOf(",");
        if (index > 0 && index == sb.length() - 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    private String parseHashToString(HashMap<String, Object> param) {
        StringBuffer sb = new StringBuffer();
        if (param != null) {
            Iterator<String> keys = param.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = param.get(key);
                if (keys.hasNext()) {
                    if (value != null) {
                        if (value instanceof String && ((String) value).isEmpty()) {
                            value = FT_KEY_VALUE_NULL;
                        }
                        sb.append(key).append("=").append(value).append(",");
                    } else {
                        sb.append(key + "=" + FT_KEY_VALUE_NULL + ",");
                    }
                } else {
                    if (value != null) {
                        if (value instanceof String && ((String) value).isEmpty()) {
                            value = FT_KEY_VALUE_NULL;
                        }
                        sb.append(key + "=" + value);
                    } else {
                        sb.append(key + "=" + FT_KEY_VALUE_NULL);
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * 获取必要的设备信息
     *
     * @return
     */
    private HashMap<String, Object> getDeviceInfo() {
        Context context = FTApplication.getApplication();
        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put("device_uuid", DeviceUtils.getUuid(context));
        objectHashMap.put("application_identifier", DeviceUtils.getApplicationId(context));
        objectHashMap.put("application_name", DeviceUtils.getAppName(context));
        objectHashMap.put("sdk_version", DeviceUtils.getSDKVersion());
        objectHashMap.put("imei", DeviceUtils.getImei(context));
        objectHashMap.put("os", DeviceUtils.getOSName());
        objectHashMap.put("os_version", DeviceUtils.getOSVersion());
        objectHashMap.put("device_band", DeviceUtils.getDeviceBand());
        objectHashMap.put("device_model", DeviceUtils.getDeviceModel());
        objectHashMap.put("display", DeviceUtils.getDisplay(context));
        objectHashMap.put("carrier", DeviceUtils.getCarrier(context));
        if(FTHttpConfig.get().useOaid){
            objectHashMap.put("oaid", OaidUtils.getOAID(context));
        }
        return objectHashMap;
    }
}
