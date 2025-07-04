package com.ft.sdk.garble.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.ft.sdk.garble.bean.BatteryBean;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:47
 * Description: Refer to open source project <a href="https://github.com/guxiaonian/MobileInfo">address</a>
 */
public class BatteryUtils {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "BatteryUtils";
    static double batteryCapacity = 0; // battery capacity mAh

    /**
     * Get the current battery usage percentage
     *
     * @param context
     * @return
     */
    public static int getBatteryUsage(Context context) {
        int capacity = 0;
        try {
            BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            int value = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);// current battery remaining percentage
            capacity = (100 - value);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return capacity;
    }

    /**
     * Current battery current, unit mA
     *
     * @param context
     * @return
     */
    public static int getBatteryCurrent(Context context) {
        try {
            BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000;
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return 0;
    }


    /**
     * Get battery capacity, unit mAh
     *
     * @param context
     * @return
     */
    public static String getBatteryCapacity(Context context) {
        if (batteryCapacity > 0) {
            return batteryCapacity + "mAh";
        }
        Object mPowerProfile;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS).getConstructor(Context.class).newInstance(context);
            batteryCapacity = (double) Class.forName(POWER_PROFILE_CLASS).getMethod("getBatteryCapacity").invoke(mPowerProfile);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        if (batteryCapacity == 0) {
            return "";
        }
        return batteryCapacity + "mAh";
    }

    /**
     * Get battery information
     *
     * @return
     */
    public static BatteryBean getBatteryInfo(Context context) {
        BatteryBean batteryBean = new BatteryBean();
        try {
            Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryStatus != null) {
//                int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
//                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//                int plugState = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//                int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
//                boolean present = batteryStatus.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
//                String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
//                int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
//                batteryBean.setStatus(batteryStatus(status));
//                batteryBean.setTemperature(temperature / 10);
//                batteryBean.setPlugState(batteryPlugged(plugState));
//                batteryBean.setHealth(batteryHealth(health));
//                batteryBean.setPresent(present);
//                batteryBean.setTechnology(technology);
//                if (voltage > 1000) {
//                    batteryBean.setVoltage(voltage / 1000f);
//                } else {
//                    batteryBean.setVoltage(voltage);
//                }
//                batteryBean.setPower(getBatteryCapacity(context));
                batteryBean.setUsage(getBatteryUsage(context));
            }
        } catch (Exception e) {
        }
        return batteryBean;
    }

    /**
     * Get battery health status
     *
     * @param health
     * @return
     */
    private static String batteryHealth(int health) {
        String healthBat = null;
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                healthBat = "cold";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthBat = "dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthBat = "good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthBat = "overVoltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthBat = "overheat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                healthBat = "unknown";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthBat = "unspecified";
                break;
        }
        return healthBat;
    }

    /**
     * Get battery charging status
     *
     * @param status
     * @return
     */
    private static String batteryStatus(int status) {
        String healthBat = null;
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                healthBat = "charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                healthBat = "unplugged";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                healthBat = "full";
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                healthBat = "unknown";
                break;
        }
        return healthBat;
    }

    /**
     * Get battery charging method
     *
     * @param status
     * @return
     */
    private static String batteryPlugged(int status) {
        String healthBat = null;
        switch (status) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                healthBat = "ac";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                healthBat = "usb";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                healthBat = "wireless";
                break;
        }
        return healthBat;
    }
}
