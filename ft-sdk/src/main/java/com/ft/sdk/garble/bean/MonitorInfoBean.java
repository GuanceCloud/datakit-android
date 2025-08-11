package com.ft.sdk.garble.bean;

/**
 * Used to calculate the max, min, and average values of monitoring data metrics
 */
public class MonitorInfoBean {
    /**
     * Average value
     */
    public double avgValue;
    /**
     * Maximum value
     */
    public double maxValue;
    /**
     * Minimum value
     */
    public double miniValue = Double.MAX_VALUE;

    /**
     * Add data count
     */
    public int count = 0;


    public MonitorInfoBean() {
    }

//    public MonitorInfoBean(double avgValue, double maxValue, double miniValue, int count) {
//        this.avgValue = avgValue;
//        this.maxValue = maxValue;
//        this.miniValue = miniValue;
//        this.count = count;
//    }
//
//    public MonitorInfoBean cloneData() {
//        return new MonitorInfoBean(avgValue, miniValue, miniValue, count);
//    }

    /**
     * Determine whether the calculated value is legal, illegal values will be discarded
     * @return
     */

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
