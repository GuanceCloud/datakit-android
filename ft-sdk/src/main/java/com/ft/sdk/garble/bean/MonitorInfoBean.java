package com.ft.sdk.garble.bean;

/**
 * 用于计算监控数据指标最大最小值，和平均值
 */
public class MonitorInfoBean {
    /**
     * 平均值
     */
    public double avgValue;
    /**
     * 最大数值
     */
    public double maxValue;
    /**
     * 最小值
     */
    public double miniValue = Double.MAX_VALUE;

    /**
     * 添加数据数量
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
     * 判断计算数值是否合法，非法数值会被舍弃
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
