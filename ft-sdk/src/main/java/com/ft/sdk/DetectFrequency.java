package com.ft.sdk;

import com.ft.sdk.garble.threadpool.MonitorRunnable;
import com.ft.sdk.garble.utils.Constants;

/**
 * Detection period, unit: milliseconds (MS)
 * <p>
 * It is recommended to use the {@link #DEFAULT} configuration for collection frequency. {@link #FREQUENT} provides more accurate values but consumes more performance, recommended for non-production environments. {@link #RARE} has the lowest consumption but also the lowest accuracy, and increases the possibility of losing monitoring data due to rapid page switching.
 * <p>
 * Applied to {@link MonitorRunnable}, affects the <a href="https://docs.guance.com/real-user-monitoring/explorer/">Explorer</a>
 * {@link Constants#KEY_CPU_TICK_COUNT_PER_SECOND}
 * {@link Constants#KEY_CPU_TICK_COUNT}
 * {@link Constants#KEY_MEMORY_MAX}
 * {@link Constants#KEY_MEMORY_AVG}
 * {@link Constants#KEY_FPS_AVG}
 * {@link Constants#KEY_FPS_MINI}
 * Display accuracy
 * <p>
 *  Set via {@link FTRUMConfig#setDeviceMetricsMonitorType(DeviceMetricsMonitorType, DetectFrequency)}
 *
 * For collection types, see {@link DeviceMetricsMonitorType}
 *
 * @author Brandon
 */
public enum DetectFrequency {

    /**
     * Default detection frequency: once every 500 ms
     */
    DEFAULT(500),

    /**
     * High-frequency collection: once every 100 ms
     */
    FREQUENT(100),

    /**
     * Low-frequency collection: once every 1000 ms
     */
    RARE(1000);

    private final long value;

    /**
     * @param periodTime Event period
     */
    DetectFrequency(long periodTime) {
        this.value = periodTime;
    }

    /**
     * @return Get monitoring period, unit: milliseconds
     */
    public long getValue() {
        return value;
    }
}

