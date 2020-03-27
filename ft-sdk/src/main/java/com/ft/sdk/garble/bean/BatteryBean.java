package com.ft.sdk.garble.bean;

/**
 * BY huangDianHua
 * DATE:2020-01-17 13:30
 * Description:
 */
public class BatteryBean {
    /**
     * 电量使用量
     */
    private int br;
    /**
     * 电池状态
     */
    private String status;

    /**
     * 电池充电状态
     */
    private String plugState;

    /**
     * 电池健康状态
     */
    private String health;

    /**
     * 是否有电池
     */
    private boolean present;
    /**
     * 电池制造技术
     */
    private String technology;
    /**
     * 电池温度
     */
    private double temperature;

    /**
     *  电池电压
     */
    private double voltage;

    /**
     * 电池电总量
     */
    private String power;

    public int getBr() {
        return br;
    }

    public void setBr(int br) {
        this.br = br;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlugState() {
        return plugState;
    }

    public void setPlugState(String plugState) {
        this.plugState = plugState;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }
}
