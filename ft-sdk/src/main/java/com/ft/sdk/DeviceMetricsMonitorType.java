package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;

/**
 * 设备检测类型
 * <p>
 * 在 View 传输类型中 {@link FTRUMGlobalManager#startView(String, HashMap)}
 * 输出页面的电池、内存、CPU、FPS 等信息，通过这些信息来判断用户在当前页面浏览的体验情况
 * ，通过观测云 Studio <a href="https://docs.guance.com/real-user-monitoring/explorer/view/">查看器 View</a> 进行查看
 * <p>
 * 在 {@link FTRUMConfig#setDeviceMetricsMonitorType(DeviceMetricsMonitorType, DetectFrequency)} 进行设置
 * 采集频率请看 {@link DetectFrequency }
 *
 * <pre>
 * //通过或运算来达到多选效果
 * DeviceMetricsMonitorType.ALL = DeviceMetricsMonitorType.ALL|DeviceMetricsMonitorType.BATTERY
 * <pre/>
 *
 * @author Brandon
 */

public enum DeviceMetricsMonitorType {


    /**
     * 所有类型 {@link #BATTERY,#MEMORY,#CPU,#FPS}
     */
    ALL(0xFFFFFFFF),
    /**
     * 电池当前剩余量
     * {@link Constants#KEY_BATTERY_CURRENT_AVG,
     *
     * @link Constants#KEY_BATTERY_CURRENT_MAX}
     */
    BATTERY(1 << 1),
    /**
     * 设备内存用度
     * {@link Constants#KEY_MEMORY_AVG,
     *
     * @link Constants#KEY_MEMORY_MAX }
     */
    MEMORY(1 << 2),
    /**
     * 设备 CPU 使用度
     * <p>
     * {@link Constants#KEY_CPU_TICK_COUNT_PER_SECOND }
     */
    CPU(1 << 3),
    /**
     * 帧数
     * {@link Constants#KEY_FPS_AVG,
     *
     * @link Constants#KEY_FPS_MINI }
     */
    FPS(1 << 4);
    private final int value;
    /**
     * 未设置, 用于监控未设置判断 {@link  FTMonitorManager}，
     */
    public static final int NO_SET = 0;


    /**
     * 内部是使用构造函数
     *
     * @param value 整型
     */
    DeviceMetricsMonitorType(int value) {

        this.value = value;
    }

    /**
     * @return 获取 {@link DeviceMetricsMonitorType} 整型数值
     */
    public int getValue() {
        return value;
    }
}
