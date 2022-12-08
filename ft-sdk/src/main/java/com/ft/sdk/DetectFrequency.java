package com.ft.sdk;

import com.ft.sdk.garble.threadpool.MonitorRunnable;
import com.ft.sdk.garble.utils.Constants;

/**
 * 检测周期，单位毫秒 MS
 * <p>
 * 推荐使用 {@link #DEFAULT}配置，采集频率, {@link #FREQUENT}，采集获得数值更精确，但也会损耗更多性能，建议在非生产环境中使用，
 * {@link #RARE} 损耗最低，但是采集精度最低，也会提升因快速切换页面丢失监控数据的可能
 * <p>
 * 应用于 {@link MonitorRunnable}，影响 <a href="https://docs.guance.com/real-user-monitoring/explorer/">查看器</a>
 * 中
 * {@link Constants#KEY_CPU_TICK_COUNT_PER_SECOND}
 * {@link Constants#KEY_CPU_TICK_COUNT}
 * {@link Constants#KEY_MEMORY_MAX}
 * {@link Constants#KEY_MEMORY_AVG}
 * {@link Constants#KEY_FPS_AVG}
 * {@link Constants#KEY_FPS_MINI}
 * 显示精准度
 * <p>
 *  通过在 {@link FTRUMConfig#setDeviceMetricsMonitorType(DeviceMetricsMonitorType, DetectFrequency)} 进行设置
 *
 * 采集类型请查阅 {@link DeviceMetricsMonitorType}
 *
 * @author Brandon
 */
public enum DetectFrequency {

    /**
     * 默认检测频率 500 ms 一次
     */
    DEFAULT(500),

    /**
     * 高频采集，100 ms 一次
     */
    FREQUENT(100),

    /**
     * 低频率采集 1000 ms 一次
     */
    RARE(1000);

    private final long value;

    /**
     * @param periodTime 事件周期
     */
    DetectFrequency(long periodTime) {

        this.value = periodTime;
    }

    /**
     * @return 获取监测周期，单位 毫秒
     */
    public long getValue() {
        return value;
    }
}

