package com.ft.sdk.garble.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

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
     * Carrier map
     */
    private static final Map<String, String> sCarrierMap = new HashMap<String, String>() {
        {
            // China Mobile
            put("46000", "China Mobile");
            put("46002", "China Mobile");
            put("46007", "China Mobile");
            put("46008", "China Mobile");

            // China Unicom
            put("46001", "China Unicom");
            put("46006", "China Unicom");
            put("46009", "China Unicom");

            // China Telecom
            put("46003", "China Telecom");
            put("46005", "China Telecom");
            put("46011", "China Telecom");

            // China Satcom
            put("46004", "China Satcom");

            // China Tietong
            put("46020", "China Tietong");

        }
    };


    /**
     * Get device unique identifier
     *
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getUuid(Context context) {
        String androidID = "";
        try {
            androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
        return androidID;
    }

    /**
     * Get system name
     *
     * @return
     */
    public static String getOSName() {
        return "Android";
    }

    /**
     * System version
     *
     * @return
     */
    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Get system locale
     *
     * @return
     */
    public static String getLocale() {
        return Locale.getDefault().toString();
    }

    /**
     * Get device brand
     *
     * @return
     */
    public static String getDeviceBand() {
        return Build.BRAND;
    }

    /**
     * Get device model
     *
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * Get CPU ABI architecture
     *
     * @return
     */
    public static String getDeviceArch() {
        return System.getProperty("os.arch");
    }


    /**
     * Get device resolution
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
     * Get RAM capacity and usage
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            return new double[]{0.00, 0.00};
        }
    }

    /**
     * Get memory, unit: Byte
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        } catch (IOException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return memorySize;

    }

    /**
     * Time interval from process start to current time, unit: milliseconds {@link Process#getStartUptimeMillis()}
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
        return appTime;
    }


    /**
     * Get CPU usage rate
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
     * Get device carrier
     *
     * @return
     */
    public static String getCarrier(Context context) {
        if (isTv(context)) return "";
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
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            } else {
                LogUtils.eOnce(TAG, "Failed to get READ_PHONE_STATE permission, unable to get carrier information");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return null;
    }

    /**
     * Get localized carrier information based on operator
     *
     * @param context         context
     * @param operator        sim operator
     * @param alternativeName alternative name
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

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
        //Convert json data to string
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bf = null;
        try {
            //Get assets resource manager
            AssetManager assetManager = context.getAssets();
            //Open file and read through manager
            bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));

                }
            }
        }
        return stringBuilder.toString();
    }

    private static final String FEATURE_GOOGLE_ANDROID_TV = "com.google.android.tv";

    public static boolean isTv(Context appContext) {
        UiModeManager uiModeManager = (UiModeManager) appContext.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            return true;
        }
        PackageManager packageManager = appContext.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                || packageManager.hasSystemFeature(FEATURE_GOOGLE_ANDROID_TV);
    }

}
