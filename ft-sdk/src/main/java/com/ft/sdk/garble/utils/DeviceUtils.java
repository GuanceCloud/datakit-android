package com.ft.sdk.garble.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.ft.sdk.BuildConfig;
import com.ft.sdk.FTApplication;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.ft.sdk.garble.utils.Utils.getSharedPreferences;

/**
 * BY huangDianHua
 * DATE:2019-12-10 10:21
 * Description:
 */
public class DeviceUtils {
    private static final Map<String, String> sCarrierMap = new HashMap<String, String>() {
        {
            //中国移动
            put("46000", "中国移动");
            put("46002", "中国移动");
            put("46007", "中国移动");
            put("46008", "中国移动");

            //中国联通
            put("46001", "中国联通");
            put("46006", "中国联通");
            put("46009", "中国联通");

            //中国电信
            put("46003", "中国电信");
            put("46005", "中国电信");
            put("46011", "中国电信");

            //中国卫通
            put("46004", "中国卫通");

            //中国铁通
            put("46020", "中国铁通");

        }
    };

    /**
     *  获取 SDK 的 UUID
     * @param context
     * @return
     */
    public static String getSDKUUid(Context context) {
        final SharedPreferences preferences = getSharedPreferences(context);
        String sdkUUid = null;

        if(preferences != null){
            sdkUUid = preferences.getString(Constants.FT_SDK_INIT_UUID, null);
        }

        if (sdkUUid == null) {
            sdkUUid = UUID.randomUUID().toString();
            if(preferences != null) {
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Constants.FT_SDK_INIT_UUID, sdkUUid);
                editor.apply();
            }
        }

        return sdkUUid;
    }

    /**
     * 设置 SDK uuid
     * @param uuid
     */
    public static void setSDKUUid(String uuid){
        final SharedPreferences preferences = getSharedPreferences(FTApplication.getApplication());
        if(preferences != null) {
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.FT_SDK_INIT_UUID, uuid);
            editor.apply();
        }
    }

    /**
     * 获取设备唯一标识
     *
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getUuid(Context context) {
        String androidID = "";
        try {
            androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return androidID;
    }

    /**
     * 设置 SDK uuid
     * @param uuid
     */
    public static void setSDKUUid(String uuid){
        final SharedPreferences preferences = getSharedPreferences(FTApplication.getApplication());
        if(preferences != null) {
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.FT_SDK_INIT_UUID, uuid);
            editor.apply();
        }
    }

    /**
     * 获取应用ID
     *
     * @return
     */
    public static String getApplicationId(Context context) {
        return context.getPackageName();
    }

    /**
     * 获得程序名称
     *
     * @return
     */
    public static String getAppName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            ApplicationInfo info = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return info.loadLabel(manager).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得SDK版本号
     */
    public static String getSDKVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 获得设备IMEI码
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getImei(Context context) {
        String imei = "";
        try {
            if (!Utils.hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                return imei;
            }
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    if (tm.hasCarrierPrivileges()) {
                        imei = tm.getImei();
                    } else {
                        LogUtils.d("未能获取到设备的IMEI信息");
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = tm.getImei();
                } else {
                    imei = tm.getDeviceId();
                }
            }
        } catch (SecurityException e) {
            LogUtils.e("未能获取到系统>>Manifest.permission.READ_PHONE_STATE<<权限");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei;
    }

    /**
     * 获得系统名称
     *
     * @return
     */
    public static String getOSName() {
        return "Android";
    }

    /**
     * 系统版本
     *
     * @return
     */
    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获得系统语言环境
     *
     * @return
     */
    public static String getLocale() {
        return Locale.getDefault().toString();
    }

    /**
     * 获得设备品牌
     *
     * @return
     */
    public static String getDeviceBand() {
        return Build.BRAND;
    }

    /**
     * 获得设备机型
     *
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获得设备分辨率
     *
     * @return
     */
    public static String getDisplay(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        return screenWidth + "*" + screenHeight;
    }

    /**
     * 获取运行内存容量和内存使用率
     *
     * @param context
     * @return
     */
    public static String[] getRamData(Context context) {
        DecimalFormat showFloatFormat =new DecimalFormat("0.00");
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            long[] data = new long[]{info.totalMem, info.availMem};
            String[] strings = new String[2];
            strings[0] = showFloatFormat.format(1.0*data[0]/1024/1024/1024)+"GB";
            strings[1] = showFloatFormat.format((data[0]-data[1])*100.0/data[0])+"%";
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"N/A", "N/A"};
        }
    }

    /**
     * CPU平台信息
     *
     * @return
     */
    public static String getHardWare() {
        String result = Build.HARDWARE;
        return result;
    }

    /**
     * 获得CPU使用率
     * @return
     */
    public static String getCpuUseRate(){
        try {
            DecimalFormat showFloatFormat = new DecimalFormat("0.00");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return showFloatFormat.format(CpuUtils.get().getCpuDataForO()) + "%";
            } else {
                return showFloatFormat.format(CpuUtils.get().getCPUData()) + "%";
            }
        }catch (Exception e){
            return "N/A";
        }
    }

    /**
     * 获得设备运营商
     *
     * @return
     */
    public static String getCarrier(Context context) {
        try {
            if (Utils.hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                            .TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        String operator = telephonyManager.getSimOperator();
                        String alternativeName = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            CharSequence tmpCarrierName = telephonyManager.getSimCarrierIdName();
                            if (!TextUtils.isEmpty(tmpCarrierName)) {
                                alternativeName = tmpCarrierName.toString();
                            }
                        }
                        if (TextUtils.isEmpty(alternativeName)) {
                            if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                                alternativeName = telephonyManager.getSimOperatorName();
                            } else {
                                alternativeName = "未知";
                            }
                        }
                        if (!TextUtils.isEmpty(operator)) {
                            return operatorToCarrier(context, operator, alternativeName);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据 operator，获取本地化运营商信息
     *
     * @param context         context
     * @param operator        sim operator
     * @param alternativeName 备选名称
     * @return local carrier name
     */
    private static String operatorToCarrier(Context context, String operator, String alternativeName) {
        try {
            if (TextUtils.isEmpty(operator)) {
                return alternativeName;
            }
            if (sCarrierMap.containsKey(operator)) {
                return sCarrierMap.get(operator);
            }
            String carrierJson = getJsonFromAssets("ft_mcc_mnc_mini.json", context);
            if (TextUtils.isEmpty(carrierJson)) {
                sCarrierMap.put(operator, alternativeName);
                return alternativeName;
            }
            JSONObject jsonObject = new JSONObject(carrierJson);
            String carrier = getCarrierFromJsonObject(jsonObject, operator);
            if (!TextUtils.isEmpty(carrier)) {
                sCarrierMap.put(operator, carrier);
                return carrier;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alternativeName;
    }

    private static String getCarrierFromJsonObject(JSONObject jsonObject, String mccMnc) {
        if (jsonObject == null || TextUtils.isEmpty(mccMnc)) {
            return null;
        }
        return jsonObject.optString(mccMnc);

    }

    private static String getJsonFromAssets(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bf = null;
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }
}
