package com.ft.sdk.garble.bean;

public class MonitorInfoBean {
    public double avgValue;
    public double maxValue;
    public double miniValue;
    public int count = 0;


    public MonitorInfoBean() {
    }

    public MonitorInfoBean(double avgValue, double maxValue, double miniValue, int count) {
        this.avgValue = avgValue;
        this.maxValue = maxValue;
        this.miniValue = miniValue;
        this.count = count;
    }

    public MonitorInfoBean cloneData() {
        return new MonitorInfoBean(avgValue, miniValue, miniValue, count);
    }

   public boolean isValid() {
        return count > 0;
    }

    @Override
    public String toString() {
        return "MonitorInfoBean{" +
                "avgValue=" + avgValue +
                ", maxValue=" + maxValue +
                ", miniValue=" + miniValue +
                ", count=" + count +
                '}';
    }
}
