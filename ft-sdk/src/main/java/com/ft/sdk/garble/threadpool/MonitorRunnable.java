package com.ft.sdk.garble.threadpool;

import com.ft.sdk.DetectFrequency;
import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.FTActivityLifecycleCallbacks;
import com.ft.sdk.FTApplication;
import com.ft.sdk.FTMonitorManager;
import com.ft.sdk.garble.bean.MonitorInfoBean;
import com.ft.sdk.garble.utils.BatteryUtils;
import com.ft.sdk.garble.utils.CpuUtils;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.FpsUtils;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Application monitoring Runnable, calculates fps, cpu, memory, battery according to {@link DetectFrequency} cycle
 * Corresponds to {@link DeviceMetricsMonitorType}
 *
 * @author Brandon
 */
public class MonitorRunnable implements Runnable {

    private final DetectFrequency detectFrequency;
    private final MonitorInfoBean fpsBean = new MonitorInfoBean();
    private final MonitorInfoBean cpuBean = new MonitorInfoBean();
    private final MonitorInfoBean memoryBean = new MonitorInfoBean();
    private final MonitorInfoBean batteryBean = new MonitorInfoBean();

    private final ScheduledExecutorService service;
    private final FTMonitorManager monitorManager;
    private final Object lifecycleLock = new Object();
    private volatile boolean stopped;
    private ScheduledFuture<?> scheduledTask;

    public MonitorRunnable(DetectFrequency detectFrequency, ScheduledExecutorService service) {
        this(detectFrequency, service, FTMonitorManager.get());
    }

    public MonitorRunnable(DetectFrequency detectFrequency, ScheduledExecutorService service,
                           FTMonitorManager monitorManager) {
        this.detectFrequency = detectFrequency;
        this.service = service;
        this.monitorManager = monitorManager;
    }

    /** Stop queued collection and prevent an in-flight sample from scheduling again. */
    public void cancel() {
        stopped = true;
        synchronized (lifecycleLock) {
            if (scheduledTask != null) {
                scheduledTask.cancel(false);
                scheduledTask = null;
            }
        }
    }


    @Override
    public void run() {
        // Save resource overhead, only record data when page is in foreground
        if (stopped || !FTActivityLifecycleCallbacks.isAppInForeground()) return;
        if (monitorManager.isDeviceMetricsMonitorType(DeviceMetricsMonitorType.CPU)) {
            computeCpuBean(cpuBean, CpuUtils.get().getAppCPUTickCount());
        }

        if (monitorManager.isDeviceMetricsMonitorType(DeviceMetricsMonitorType.FPS)) {
            double fps = FpsUtils.get().getFps();
            if (fps > 0) {
                computeMonitorBean(fpsBean, fps);
            }
        }
        if (monitorManager.isDeviceMetricsMonitorType(DeviceMetricsMonitorType.MEMORY)) {
            long memory = DeviceUtils.get().getAppMemoryUseSize();
            computeMonitorBean(memoryBean, memory);
        }

        if (monitorManager.isDeviceMetricsMonitorType(DeviceMetricsMonitorType.BATTERY)) {
            computeMonitorBean(batteryBean, BatteryUtils.getBatteryCurrent(FTApplication.getApplication()));
        }

        synchronized (lifecycleLock) {
            if (stopped || service.isShutdown()) return;
            try {
                scheduledTask = service.schedule(this, detectFrequency.getValue(), TimeUnit.MILLISECONDS);
            } catch (RejectedExecutionException error) {
                // External shutdown may race with the isShutdown check.
                if (!service.isShutdown()) throw error;
                stopped = true;
            }
        }
    }


    public MonitorInfoBean getFpsBean() {
        return fpsBean;
    }

    public MonitorInfoBean getCpuBean() {
        return cpuBean;
    }

    public MonitorInfoBean getMemoryBean() {
        return memoryBean;
    }

    public MonitorInfoBean getBatteryBean() {
        return batteryBean;
    }

    /**
     * Record CPU maximum and minimum values
     *
     * @param bean
     * @param value
     */
    private void computeCpuBean(MonitorInfoBean bean, double value) {
        if (bean.count == 0) {
            bean.miniValue = value;
        } else {
            bean.maxValue = value;
        }
        bean.count = bean.count + 1;

    }

    /**
     * Calculate maximum, minimum, and average values
     *
     * @param bean
     * @param lastValue Latest value
     */
    private void computeMonitorBean(MonitorInfoBean bean, double lastValue) {
        int count = bean.count + 1;
        bean.avgValue = (lastValue + (bean.count * bean.avgValue)) / count;
        bean.maxValue = Math.max(lastValue, bean.maxValue);
        bean.miniValue = Math.min(lastValue, bean.miniValue);
        bean.count = count;
    }

}
