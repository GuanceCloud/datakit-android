package com.ft.sdk.garble.threadpool;

import com.ft.sdk.DetectFrequency;
import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.FTActivityManager;
import com.ft.sdk.FTApplication;
import com.ft.sdk.FTMonitorManager;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.garble.bean.MonitorInfoBean;
import com.ft.sdk.garble.utils.BatteryUtils;
import com.ft.sdk.garble.utils.CpuUtils;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.FpsUtils;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MonitorRunnable implements Runnable {
    private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
    private final DetectFrequency detectFrequency;
    private final MonitorInfoBean fpsBean = new MonitorInfoBean();
    private final MonitorInfoBean cpuBean = new MonitorInfoBean();
    private final MonitorInfoBean memoryBean = new MonitorInfoBean();
    private final MonitorInfoBean batteryBean = new MonitorInfoBean();


    public MonitorRunnable() {
        detectFrequency = FTRUMConfigManager.get().getConfig().getDeviceMetricsDetectFrequency();
    }


    @Override
    public void run() {
        if (!FTActivityManager.get().isAppForeground()) return;
        if (FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.CPU)) {
            computeCpuBean(cpuBean, CpuUtils.get().getAppCPUTickCount());
        }

        if (FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.FPS)) {
            double fps = FpsUtils.get().getFps();
            if (fps > 0) {
                computeMonitorBean(fpsBean, fps);
            }
        }
        if (FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.MEMORY)) {
            long memory = DeviceUtils.get().getAppMemoryUseSize();
            computeMonitorBean(memoryBean, memory);
        }

        if (FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.BATTERY)) {
            computeMonitorBean(batteryBean, BatteryUtils.getBatteryCurrent(FTApplication.getApplication()));
        }

        service.schedule(this, detectFrequency.getValue(), TimeUnit.MILLISECONDS);
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

    private void computeCpuBean(MonitorInfoBean bean, double value) {
        if (bean.count == 0) {
            bean.miniValue = value;
        } else {
            bean.maxValue = value;
        }
        bean.count = bean.count + 1;

    }

    private void computeMonitorBean(MonitorInfoBean bean, double lastValue) {
        int count = bean.count + 1;
        bean.avgValue = (lastValue + (bean.count * bean.avgValue)) / count;
        bean.maxValue = Math.max(lastValue, bean.maxValue);
        bean.miniValue = Math.min(lastValue, bean.miniValue);
        bean.count = count;
    }

    public void stop() {
        service.shutdown();
    }

}
