package com.ft.sdk.garble.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BY huangDianHua
 * DATE:2019-12-10 10:21
 * Description:
 */
public class DeviceUtils {
    public static final String TAG = Constants.LOG_TAG_PREFIX + "DeviceUtils";
    private static final String VM_RSS_PATTERN = "VmRSS:\\s+(\\d+) kB";

    private RandomAccessFile mProcStatusFile;


    private DeviceUtils() {

    }

    private static class SingletonHolder {
        private static final DeviceUtils INSTANCE = new DeviceUtils();
    }

    public static DeviceUtils get() {
        return DeviceUtils.SingletonHolder.INSTANCE;
    }

    /**
     * 运营商 map
     */
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
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
        return androidID;
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
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
        return "";
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
    public static double[] getRamData(Context context) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            long[] data = new long[]{info.totalMem, info.availMem};
            double[] strings = new double[2];
            strings[0] = Utils.formatDouble(1.0 * data[0] / 1024 / 1024 / 1024);
            strings[1] = Utils.formatDouble((data[0] - data[1]) * 100.0 / data[0]);
            return strings;
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
            return new double[]{0.00, 0.00};
        }
    }

    /**
     * 获取内存，单位 Byte
     *
     * @param
     * @return
     */
    public long getAppMemoryUseSize() {
        long memorySize = 0;
        try {
            if (mProcStatusFile == null) {
                mProcStatusFile = new RandomAccessFile("/proc/" + android.os.Process.myPid() + "/status", "r");
            } else {
                mProcStatusFile.seek(0);
            }
            String statusString;
            Pattern p = Pattern.compile(VM_RSS_PATTERN);
            while ((statusString = mProcStatusFile.readLine()) != null) {
                Matcher m = p.matcher(statusString);
                if (m.matches()) {
                    memorySize = Long.parseLong(m.group(1));
                    break;
                }
            }
            return memorySize * 1000;
        } catch (FileNotFoundException e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        } catch (IOException e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        }
        return memorySize;

    }

    /**
     * 进程启动到当前间隔时间,单位毫秒 {@link Process#getStartUptimeMillis();}
     *
     * @return
     */
    public long getStartTime() {
        long appTime = -1;
        try {
            RandomAccessFile appStatFile = new RandomAccessFile("/proc/"
                    + android.os.Process.myPid() + "/stat", "r");
            String appStatString = appStatFile.readLine();
            String[] appStats = appStatString.split(" ");
            appTime = Long.parseLong(appStats[19]);

        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
        return appTime;
    }


    /**
     * 获得CPU使用率
     *
     * @return
     */
    public static double getCpuUsage() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Utils.formatDouble(CpuUtils.get().getCPUUsageForO());
            } else {
                return Utils.formatDouble(CpuUtils.get().getCPUUsage());
            }
        } catch (Exception e) {
            return 0.00;
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
                                alternativeName = null;
                            }
                        }
                        if (!TextUtils.isEmpty(operator)) {
                            return operatorToCarrier(context, operator, alternativeName);
                        }
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, Log.getStackTraceString(e));
                }
            } else {
                LogUtils.e(TAG, "没有获得到 READ_PHONE_STATE 权限无法获取运营商信息");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        }
        return null;
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
            LogUtils.e(TAG, Log.getStackTraceString(e));

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
            LogUtils.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, Log.getStackTraceString(e));

                }
            }
        }
        return stringBuilder.toString();
    }
}
