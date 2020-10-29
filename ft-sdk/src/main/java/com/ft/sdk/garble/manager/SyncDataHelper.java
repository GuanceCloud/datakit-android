package com.ft.sdk.garble.manager;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.Address;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.bean.BatteryBean;
import com.ft.sdk.garble.bean.CameraPx;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.SyncJsonData;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.ft.sdk.garble.bean.OP.CLIENT_ACTIVATED_TIME;
import static com.ft.sdk.garble.bean.OP.HTTP_CLIENT;
import static com.ft.sdk.garble.bean.OP.WEBVIEW_LOADING;
import static com.ft.sdk.garble.bean.OP.WEBVIEW_LOAD_COMPLETED;
import static com.ft.sdk.garble.utils.Constants.FT_KEY_VALUE_NULL;
import static com.ft.sdk.garble.utils.Constants.UNKNOWN;

/**
 * BY huangDianHua
 * DATE:2019-12-11 14:48
 * Description:
 */
public class SyncDataHelper {
    public final static String TAG = "SyncDataHelper";

    /**
     * 封装同步上传的数据
     *
     * @param dataType
     * @param recordDatas
     * @return
     */
    public String getBodyContent(DataType dataType, List<SyncJsonData> recordDatas) {
        if (dataType == DataType.OBJECT) {
            return getObjectBodyContent(recordDatas);
        } else if (dataType == DataType.LOG) {
            return getLogBodyContent(recordDatas);
        }
        return getTrackBodyContent(recordDatas);
    }

    /**
     * 封装本地对象数据
     *
     * @param recordDataList
     * @return
     */
    public String getObjectBodyContent(List<SyncJsonData> recordDataList) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (SyncJsonData recordData : recordDataList) {
                JSONArray array = new JSONArray(recordData.getDataString());
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        jsonArray.put(array.optJSONObject(i));
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }
        return jsonArray.toString();
    }

    /**
     * 封装本地埋点数据
     *
     * @param datas
     * @return
     */
    public String getTrackBodyContent(List<SyncJsonData> datas) {
        StringBuilder sb = new StringBuilder();
        String device = parseHashToString(getBaseDeviceInfoTagsMap());
        String uuid = parseHashToString(getUniqueIdMap());

        for (SyncJsonData recordData : datas) {
            //获取这条事件的指标
            sb.append(getMeasurement(recordData));
            //获取埋点事件数据
            if (!CLIENT_ACTIVATED_TIME.equals(recordData.getOpData().getOp())
                    && !HTTP_CLIENT.equals(recordData.getOpData().getOp())
                    && !HTTP_CLIENT.equals(recordData.getOpData().getOp())
                    && !WEBVIEW_LOAD_COMPLETED.equals(recordData.getOpData().getOp())
                    && !WEBVIEW_LOADING.equals(recordData.getOpData().getOp())) {
                sb.append(",");
                sb.append(uuid);
            }
            sb.append(",");
            sb.append(device);
            //获取埋点事件数据
            sb.append(composeUpdateData(recordData));
            sb.append(Constants.SEPARATION_LINE_BREAK);
        }
        return sb.toString();
    }

    public String getLogBodyContent(List<SyncJsonData> datas) {
        StringBuilder sb = new StringBuilder();
        for (SyncJsonData recordData : datas) {
            //获取这条事件的指标
            sb.append(getMeasurement(recordData));
            //获取埋点事件数据
            sb.append(composeUpdateData(recordData));
            sb.append(Constants.SEPARATION_LINE_BREAK);
        }
        return sb.toString();
    }

    /**
     * 获得数据头
     * (当{@link SyncJsonData#getOpData()#getOp()}等于
     * {@link com.ft.sdk.garble.bean.OP#CSTM} 时用field字段，其他情况用
     * {@link com.ft.sdk.garble.utils.Constants#FT_MEASUREMENT_PAGE_EVENT}
     *
     * @return
     */
    private String getMeasurement(SyncJsonData data) {
        String measurement;
        try {

            String jsonString;
            if (data.getDataType().equals(DataType.TRACK)) {
                jsonString = data.getOpData().getContent();
            } else {
                jsonString = data.getDataString();
            }

            JSONObject jsonObject = new JSONObject(jsonString);
            String measurementTemp = jsonObject.optString(Constants.MEASUREMENT);
            if (Utils.isNullOrEmpty(measurementTemp)) {
                measurement = FT_KEY_VALUE_NULL;
            } else {
                measurement = Utils.translateMeasurements(measurementTemp);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
            measurement = FT_KEY_VALUE_NULL;
        }
        return measurement;
    }

    /**
     * 获取自定义数据
     *
     * @param obj
     * @return
     */
    private static StringBuffer getCustomHash(JSONObject obj, boolean isTag) {
        StringBuffer sb = new StringBuffer();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String keyTemp = keys.next();
            Object value = obj.opt(keyTemp);
            String key = Utils.translateTagKeyValue(keyTemp);
            sb.append(key);
            sb.append("=");
            if (value == null) {
                addQuotationMarks(sb, UNKNOWN, !isTag);
            } else {
                if ("".equals(value)) {
                    addQuotationMarks(sb, UNKNOWN, !isTag);
                } else {
                    if (value instanceof Float) {
                        sb.append(Utils.formatDouble((float) value));
                    } else if (value instanceof Double) {
                        sb.append(Utils.formatDouble((double) value));
                    } else if (value instanceof Boolean) {
                        sb.append(value);
                    } else if (value instanceof Long || value instanceof Integer) {
                        sb.append(value).append(isTag ? "" : "i");
                    } else {// String or Others
                        addQuotationMarks(sb, (String) value, !isTag);
                    }
                }
            }
            sb.append(",");
        }
        return sb;
    }

    private static void addQuotationMarks(StringBuffer sb, String value, boolean add) {
        if (add) {
            sb.append(Utils.translateFieldValue(value));
        } else {
            sb.append(Utils.translateTagKeyValue(value));
        }
    }

    /**
     * 拼装数据
     *
     * @return
     */
    private String composeUpdateData(SyncJsonData data) {
        StringBuffer sb = new StringBuffer();
        String jsonString;
        if (data.getDataType().equals(DataType.TRACK)) {
            jsonString = data.getOpData().getContent();
        } else {
            jsonString = data.getDataString();
        }

        if (jsonString != null) {
            try {
                JSONObject opJson = new JSONObject(jsonString);
                JSONObject tags = opJson.optJSONObject(Constants.TAGS);
                JSONObject fields = opJson.optJSONObject(Constants.FIELDS);
                StringBuffer tagSb = getCustomHash(tags, true);
                StringBuffer valueSb = getCustomHash(fields, false);
                if (!data.getDataType().equals(DataType.LOG) && data.getOpData().getOp().isUserRelativeOp()) {
                    addUserData(tagSb, data);
                }
                deleteLastComma(tagSb);
                if (tagSb.length() > 0) {
                    sb.append(",");
                    sb.append(tagSb.toString());
                }
                sb.append(Constants.SEPARATION_PRINT);
                deleteLastComma(valueSb);
                sb.append(valueSb);
                sb.append(Constants.SEPARATION_PRINT);
                sb.append(data.getTime() * 1000 * 1000);
            } catch (Exception e) {
                LogUtils.e(TAG, e.getMessage());
            }
        }
        return sb.toString();
    }


    /**
     * 添加用户信息
     *
     * @param sb
     */
    private void addUserData(StringBuffer sb, SyncJsonData opData) {
        if (FTUserConfig.get().isNeedBindUser() && FTUserConfig.get().isUserDataBinded()) {
            UserData userData = FTUserConfig.get().getUserData(opData.getSessionId());
            if (userData != null) {
                sb.append(Constants.KEY_PAGE_EVENT_USER_NAME).append("=").append(Utils.translateTagKeyValue(userData.getName())).append(",");
                sb.append(Constants.KEY_PAGE_EVENT_USER_ID).append("=").append(Utils.translateTagKeyValue(userData.getId())).append(",");
                JSONObject js = userData.getExts();
                if (js == null) {
                    return;
                }
                Iterator<String> iterator = js.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        sb.append("ud_").append(Utils.translateTagKeyValue(key)).append("=").append(Utils.translateTagKeyValue(js.getString(key))).append(",");
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

        String device = parseHashToString(getBaseDeviceInfoTagsMap());
        String uuid = parseHashToString(getUniqueIdMap());

        sb.append(",").append(uuid).append(",").append(device);
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
            tags.put(Constants.KEY_BATTERY_TOTAL, batteryBean.getPower());
            tags.put(Constants.KEY_BATTERY_CHARGE_TYPE, batteryBean.getPlugState());
            tags.put(Constants.KEY_BATTERY_STATUS, batteryBean.getStatus());
            fields.put(Constants.KEY_BATTERY_USE, (float) batteryBean.getBr());
        } catch (Exception e) {
            LogUtils.e(TAG, "电池数据获取异常:" + e.getMessage());
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
            tags.put(Constants.KEY_MEMORY_TOTAL, memory[0] + "GB");
            fields.put(Constants.KEY_MEMORY_USE, memory[1]);
        } catch (Exception e) {
            LogUtils.e(TAG, "内存数据获取异常:" + e.getMessage());
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
            tags.put(Constants.KEY_CPU_NO, DeviceUtils.getHardWare());
            fields.put(Constants.KEY_CPU_USE, DeviceUtils.getCpuUseRate());
            fields.put(Constants.KEY_CPU_TEMPERATURE, CpuUtils.get().getCpuTemperature());
            tags.put(Constants.KEY_CPU_HZ, CpuUtils.get().getCPUMaxFreqKHz());
        } catch (Exception e) {
            LogUtils.e(TAG, "CPU数据获取异常:" + e.getMessage());
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
            tags.put(Constants.KEY_GPU_MODEL, GpuUtils.GPU_VENDOR_RENDERER);
            tags.put(Constants.KEY_GPU_HZ, GpuUtils.getGpuMaxFreq());
            fields.put(Constants.KEY_GPU_RATE, GpuUtils.getGpuUseRate());
        } catch (Exception e) {
            LogUtils.e(TAG, "GPU数据获取异常:" + e.getMessage());
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
                tags.put(Constants.KEY_NETWORK_TYPE, "Wi-Fi");
            } else if (networkType == 0) {
                tags.put(Constants.KEY_NETWORK_TYPE, UNKNOWN);
            } else {
                tags.put(Constants.KEY_NETWORK_TYPE, "蜂窝网络");
            }
            fields.put(Constants.KEY_NETWORK_STRENGTH, NetUtils.get().getSignalStrength(FTApplication.getApplication()));
            fields.put(Constants.KEY_NETWORK_IN_RATE, NetUtils.get().getNetDownRate());
            fields.put(Constants.KEY_NETWORK_OUT_RATE, NetUtils.get().getNetUpRate());
            tags.put(Constants.KEY_NETWORK_PROXY, NetUtils.get().isWifiProxy(FTApplication.getApplication()));
            String[] dns = NetUtils.get().getDnsFromConnectionManager(FTApplication.getApplication());
            for (int i = 0; i < dns.length; i++) {
                fields.put(Constants.KEY_NETWORK_DNS + (i + 1), dns[i]);
            }
            tags.put(Constants.KEY_NETWORK_ROAM, NetUtils.get().getRoamState());
            fields.put(Constants.KEY_NETWORK_WIFI_SSID, NetUtils.get().getSSId());
            fields.put(Constants.KEY_NETWORK_WIFI_IP, NetUtils.get().getWifiIp());
            NetStatusBean lastStatus = NetUtils.get().getLastMonitorStatus();

            if (lastStatus != null) {
                if (lastStatus.isInnerRequest()) {
                    fields.put(Constants.KEY_INNER_NETWORK_TCP_TIME, lastStatus.getTcpTime());
                    fields.put(Constants.KEY_INNER_NETWORK_DNS_TIME, lastStatus.getDNSTime());
                    fields.put(Constants.KEY_INNER_NETWORK_RESPONSE_TIME, lastStatus.getResponseTime());
                } else {
                    fields.put(Constants.KEY_NETWORK_TCP_TIME, lastStatus.getTcpTime());
                    fields.put(Constants.KEY_NETWORK_DNS_TIME, lastStatus.getDNSTime());
                    fields.put(Constants.KEY_NETWORK_RESPONSE_TIME, lastStatus.getResponseTime());
                }
                fields.put(Constants.KEY_NETWORK_ERROR_RATE, lastStatus.getErrorRate());
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "网络数据获取异常:" + e.getMessage());
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
            LogUtils.e(TAG, "相机数据获取异常:" + e.getMessage());
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
            Address address = LocationUtils.get().getAddress();
            double[] location = LocationUtils.get().getLocation();
            if (address != null) {
                tags.put(Constants.KEY_LOCATION_PROVINCE, address.getAdminArea());
                tags.put(Constants.KEY_LOCATION_CITY, address.getLocality());
                tags.put(Constants.KEY_LOCATION_COUNTRY, address.getCountryName());
            } else {
                tags.put(Constants.KEY_LOCATION_PROVINCE, Constants.UNKNOWN);
                tags.put(Constants.KEY_LOCATION_CITY, Constants.UNKNOWN);
                tags.put(Constants.KEY_LOCATION_COUNTRY, Constants.UNKNOWN);
            }

            if (location != null) {
                fields.put(Constants.KEY_LOCATION_LATITUDE, location[0]);
                fields.put(Constants.KEY_LOCATION_LONGITUDE, location[1]);
            } else {
                fields.put(Constants.KEY_LOCATION_LATITUDE, 0);
                fields.put(Constants.KEY_LOCATION_LONGITUDE, 0);
            }
            tags.put(Constants.KEY_LOCATION_GPS_OPEN, LocationUtils.get().isOpenGps());
        } catch (Exception e) {
            LogUtils.e(TAG, "位置数据获取异常:" + e.getMessage());
        }
    }

    private static void createSystem(JSONObject tags, JSONObject fields) {
        try {
            fields.put(Constants.KEY_DEVICE_OPEN_TIME, DeviceUtils.getSystemOpenTime());
            tags.put(Constants.KEY_DEVICE_NAME, BluetoothUtils.get().getDeviceName());
        } catch (Exception e) {
            LogUtils.e(TAG, "系统数据获取异常:" + e.getMessage());
        }
    }

    private static void createBluetooth(JSONObject tags, JSONObject fields) {
        try {
            Set<BluetoothDevice> set = BluetoothUtils.get().getBondedDevices();
            if (set != null) {
                int i = 1;
                for (BluetoothDevice device : set) {
                    fields.put(Constants.KEY_BT_DEVICE + (i++), device.getAddress());
                }
            }
            tags.put(Constants.KEY_BT_OPEN, BluetoothUtils.get().isOpen());

        } catch (Exception e) {
            LogUtils.e(TAG, "蓝牙数据获取异常:" + e.getMessage());
        }
    }

    private static void createSensor(JSONObject tags, JSONObject fields) {
        try {
            if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR)
                    || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_BRIGHTNESS)) {
                fields.put(Constants.KEY_SENSOR_BRIGHTNESS, DeviceUtils.getSystemScreenBrightnessValue());
            }
            if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR)
                    || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_LIGHT)) {
                fields.put(Constants.KEY_SENSOR_LIGHT, SensorUtils.get().getSensorLight());
            }
            if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR)
                    || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_PROXIMITY)) {
                fields.put(Constants.KEY_SENSOR_PROXIMITY, SensorUtils.get().getDistance());
            }
            if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR)
                    || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_STEP)) {
                fields.put(Constants.KEY_SENSOR_STEPS, (int) SensorUtils.get().getTodayStep());
            }
            if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR)
                    || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_ROTATION)) {
                float[] rotation = SensorUtils.get().getGyroscope();
                if (rotation != null && rotation.length == 3) {
                    fields.put(Constants.KEY_SENSOR_ROTATION_X, rotation[0]);
                    fields.put(Constants.KEY_SENSOR_ROTATION_Y, rotation[1]);
                    fields.put(Constants.KEY_SENSOR_ROTATION_Z, rotation[2]);
                }
            }
            if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR) || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_ACCELERATION)) {
                float[] acceleration = SensorUtils.get().getAcceleration();
                if (acceleration != null && acceleration.length == 3) {
                    fields.put(Constants.KEY_SENSOR_ACCELERATION_X, acceleration[0]);
                    fields.put(Constants.KEY_SENSOR_ACCELERATION_Y, acceleration[1]);
                    fields.put(Constants.KEY_SENSOR_ACCELERATION_Z, acceleration[2]);
                }
            }
            if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR) || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_MAGNETIC)) {
                float[] magnetic = SensorUtils.get().getMagnetic();
                if (magnetic != null && magnetic.length == 3) {
                    fields.put(Constants.KEY_SENSOR_MAGNETIC_X, magnetic[0]);
                    fields.put(Constants.KEY_SENSOR_MAGNETIC_Y, magnetic[1]);
                    fields.put(Constants.KEY_SENSOR_MAGNETIC_Z, magnetic[2]);
                }
            }
            if (FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR) || FTMonitorConfig.get().isMonitorType(MonitorType.SENSOR_TORCH)) {
                createTorch(tags, fields);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "传感器数据获取异常:" + e.getMessage());
        }
    }

    private static void createFps(JSONObject tags, JSONObject fields) {
        try {
            fields.put(Constants.KEY_FPS, FpsUtils.get().getFps());
        } catch (JSONException e) {
            LogUtils.e(TAG, "FPS数据获取异常:" + e.getMessage());
        }
    }

    /**
     * 闪光灯数据
     */
    private static void createTorch(JSONObject tags, JSONObject fields) {
        try {
            tags.put(Constants.KEY_TORCH, CameraUtils.get().isTorchState());
        } catch (JSONException e) {
            LogUtils.e(TAG, "闪光灯数据获取异常:" + e.getMessage());
        }
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

    /**
     * map 转化 String
     *
     * @param param
     * @return
     */
    private static String parseHashToString(HashMap<String, Object> param) {
        StringBuilder sb = new StringBuilder();
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
    private static HashMap<String, Object> getBaseDeviceInfoTagsMap() {
        HashMap<String, Object> objectHashMap = getBaseDeviceInfoHashMap();

        HashMap<String, Object> temp = new HashMap<>();
        for (Map.Entry<String, Object> entry : objectHashMap.entrySet()) {
            String mapKey = entry.getKey();
            Object mapValue = entry.getValue();
            if (mapValue instanceof String) {
                temp.put(mapKey, Utils.translateTagKeyValue((String) mapValue));
            } else {
                temp.put(mapKey, mapValue);
            }
        }
        return temp;
    }

    /**
     * 构建一个默认的 ObjectBean
     *
     * @return
     */
    public static HashMap<String, Object> getDefaultObjectBean() {
        HashMap<String, Object> objectHashMap = getBaseDeviceInfoHashMap();
        objectHashMap.putAll(getUniqueIdMap());
        return objectHashMap;

    }

    public static HashMap<String, Object> getBaseDeviceInfoHashMap() {
        Context context = FTApplication.getApplication();
        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put(Constants.KEY_DEVICE_APPLICATION_ID, DeviceUtils.getApplicationId(context));
        objectHashMap.put(Constants.KEY_DEVICE_APPLICATION_NAME, DeviceUtils.getAppName(context));
        objectHashMap.put(Constants.KEY_DEVICE_SDK_AGENT, DeviceUtils.getSDKVersion());
        objectHashMap.put(Constants.KEY_DEVICE_SDK_AUTO_TRACK, FTSdk.PLUGIN_VERSION);
        objectHashMap.put(Constants.KEY_DEVICE_OS, DeviceUtils.getOSName());
        objectHashMap.put(Constants.KEY_DEVICE_OS_VERSION, DeviceUtils.getOSVersion());
        objectHashMap.put(Constants.KEY_DEVICE_DEVICE_BAND, DeviceUtils.getDeviceBand());
        objectHashMap.put(Constants.KEY_DEVICE_DEVICE_MODEL, DeviceUtils.getDeviceModel());
        objectHashMap.put(Constants.KEY_DEVICE_DISPLAY, DeviceUtils.getDisplay(context));
        objectHashMap.put(Constants.KEY_DEVICE_CARRIER, DeviceUtils.getCarrier(context));
        objectHashMap.put(Constants.KEY_DEVICE_LOCALE, Locale.getDefault());
        objectHashMap.put(Constants.KEY_APP_VERSION_NAME, Utils.getAppVersionName());
        return objectHashMap;
    }


    public static HashMap<String, Object> getUniqueIdMap() {
        Context context = FTApplication.getApplication();
        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put(Constants.KEY_DEVICE_UUID, DeviceUtils.getUuid(context));
        objectHashMap.put(Constants.KEY_DEVICE_IMEI, DeviceUtils.getImei(context));
        if (FTHttpConfig.get().useOaid) {
            objectHashMap.put(Constants.KEY_DEVICE_OAID, OaidUtils.getOAID(context));
        }
        return objectHashMap;
    }

    /**
     * 将上传的数据格式化（供打印日志使用）
     *
     * @param body
     */
    public static void printUpdateData(boolean noTran, String body) {
        if (noTran) {
            StringBuffer sb = new StringBuffer();
            sb.append("-----------------------------------------------------------\n");
            sb.append("----------------------同步数据--开始-------------------------\n");
            sb.append(body);
            sb.append("----------------------同步数据--结束----------------------\n");
            LogUtils.d(TAG, sb.toString());
            return;
        }
        try {
            StringBuffer sb = new StringBuffer();
            String[] counts = body.split(Constants.SEPARATION_LINE_BREAK);
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
            LogUtils.d(TAG, "同步的数据\n" + sb.toString());
        } catch (Exception e) {
            LogUtils.d(TAG, "同步的数据\n" + body);
        }
    }
}
