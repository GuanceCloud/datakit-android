package com.ft.sdk.garble.manager;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.Address;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.FTAliasConfig;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.bean.BatteryBean;
import com.ft.sdk.garble.bean.CameraPx;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.BatteryUtils;
import com.ft.sdk.garble.utils.BluetoothUtils;
import com.ft.sdk.garble.utils.CameraUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.CpuUtils;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.FpsUtils;
import com.ft.sdk.garble.utils.GpuUtils;
import com.ft.sdk.garble.utils.LocationUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.OaidUtils;
import com.ft.sdk.garble.utils.SensorUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.ft.sdk.garble.bean.OP.CSTM;
import static com.ft.sdk.garble.bean.OP.FLOW_CHAT;
import static com.ft.sdk.garble.utils.Constants.FT_DEFAULT_MEASUREMENT;
import static com.ft.sdk.garble.utils.Constants.FT_KEY_VALUE_NULL;
import static com.ft.sdk.garble.utils.Constants.UNKNOWN;

/**
 * BY huangDianHua
 * DATE:2019-12-11 14:48
 * Description:
 */
public class SyncDataManager {

    /**
     * 封装同步上传的数据
     * @param dataType
     * @param recordDatas
     * @return
     */
    public String getBodyContent(DataType dataType,List<RecordData> recordDatas){
        switch (dataType){
            case OBJECT:
                return getObjectBodyContent(recordDatas);
            case LOG:
                return getLogBodyContent(recordDatas);
            case KEY_EVENT:
                return getKeyEventBodyContent(recordDatas);
            default:
                return getTrackBodyContent(recordDatas);
        }
    }

    /**
     * 封装本地对象数据
     * @param recordDataList
     * @return
     */
    public String getObjectBodyContent(List<RecordData> recordDataList){
        return "";
    }

    /**
     * 封装本地事件数据
     * @param recordDataList
     * @return
     */
    public String getKeyEventBodyContent(List<RecordData> recordDataList){
        return "";
    }

    /**
     * 封装本地日志数据
     * @param recordDataList
     * @return
     */
    public String getLogBodyContent(List<RecordData> recordDataList){
        return "";
    }
    /**
     * 封装本地埋点数据
     *
     * @param recordDatas
     * @return
     */
    public String getTrackBodyContent(List<RecordData> recordDatas) {
        StringBuffer sb = new StringBuffer();
        String device = parseHashToString(getDeviceInfo());
        for (RecordData recordData : recordDatas) {
            //流程图ID
            String traceId = recordData.getTraceId();
            if(!Utils.isNullOrEmpty(traceId)) {
                if (OP.OPEN_ACT.value.equals(recordData.getOp())) {
                    sb.append("$flow_mobile_activity");
                    sb.append(",$traceId=").append(recordData.getTraceId());
                    if (recordData.getPpn() != null && recordData.getPpn().startsWith(Constants.MOCK_SON_PAGE_DATA)) {
                        String[] strArr = recordData.getPpn().split(":");
                        String name = null;
                        String parent = null;
                        //数组长度等于 3 表示当前页面是一个 Fragment
                        if (strArr.length == 3) {
                            name = FTAliasConfig.get().getFlowChartDesc(recordData.getCpn() + "." + strArr[1]);
                            parent = FTAliasConfig.get().getFlowChartDesc(strArr[2]);
                            //数组长度等于 2 表示当前页面是一个 Activity
                        } else if (strArr.length == 2) {
                            name = FTAliasConfig.get().getFlowChartDesc(recordData.getCpn());
                            parent = FTAliasConfig.get().getFlowChartDesc(strArr[1]);
                        }
                        sb.append(",$name=").append(name);
                        sb.append(",$parent=").append(parent);
                    } else {
                        sb.append(",$name=").append(FTAliasConfig.get().getFlowChartDesc(recordData.getCpn()));
                        //如果父页面是root表示其为起始节点，不添加父节点
                        if (!Constants.FLOW_ROOT.equals(recordData.getPpn())) {
                            String ppn = recordData.getPpn();
                            sb.append(",$parent=").append(FTAliasConfig.get().getFlowChartDesc(ppn));
                        }
                    }
                    sb.append(",").append(device).append(",");
                    addUserData(sb, recordData);
                    //删除多余的逗号
                    deleteLastComma(sb);
                    sb.append(Constants.SEPARATION_PRINT);
                    sb.append("$duration=").append(recordData.getDuration()).append("i");
                    sb.append(Constants.SEPARATION_PRINT);
                    sb.append(recordData.getTime() * 1000 * 1000);
                    sb.append("\n");
                } else if (OP.OPEN_FRA.value.equals(recordData.getOp())) {
                    sb.append("$flow_mobile_activity");
                    sb.append(",$traceId=").append(recordData.getTraceId());
                    sb.append(",$name=").append(FTAliasConfig.get().getFlowChartDesc(recordData.getRpn() + "." + recordData.getCpn()));
                    String parent;
                    if (!Constants.FLOW_ROOT.equals(recordData.getPpn())) {
                        if (recordData.getPpn().startsWith(Constants.PERFIX)) {
                            parent = FTAliasConfig.get().getFlowChartDesc(recordData.getPpn().replace(Constants.PERFIX, ""));
                        } else {
                            parent = FTAliasConfig.get().getFlowChartDesc(recordData.getRpn() + "." + recordData.getPpn());
                        }
                    } else {
                        parent = FTAliasConfig.get().getFlowChartDesc(recordData.getRpn());
                    }
                    sb.append(",$parent=").append(parent);
                    sb.append(",").append(device).append(",");
                    addUserData(sb, recordData);
                    //删除多余的逗号
                    deleteLastComma(sb);
                    sb.append(Constants.SEPARATION_PRINT);
                    sb.append("$duration=").append(recordData.getDuration()).append("i");
                    sb.append(Constants.SEPARATION_PRINT);
                    sb.append(recordData.getTime() * 1000 * 1000);
                    sb.append("\n");
                }
            }else {
                //获取这条事件的指标
                sb.append(getMeasurement(recordData));
                sb.append(",");
                sb.append(device);
                //获取埋点事件数据
                sb.append(getUpdateData(recordData));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 获得数据头
     * (当{@link RecordData#getOp()}等于
     * {@link com.ft.sdk.garble.bean.OP#CSTM},{@link com.ft.sdk.garble.bean.OP#FLOW_CHAT}时用field字段，其他情况用
     * {@link com.ft.sdk.garble.utils.Constants#FT_DEFAULT_MEASUREMENT}
     *
     * @return
     */
    private String getMeasurement(RecordData recordData) {
        String measurement;
        try {
            JSONObject jsonObject = new JSONObject(recordData.getOpdata());
            if (CSTM.value.equals(recordData.getOp()) || FLOW_CHAT.value.equals(recordData.getOp())) {
                String measurementTemp = jsonObject.optString(Constants.MEASUREMENT);
                if (Utils.isNullOrEmpty(measurementTemp)) {
                    measurement = FT_KEY_VALUE_NULL;
                } else {
                    measurement = Utils.translateMeasurements(measurementTemp);
                }
            } else {
                measurement = FT_DEFAULT_MEASUREMENT;
            }
        } catch (Exception e) {
            e.printStackTrace();
            measurement = FT_KEY_VALUE_NULL;
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
        if (CSTM.value.equals(recordData.getOp()) || FLOW_CHAT.value.equals(recordData.getOp())) {
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
                JSONObject tags = opJson.optJSONObject(Constants.TAGS);
                JSONObject fields = opJson.optJSONObject(Constants.FIELDS);
                StringBuffer tagSb = getCustomHash(tags, true);
                StringBuffer valueSb = getCustomHash(fields, false);
                addUserData(tagSb, recordData);
                deleteLastComma(tagSb);
                if (tagSb.length() > 0) {
                    sb.append(",");
                    sb.append(tagSb.toString());
                }
                sb.append(Constants.SEPARATION_PRINT);
                deleteLastComma(valueSb);
                sb.append(valueSb);
                sb.append(Constants.SEPARATION_PRINT);
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
    private static StringBuffer getCustomHash(JSONObject tags, boolean isTag) {
        StringBuffer sb = new StringBuffer();
        Iterator<String> keys = tags.keys();
        while (keys.hasNext()) {
            String keyTemp = keys.next();
            Object value = tags.opt(keyTemp);
            String key = Utils.translateTagKeyValueAndFieldKey(keyTemp);
            sb.append(key);
            sb.append("=");
            if (value == null) {
                addQuotationMarks(sb, UNKNOWN, !isTag);
            } else {
                if ("".equals(value)) {
                    addQuotationMarks(sb, UNKNOWN, !isTag);
                } else {
                    if (value instanceof String) {
                        addQuotationMarks(sb, (String) value, !isTag);
                    } else if (value instanceof Float) {
                        sb.append(Utils.formatDouble((float) value));
                    } else if (value instanceof Double) {
                        sb.append(Utils.formatDouble((double) value));
                    } else {
                        sb.append(value);
                    }
                }
            }
            sb.append(",");
        }
        return sb;
    }

    private static void addQuotationMarks(StringBuffer sb, String value, boolean add) {
        if (add) {
            sb.append("\"").append(Utils.translateFieldValue(value)).append("\"");
        } else {
            sb.append(Utils.translateTagKeyValueAndFieldKey(value));
        }
    }

    /**
     * 获得自动埋点的数据
     *
     * @return
     */
    private String composeAutoUpdateData(RecordData recordData) {
        StringBuffer sb = new StringBuffer();
        JSONObject fields = null;
        if (recordData.getOpdata() != null) {
            try {
                JSONObject opJson = new JSONObject(recordData.getOpdata());
                JSONObject tags = opJson.optJSONObject(Constants.TAGS);
                if (tags == null) {
                    tags = new JSONObject();
                }
                if (!Utils.isNullOrEmpty(recordData.getCpn())) {
                    //如果是 Fragment 就把Activity 的名称也添加上去
                    if (recordData.getOp().equals(OP.OPEN_FRA.value) || recordData.getOp().equals(OP.CLS_FRA.value)) {
                        tags.put("current_page_name", recordData.getRpn() + "." + recordData.getCpn());
                    } else {
                        tags.put("current_page_name", recordData.getCpn());
                    }
                }
                if (!Utils.isNullOrEmpty(recordData.getRpn())) {
                    tags.put("root_page_name", recordData.getRpn());
                }
                tags.put("event_id", Utils.MD5(getEventName(recordData.getOp())));
                sb.append(getCustomHash(tags, true));
                fields = opJson.optJSONObject(Constants.FIELDS);
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
        sb.append(Constants.SEPARATION_PRINT);

        if (fields != null) {
            try {
                fields.put("event", getEventName(recordData.getOp()));
                if (!Utils.isNullOrEmpty(recordData.getCpn())) {
                    if (recordData.getOp().equals(OP.OPEN_FRA.value) || recordData.getOp().equals(OP.CLS_FRA.value)) {
                        fields.put("page_desc", FTAliasConfig.get().getPageDesc(recordData.getRpn() + "." + recordData.getCpn()));
                    } else {
                        fields.put("page_desc", FTAliasConfig.get().getPageDesc(recordData.getCpn()));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            StringBuffer valueSb = getCustomHash(fields, false);
            deleteLastComma(valueSb);
            sb.append(valueSb);
        } else {
            sb.append("event=\"" + getEventName(recordData.getOp()) + "\"");
        }
        sb.append(Constants.SEPARATION_PRINT);
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

    public static String getMonitorUploadData() {
        JSONObject tags = new JSONObject();
        JSONObject fields = new JSONObject();
        addMonitorData(tags, fields);
        StringBuffer sb = new StringBuffer();
        //获取这条事件的指标
        sb.append(Constants.FT_MONITOR_MEASUREMENT);
        StringBuffer tagSb = getCustomHash(tags, true);
        StringBuffer fieldSb = getCustomHash(fields, false);
        deleteLastComma(tagSb);
        if (tagSb.length() > 0) {
            sb.append(",");
            sb.append(tagSb.toString());
        }

        String device = parseHashToString(getDeviceInfo());
        sb.append(",").append(device);
        //删除多余的逗号
        deleteLastComma(sb);

        sb.append(Constants.SEPARATION_PRINT);
        deleteLastComma(fieldSb);
        sb.append(fieldSb);
        sb.append(Constants.SEPARATION_PRINT);
        sb.append(System.currentTimeMillis() * 1000 * 1000);
        return sb.toString();
    }

    /**
     * 添加配置监控项数据
     */
    public static void addMonitorData(JSONObject tags, JSONObject fields) {
        try {
            if (FTMonitorConfig.get().isMonitorType(MonitorType.ALL)) {
                //电池
                createBattery(tags, fields);
                //内存
                createMemory(tags, fields);
                //CPU
                createCPU(tags, fields);
                //GPU
                createGPU(tags, fields);
                //网络
                createNetWork(tags, fields);
                //相机
                createCamera(tags, fields);
                //位置
                createLocation(tags, fields);
                //蓝牙
                createBluetooth(tags, fields);
                //系统
                createSystem(tags, fields);
                createFps(tags, fields);
            } else {
                if (FTMonitorConfig.get().isMonitorType(MonitorType.BATTERY)) {
                    //电池
                    createBattery(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.MEMORY)) {
                    //内存
                    createMemory(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.CPU)) {
                    //CPU
                    createCPU(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.GPU)) {
                    //GPU
                    createGPU(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.NETWORK)) {
                    //网络
                    createNetWork(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.CAMERA)) {
                    createCamera(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.LOCATION)) {
                    createLocation(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.BLUETOOTH)) {
                    createBluetooth(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SYSTEM)) {
                    //系统
                    createSystem(tags, fields);
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.FPS)) {
                    createFps(tags, fields);
                }
            }
            //传感器
            createSensor(tags, fields);
        } catch (Exception e) {
        }
    }

    /**
     * 添加电池监控数据
     *
     * @param tags
     * @param fields
     */
    private static void createBattery(JSONObject tags, JSONObject fields) {
        try {
            BatteryBean batteryBean = BatteryUtils.getBatteryInfo(FTApplication.getApplication());
            tags.put("battery_total", batteryBean.getPower());
            tags.put("battery_charge_type", batteryBean.getPlugState());
            tags.put("battery_status", batteryBean.getStatus());
            fields.put("battery_use", batteryBean.getBr());
        } catch (Exception e) {
            LogUtils.e("电池数据获取异常:" + e.getMessage());
        }
    }

    /**
     * 添加内存监控数据
     *
     * @param tags
     * @param fields
     */
    private static void createMemory(JSONObject tags, JSONObject fields) {
        try {
            double[] memory = DeviceUtils.getRamData(FTApplication.getApplication());
            tags.put("memory_total", memory[0] + "GB");
            fields.put("memory_use", memory[1]);
        } catch (Exception e) {
            LogUtils.e("内存数据获取异常:" + e.getMessage());
        }
    }

    /**
     * 添加CPU监控数据
     *
     * @param tags
     * @param fields
     */
    private static void createCPU(JSONObject tags, JSONObject fields) {
        try {
            tags.put("cpu_no", DeviceUtils.getHardWare());
            fields.put("cpu_use", DeviceUtils.getCpuUseRate());
            fields.put("cpu_temperature", CpuUtils.get().getCpuTemperature());
            tags.put("cpu_hz", CpuUtils.get().getCPUMaxFreqKHz());
        } catch (Exception e) {
            LogUtils.e("CPU数据获取异常:" + e.getMessage());
        }
    }

    /**
     * 添加GPU监控数据
     *
     * @param tags
     * @param fields
     */
    private static void createGPU(JSONObject tags, JSONObject fields) {
        try {
            tags.put("gpu_model", GpuUtils.GPU_VENDOR_RENDERER);
            tags.put("gpu_hz", GpuUtils.getGpuMaxFreq());
            fields.put("gpu_rate", GpuUtils.getGpuUseRate());
        } catch (Exception e) {
            LogUtils.e("GPU数据获取异常:" + e.getMessage());
        }
    }

    /**
     * 添加网络监控数据
     *
     * @param tags
     * @param fields
     */
    private static void createNetWork(JSONObject tags, JSONObject fields) {
        try {
            int networkType = NetUtils.get().getNetworkState(FTApplication.getApplication());
            if (networkType == 1) {
                tags.put("network_type", "WIFI");
            } else if (networkType == 0) {
                tags.put("network_type", "N/A");
            } else {
                tags.put("network_type", "蜂窝网络");
            }
            fields.put("network_strength", NetUtils.get().getSignalStrength(FTApplication.getApplication()));
            fields.put("network_in_rate", NetUtils.get().getNetDownRate());
            fields.put("network_out_rate", NetUtils.get().getNetUpRate());
            tags.put("network_proxy", NetUtils.get().isWifiProxy(FTApplication.getApplication()));
            String[] dns = NetUtils.get().getDnsFromConnectionManager(FTApplication.getApplication());
            for (int i = 0; i < dns.length; i++) {
                fields.put("dns" + (i + 1), dns[i]);
            }
            tags.put("roam", NetUtils.get().getRoamState());
            fields.put("wifi_ssid", NetUtils.get().getSSId());
            fields.put("wifi_ip", NetUtils.get().getWifiIp());
            if(NetUtils.get().isInnerRequest()){
                fields.put("_network_tcp_time", NetUtils.get().getTcpTime());
                fields.put("_network_dns_time", NetUtils.get().getDNSTime());
                fields.put("_network_response_time", NetUtils.get().getResponseTime());
            }else {
                fields.put("network_tcp_time", NetUtils.get().getTcpTime());
                fields.put("network_dns_time", NetUtils.get().getDNSTime());
                fields.put("network_response_time", NetUtils.get().getResponseTime());
            }
            fields.put("network_error_rate", NetUtils.get().getErrorRate());
        } catch (Exception e) {
            LogUtils.e("网络数据获取异常:" + e.getMessage());
        }
    }

    /**
     * 添加相机监控数据
     *
     * @param tags
     * @param fields
     */
    private static void createCamera(JSONObject tags, JSONObject fields) {
        try {
            List<CameraPx> cameraPxs = CameraUtils.get().getCameraPxList(FTApplication.getApplication());
            for (CameraPx cameraPx : cameraPxs) {
                tags.put(cameraPx.getPx()[0], cameraPx.getPx()[1]);
            }
        } catch (Exception e) {
            LogUtils.e("相机数据获取异常:" + e.getMessage());
        }
    }

    /**
     * 添加位置监控数据
     *
     * @param tags
     * @param fields
     */
    private static void createLocation(JSONObject tags, JSONObject fields) {
        try {
            Address address = LocationUtils.get().getCity();
            double[] location = LocationUtils.get().getLocation();
            if (address != null) {
                tags.put("province", address.getAdminArea());
                tags.put("city", address.getLocality());
                tags.put("country", "中国");
            } else {
                tags.put("province", Constants.UNKNOWN);
                tags.put("city", Constants.UNKNOWN);
                tags.put("country", "中国");
            }

            if (location != null) {
                fields.put("latitude", location[0]);
                fields.put("longitude", location[1]);
            } else {
                fields.put("latitude", 0);
                fields.put("longitude", 0);
            }
            tags.put("gps_open", LocationUtils.get().isOpenGps());
        } catch (Exception e) {
            LogUtils.e("位置数据获取异常:" + e.getMessage());
        }
    }

    private static void createSystem(JSONObject tags, JSONObject fields) {
        try {
            fields.put("device_open_time", DeviceUtils.getSystemOpenTime());
            tags.put("device_name", BluetoothUtils.get().getDeviceName());
        } catch (Exception e) {
            LogUtils.e("系统数据获取异常:" + e.getMessage());
        }
    }

    private static void createBluetooth(JSONObject tags, JSONObject fields) {
        try {
            Set<BluetoothDevice> set = BluetoothUtils.get().getBondedDevices();
            if (set != null) {
                int i = 1;
                for (BluetoothDevice device : set) {
                    fields.put("bt_device" + (i++), device.getAddress());
                }
            }
            tags.put("bt_open", BluetoothUtils.get().isOpen());

        } catch (Exception e) {
            LogUtils.e("蓝牙数据获取异常:" + e.getMessage());
        }
    }

    private static void createSensor(JSONObject tags, JSONObject fields) {
        try {
            if (FTMonitorConfig.get().isMonitorType(MonitorType.ALL) || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR)) {
                fields.put("screen_brightness", DeviceUtils.getSystemScreenBrightnessValue());
                fields.put("light", SensorUtils.get().getSensorLight());
                fields.put("proximity", SensorUtils.get().getDistance());
                fields.put("steps", SensorUtils.get().getTodayStep());
                float[] rotation = SensorUtils.get().getGyroscope();
                if (rotation != null && rotation.length == 3) {
                    fields.put("rotation_x", rotation[0]);
                    fields.put("rotation_y", rotation[1]);
                    fields.put("rotation_z", rotation[2]);
                }

                float[] acceleration = SensorUtils.get().getAcceleration();
                if (acceleration != null && acceleration.length == 3) {
                    fields.put("acceleration_x", acceleration[0]);
                    fields.put("acceleration_y", acceleration[1]);
                    fields.put("acceleration_z", acceleration[2]);
                }

                float[] magnetic = SensorUtils.get().getMagnetic();
                if (magnetic != null && magnetic.length == 3) {
                    fields.put("magnetic_x", magnetic[0]);
                    fields.put("magnetic_y", magnetic[1]);
                    fields.put("magnetic_z", magnetic[2]);
                }
                createTorch(tags, fields);
            } else {
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_BRIGHTNESS)) {
                    fields.put("screen_brightness", DeviceUtils.getSystemScreenBrightnessValue());
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_LIGHT)) {
                    fields.put("light", SensorUtils.get().getSensorLight());
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_PROXIMITY)) {
                    fields.put("proximity", SensorUtils.get().getDistance());
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_STEP)) {
                    fields.put("steps", SensorUtils.get().getTodayStep());
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_ROTATION)) {
                    float[] rotation = SensorUtils.get().getGyroscope();
                    if (rotation != null && rotation.length == 3) {
                        fields.put("rotation_x", rotation[0]);
                        fields.put("rotation_y", rotation[1]);
                        fields.put("rotation_z", rotation[2]);
                    }
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_ACCELERATION)) {
                    float[] acceleration = SensorUtils.get().getAcceleration();
                    if (acceleration != null && acceleration.length == 3) {
                        fields.put("acceleration_x", acceleration[0]);
                        fields.put("acceleration_y", acceleration[1]);
                        fields.put("acceleration_z", acceleration[2]);
                    }
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_MAGNETIC)) {
                    float[] magnetic = SensorUtils.get().getMagnetic();
                    if (magnetic != null && magnetic.length == 3) {
                        fields.put("magnetic_x", magnetic[0]);
                        fields.put("magnetic_y", magnetic[1]);
                        fields.put("magnetic_z", magnetic[2]);
                    }
                }
                if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_TORCH)) {
                    createTorch(tags, fields);
                }
            }
        } catch (Exception e) {
            LogUtils.e("传感器数据获取异常:" + e.getMessage());
        }
    }

    private static void createFps(JSONObject tags, JSONObject fields) {
        try {
            fields.put("fps", FpsUtils.get().getFps());
        } catch (JSONException e) {
            LogUtils.e("FPS数据获取异常:" + e.getMessage());
        }
    }

    /**
     * 闪光灯数据
     */
    private static void createTorch(JSONObject tags, JSONObject fields) {
        try {
            tags.put("torch", CameraUtils.get().isTorchState());
        } catch (JSONException e) {
            LogUtils.e("闪光灯数据获取异常:" + e.getMessage());
        }
    }

    private String getEventName(String op) {
        if (OP.LANC.value.equals(op)) {
            return "launch";
        } else if (OP.CLK.value.equals(op)) {
            return "click";
        } else if (OP.CLS_FRA.value.equals(op)) {
            return "leave";
        } else if (OP.CLS_ACT.value.equals(op)) {
            return "leave";
        } else if (OP.OPEN_ACT.value.equals(op)) {
            return "enter";
        } else if (OP.OPEN_FRA.value.equals(op)) {
            return "enter";
        }
        return op;
    }

    /**
     * 删除最后的逗号
     *
     * @param sb
     */
    private static void deleteLastComma(StringBuffer sb) {
        if (sb == null) {
            return;
        }
        int index = sb.lastIndexOf(",");
        if (index > 0 && index == sb.length() - 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    private static String parseHashToString(HashMap<String, Object> param) {
        StringBuffer sb = new StringBuffer();
        if (param != null) {
            Iterator<String> keys = param.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = param.get(key);
                if (keys.hasNext()) {
                    if (value != null) {
                        if (value instanceof String && ((String) value).isEmpty()) {
                            value = UNKNOWN;
                        }
                        sb.append(key).append("=").append(value).append(",");
                    } else {
                        sb.append(key + "=" + UNKNOWN + ",");
                    }
                } else {
                    if (value != null) {
                        if (value instanceof String && ((String) value).isEmpty()) {
                            value = UNKNOWN;
                        }
                        sb.append(key + "=" + value);
                    } else {
                        sb.append(key + "=" + UNKNOWN);
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
    private static HashMap<String, Object> getDeviceInfo() {
        Context context = FTApplication.getApplication();
        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put("device_uuid", DeviceUtils.getUuid(context));
        objectHashMap.put("application_identifier", DeviceUtils.getApplicationId(context));
        objectHashMap.put("application_name", DeviceUtils.getAppName(context));
        objectHashMap.put("agent", DeviceUtils.getSDKVersion());
        objectHashMap.put("autoTrack", FTSdk.PLUGIN_VERSION);
        objectHashMap.put("imei", DeviceUtils.getImei(context));
        objectHashMap.put("os", DeviceUtils.getOSName());
        objectHashMap.put("os_version", DeviceUtils.getOSVersion());
        objectHashMap.put("device_band", DeviceUtils.getDeviceBand());
        objectHashMap.put("device_model", DeviceUtils.getDeviceModel());
        objectHashMap.put("display", DeviceUtils.getDisplay(context));
        objectHashMap.put("carrier", DeviceUtils.getCarrier(context));
        objectHashMap.put("locale", Locale.getDefault());
        if (FTHttpConfig.get().useOaid) {
            objectHashMap.put("oaid", OaidUtils.getOAID(context));
        }
        HashMap<String, Object> temp = new HashMap<>();
        for (Map.Entry<String, Object> entry : objectHashMap.entrySet()) {
            String mapKey = entry.getKey();
            Object mapValue = entry.getValue();
            if (mapValue instanceof String) {
                temp.put(mapKey, Utils.translateTagKeyValueAndFieldKey((String) mapValue));
            } else {
                temp.put(mapKey, mapValue);
            }
        }
        return temp;
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
            sb.append("-----------------------------------------------------------\n");
            sb.append("----------------------同步数据--开始-------------------------\n");
            sb.append("----------------------总共 ").append(counts.length).append(" 条数据------------------------\n");
            for (int i = 0; i < counts.length; i++) {
                String str = counts[i];
                String[] strArr = str.split(Constants.SEPARATION_PRINT);
                sb.append("---------------------第 ").append(i).append(" 条数据---开始----------------------\n");
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
            sb.append("----------------------同步数据--结束----------------------\n");
            LogUtils.d("同步的数据\n" + sb.toString());
        } catch (Exception e) {
            LogUtils.d("同步的数据\n" + body);
        }
    }
}
