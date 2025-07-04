package com.ft.sdk.garble.bean;

/**
 * BY huangDianHua
 * DATE:2020-01-17 13:30
 * Description: Battery metric data
 */
public class BatteryBean {
    /**
     * Battery usage
     */
    private int usage;

    /**
     * Whether in power saving mode
     */
    private boolean isSaveMode;

    /**
     * Whether the battery exists
     */
    private boolean isBatteryPresent;

    /**
     * {@link android.os.BatteryManager#BATTERY_STATUS_FULL}
     * {@link android.os.BatteryManager#BATTERY_STATUS_NOT_CHARGING}
     * {@link android.os.BatteryManager#BATTERY_STATUS_DISCHARGING}
     * {@link android.os.BatteryManager#BATTERY_STATUS_CHARGING}
     */
    private int batteryStatue;


    /**
     * Battery charging status
     * {@link android.os.BatteryManager#BATTERY_PLUGGED_AC}
     * {@link android.os.BatteryManager#BATTERY_PLUGGED_USB}
     * {@link android.os.BatteryManager#BATTERY_PLUGGED_WIRELESS}
     */
    private int plugState;

    /**
     * Battery remaining
     */
    private int level;

    public int getUsage() {
        return usage;
    }

    public void setUsage(int br) {
        this.usage = br;
    }

    public boolean isSaveMode() {
        return isSaveMode;
    }

    public void setSaveMode(boolean saveMode) {
        isSaveMode = saveMode;
    }

    public int getPlugState() {
        return plugState;
    }

    public void setPlugState(int plugState) {
        this.plugState = plugState;
    }

    public boolean isBatteryPresent() {
        return isBatteryPresent;
    }

    public void setBatteryPresent(boolean batteryPresent) {
        isBatteryPresent = batteryPresent;
    }

    public int getBatteryStatue() {
        return batteryStatue;
    }

    public void setBatteryStatue(int batteryStatue) {
        this.batteryStatue = batteryStatue;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.usage = 100 - level;
    }

}
