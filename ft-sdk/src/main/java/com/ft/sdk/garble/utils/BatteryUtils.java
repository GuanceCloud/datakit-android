package com.ft.sdk.garble.utils;

import android.content.Context;
import android.os.BatteryManager;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:47
 * Description:
 */
public class BatteryUtils {
    static double batteryCapacity = 0; //电池的容量mAh
    /**
     * 获取当前电量百分比
     *
     * @param context
     * @return
     */
    public static int getBatteryCurrent(Context context) {
        int capacity = 0;
        try {
            BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            capacity = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);//当前电量剩余百分比
        } catch (Exception e) {

        }
        return capacity;
    }

    /**
     * 获取电池的容量
     *
     * @param context
     * @return
     */
    public static double getBatteryTotal(Context context) {
        if(batteryCapacity > 0){
            return batteryCapacity;
        }
        Object mPowerProfile;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS).getConstructor(Context.class).newInstance(context);
            batteryCapacity = (double) Class.forName(POWER_PROFILE_CLASS).getMethod("getBatteryCapacity").invoke(mPowerProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return batteryCapacity;
    }
}
