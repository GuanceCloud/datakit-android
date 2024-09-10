package com.ft.sdk.garble.bean;

/**
 * BY huangDianHua
 * DATE:2020-01-17 13:30
 * Description: 电池指标数据
 */
public class BatteryBean {
    /**
     * 电量使用量
     */
    private int usage;

    /**
     * 是否在省电模式
     */
    private boolean isSaveMode;

    /**
     * 电池是否存在
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
     * 电池充电状态
     * {@link android.os.BatteryManager#BATTERY_PLUGGED_AC}
     * {@link android.os.BatteryManager#BATTERY_PLUGGED_USB}
     * {@link android.os.BatteryManager#BATTERY_PLUGGED_WIRELESS}
     */
    private int plugState;

    /**
     * 电池剩余
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
