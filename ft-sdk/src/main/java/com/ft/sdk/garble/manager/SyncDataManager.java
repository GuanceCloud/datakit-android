package com.ft.sdk.garble.manager;

import android.content.Context;

import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.FTFlowChartConfig;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.bean.CameraPx;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.BatteryUtils;
import com.ft.sdk.garble.utils.CameraUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.CpuUtils;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.GpuUtils;
import com.ft.sdk.garble.utils.LocationUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;
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

    /**
     * 将本地将要同步的数据封装
     * @param recordDatas
     * @return
     */
    public String getBodyContent(List<RecordData> recordDatas) {
        StringBuffer sb = new StringBuffer();
        String device = parseHashToString(getDeviceInfo());
        for (RecordData recordData : recordDatas) {
            if(OP.OPEN_ACT.value.equals(recordData.getOp())){
                //如果是页面打开操作，就在该条数据上添加一条表示流程图的数据
                try {
                    JSONObject opData = new JSONObject(recordData.getOpdata());
                    //获取指标名称
                    if (opData.has(Constants.MEASUREMENT)) {
                        sb.append("$flow_mobile_activity_").append(opData.optString(Constants.MEASUREMENT));
                    }else{
                        sb.append("$flow_mobile_activity_").append(FTFlowChartConfig.get().getFlowProduct());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sb.append(",$traceId=").append(recordData.getTraceId());
                sb.append(",$name=").append(recordData.getCpn());
                //如果父页面是root表示其为起始节点，不添加父节点
                if(!Constants.FLOW_ROOT.equals(recordData.getPpn())){
                    sb.append(",$parent=").append(recordData.getPpn());
                }
                sb.append(",").append(device).append(",");
                addUserData(sb, recordData);
                //删除多余的逗号
                deleteLastComma(sb);
                sb.append(" ");
                sb.append("$duration=").append(recordData.getDuration()).append("i");
                sb.append(" ");
                sb.append(recordData.getTime() * 1000 * 1000);
                sb.append("\n");
            } else if(OP.CLS_ACT.value.equals(recordData.getOp())){
                //如果是关闭页面，也要附加一条页面关闭的流程图数据
                try {
                    //获取指标
                    JSONObject opData = new JSONObject(recordData.getOpdata());
                    if (opData.has(Constants.MEASUREMENT)) {
                        sb.append("$flow_mobile_activity_").append(opData.optString(Constants.MEASUREMENT));
                    }else{
                        sb.append("$flow_mobile_activity_").append(FTFlowChartConfig.get().getFlowProduct());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sb.append(",$traceId=").append(recordData.getTraceId());
                //如果父页面不是root，表示其为子页面的关闭
                if(!Constants.FLOW_ROOT.equals(recordData.getPpn())){
                    //交换当前页面和父页面
                    sb.append(",$name=").append(recordData.getPpn());
                    sb.append(",$parent=").append(recordData.getCpn());
                }
                sb.append(",").append(device).append(",");
                addUserData(sb, recordData);
                deleteLastComma(sb);
                sb.append(" ");
                sb.append("$duration=").append(recordData.getDuration()).append("i");
                sb.append(" ");
                sb.append(recordData.getTime() * 1000 * 1000);
                sb.append("\n");
            }
            //获取这条事件的指标
            sb.append(getMeasurement(recordData));
            sb.append(",");
            sb.append(device);
            //获取埋点事件数据
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
                String measurementTemp = jsonObject.optString(Constants.MEASUREMENT);
                if (Utils.isNullOrEmpty(measurementTemp)) {
                    measurement = FT_KEY_VALUE_NULL;
                } else {
                    measurement = Utils.translateMeasurements(measurementTemp);
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
                JSONObject fields = opJson.optJSONObject(Constants.FIELDS);
                StringBuffer tagSb = getCustomHash(tags, true);
                StringBuffer valueSb = getCustomHash(fields, false);
                addUserData(tagSb, recordData);
                deleteLastComma(tagSb);
                if (tagSb.length() > 0) {
                    sb.append(",");
                    sb.append(tagSb.toString());
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
            String keyTemp = keys.next();
            Object value = tags.opt(keyTemp);
            String key = Utils.translateTagKeyValueAndFieldKey(keyTemp);
            sb.append(key);
            sb.append("=");
            if (value == null) {
                addQuotationMarks(sb, FT_KEY_VALUE_NULL, !isTag);
            } else {
                if ("".equals(value)) {
                    addQuotationMarks(sb, FT_KEY_VALUE_NULL, !isTag);
                } else {
                    if (value instanceof String) {
                        addQuotationMarks(sb, Utils.translateTagKeyValueAndFieldKey((String) value), !isTag);
                    } else {
                        sb.append(value);
                    }
                }
            }
            sb.append(",");
        }
        return sb;
    }

    private void addQuotationMarks(StringBuffer sb, String value, boolean add) {
        if (add) {
            sb.append("\"").append(Utils.translateFieldValue(value)).append("\"");
        } else {
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
                JSONObject tags = opJson.optJSONObject("tags");
                if(tags != null) {
                    sb.append(getCustomHash(tags,true));
                }
            } catch (Exception e) {
            }
        }
        addUserData(sb, recordData);
        deleteLastComma(sb);
        if (sb.length() > 0) {
            sb.insert(0, ",");
            String temp = sb.toString();
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
     *
     * @param sb
     */
    private void addUserData(StringBuffer sb, RecordData recordData) {
        if (FTUserConfig.get().isNeedBindUser() && FTUserConfig.get().isUserDataBinded()) {
            UserData userData = FTUserConfig.get().getUserData(recordData.getSessionid());
            if (userData != null) {
                sb.append("ud_name=").append(Utils.translateTagKeyValueAndFieldKey(userData.getName())).append(",");
                sb.append("ud_id=").append(Utils.translateTagKeyValueAndFieldKey(userData.getId())).append(",");
                JSONObject js = userData.getExts();
                if (js == null) {
                    return;
                }
                Iterator<String> iterator = js.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        sb.append("ud_").append(Utils.translateTagKeyValueAndFieldKey(key)).append("=").append(Utils.translateTagKeyValueAndFieldKey(js.getString(key))).append(",");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 添加配置监控项数据
     */
    public static void addMonitorData(JSONObject tags) {
        try {
            Context context = FTSdk.get().getApplication();
            if (FTMonitorConfig.get().isMonitorType(MonitorType.ALL)) {
                tags.put("battery_total",BatteryUtils.getBatteryTotal(context)+"mAh");
                tags.put("battery_use",(100 - BatteryUtils.getBatteryCurrent(context))+"%");
                String[] memory = DeviceUtils.getRamData(context);
                tags.put("memory_total",memory[0]);
                tags.put("memory_use",memory[1]);

                tags.put("cpu_no",DeviceUtils.getHardWare());
                tags.put("cpu_use",DeviceUtils.getCpuUseRate());
                tags.put("cpu_temperature",CpuUtils.get().getCpuTemperature()+"℃");
                tags.put("cpu_hz",CpuUtils.get().getCPUMaxFreqKHz()+"Hz");

                tags.put("gpu_model",GpuUtils.GPU_VENDOR_RENDERER);
                tags.put("gpu_hz",GpuUtils.getGpuMaxFreq()+"Hz");
                tags.put("gpu_rate",GpuUtils.getGpuUseRate()+"%");

                int networkType = NetUtils.get().getNetworkState(context);
                if (networkType == 1) {
                    tags.put("network_type","WIFI");
                } else if (networkType == 0) {
                    tags.put("network_type","N/A");
                } else {
                    tags.put("network_type","蜂窝网络");
                }
                tags.put("network_strength",NetUtils.get().getSignalStrength());
                tags.put("network_speed",NetUtils.get().getNetRate());
                tags.put("network_proxy",NetUtils.get().isWifiProxy(context));
                List<CameraPx> cameraPxs = CameraUtils.getCameraPxList(context);
                for (CameraPx cameraPx : cameraPxs) {
                    tags.put(cameraPx.getPx()[0],cameraPx.getPx()[1]);
                }
                tags.put("location_city",LocationUtils.get().getCity());
            } else {
                if (FTMonitorConfig.get().isMonitorType(MonitorType.BATTERY)) {
                    tags.put("battery_total",BatteryUtils.getBatteryTotal(context)+"mAh");
                    tags.put("battery_use",(100 - BatteryUtils.getBatteryCurrent(context))+"%");
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.MEMORY)) {
                    String[] memory = DeviceUtils.getRamData(context);
                    tags.put("memory_total",memory[0]);
                    tags.put("memory_use",memory[1]);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.CPU)) {
                    tags.put("cpu_no", DeviceUtils.getHardWare());
                    tags.put("cpu_use", DeviceUtils.getCpuUseRate());
                    tags.put("cpu_temperature", CpuUtils.get().getCpuTemperature() + "℃");
                    tags.put("cpu_hz", CpuUtils.get().getCPUMaxFreqKHz() + "Hz");
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.GPU)) {
                    tags.put("gpu_model",GpuUtils.GPU_VENDOR_RENDERER);
                    tags.put("gpu_hz",GpuUtils.getGpuMaxFreq()+"Hz");
                    tags.put("gpu_rate",GpuUtils.getGpuUseRate()+"%");
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.NETWORK)) {
                    int networkType = NetUtils.get().getNetworkState(context);
                    if (networkType == 1) {
                        tags.put("network_type","WIFI");
                    } else if (networkType == 0) {
                        tags.put("network_type","N/A");
                    } else {
                        tags.put("network_type","蜂窝网络");
                    }
                    tags.put("network_strength",NetUtils.get().getSignalStrength());
                    tags.put("network_speed",NetUtils.get().getNetRate());
                    tags.put("network_proxy",NetUtils.get().isWifiProxy(context));
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.CAMERA)) {
                    List<CameraPx> cameraPxs = CameraUtils.getCameraPxList(context);
                    for (CameraPx cameraPx : cameraPxs) {
                        tags.put(cameraPx.getPx()[0],cameraPx.getPx()[1]);
                    }
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.LOCATION)) {
                    tags.put("location_city",LocationUtils.get().getCity());
                }
            }
        } catch (Exception e) {
        }
    }

    private String getEventName(String op) {
        if (OP.LANC.value.equals(op)) {
            return "launch";
        } else if (OP.CLK.value.equals(op)) {
            return "click";
        } else if (OP.CLS_FRA.value.equals(op)) {
            return "close";
        } else if (OP.CLS_ACT.value.equals(op)){
            return "close";
        }else if (OP.OPEN_ACT.value.equals(op)) {
            return "open";
        }else if (OP.OPEN_FRA.value.equals(op)) {
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
        Context context = FTSdk.get().getApplication();
        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put("device_uuid", DeviceUtils.getUuid(context));
        objectHashMap.put("application_identifier", DeviceUtils.getApplicationId(context));
        objectHashMap.put("application_name", DeviceUtils.getAppName(context));
        objectHashMap.put("sdk_version", DeviceUtils.getSDKVersion());
        objectHashMap.put("imei", DeviceUtils.getImei(context));
        objectHashMap.put("os", DeviceUtils.getOSName());
        objectHashMap.put("os_version", DeviceUtils.getOSVersion());
        objectHashMap.put("device_band", Utils.translateTagKeyValueAndFieldKey(DeviceUtils.getDeviceBand()));
        objectHashMap.put("device_model", Utils.translateTagKeyValueAndFieldKey(DeviceUtils.getDeviceModel()));
        objectHashMap.put("display", DeviceUtils.getDisplay(context));
        objectHashMap.put("carrier", DeviceUtils.getCarrier(context));
        if (FTHttpConfig.get().useOaid) {
            objectHashMap.put("oaid", OaidUtils.getOAID(context));
        }
        return objectHashMap;
    }

    /**
     * 将上传的数据格式化（供打印日志使用）
     *
     * @param body
     */
    public static void printUpdateData(String body) {
        try {
            StringBuffer sb = new StringBuffer();
            String[] counts = body.split("\n");
            for (String str : counts) {
                str = str.replaceAll("\\\\ ", "_");
                String[] strArr = str.split(" ");
                sb.append("{\n ");
                if (strArr.length == 3) {
                    sb.append("measurement{\n\t");
                    String str1 = strArr[0].replaceFirst(",", "\n },tags{\n\t");
                    str1 = str1.replaceAll(",", ",\n\t");
                    str1 = str1.replaceFirst(",\n\t", ",\n ");
                    sb.append(str1);
                    sb.append("\n },\n ");
                    sb.append("fields{\n\t");
                    String str2 = strArr[1].replaceAll(",", ",\n\t");
                    sb.append(str2);
                    sb.append("\n },\n ");
                    sb.append("time{\n\t");
                    sb.append(strArr[2]);
                    sb.append("\n }\n");
                }
                sb.append("},\n");
            }
            LogUtils.d("同步的数据\n" + sb.toString());
        } catch (Exception e) {
            LogUtils.d("同步的数据\n" + body);
        }
    }
}
