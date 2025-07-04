package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;

/**
 * Device detection type
 * <p>
 * In View transmission type {@link FTRUMGlobalManager#startView(String, HashMap)}
 * Output the battery, memory, CPU, FPS, etc. of the page, through these information to judge the experience of the user browsing the current page,
 * Through observation of the cloud Studio <a href="https://docs.guance.com/real-user-monitoring/explorer/view/">View</a> for inspection
 * <p>
 * Set in {@link FTRUMConfig#setDeviceMetricsMonitorType(DeviceMetricsMonitorType, DetectFrequency)}
 * See {@link DetectFrequency } for collection frequency
 *
 * <pre>
 * //Through or operation to achieve multi-selection effect
 * DeviceMetricsMonitorType.ALL = DeviceMetricsMonitorType.ALL|DeviceMetricsMonitorType.BATTERY
 * <pre/>
 *
 * @author Brandon
 */

public enum DeviceMetricsMonitorType {


    /**
     * All types {@link #BATTERY,#MEMORY,#CPU,#FPS}
     */
    ALL(0xFFFFFFFF),
    /**
     * Battery current remaining, TV devices do not support
     * {@link Constants#KEY_BATTERY_CURRENT_AVG,
     *
     * @link Constants#KEY_BATTERY_CURRENT_MAX}
     */
    BATTERY(1 << 1),
    /**
     * Device memory usage
     * {@link Constants#KEY_MEMORY_AVG,
     *
     * @link Constants#KEY_MEMORY_MAX }
     */
    MEMORY(1 << 2),
    /**
     * Device CPU usage
     * <p>
     * {@link Constants#KEY_CPU_TICK_COUNT_PER_SECOND }
     */
    CPU(1 << 3),
    /**
     * Frame number
     * {@link Constants#KEY_FPS_AVG,
     *
     * @link Constants#KEY_FPS_MINI }
     */
    FPS(1 << 4);
    private final int value;
    /**
     * Not set, used for monitoring un-set judgment {@link  FTMonitorManager},
     */
    public static final int NO_SET = 0;


    /**
     * Internal use constructor
     *
     * @param value Integer
     */
    DeviceMetricsMonitorType(int value) {

        this.value = value;
    }

    /**
     * @return Get {@link DeviceMetricsMonitorType} integer value
     */
    public int getValue() {
        return value;
    }
}
