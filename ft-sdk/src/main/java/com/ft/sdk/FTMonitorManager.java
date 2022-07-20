package com.ft.sdk;

import android.view.View;

import com.ft.sdk.garble.bean.MonitorInfoBean;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.threadpool.MonitorRunnable;
import com.ft.sdk.garble.utils.BatteryUtils;
import com.ft.sdk.garble.utils.CpuUtils;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.FpsUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:26
 * Description:监控项配置
 */
public class FTMonitorManager {

    private static final int NOT_SET = 0;
    private volatile static FTMonitorManager ftMonitorConfig;
    private int errorMonitorType;
    private int deviceMetricsMonitorType = NOT_SET;


    private final HashMap<String, MonitorRunnable> runnerMap = new HashMap<>();

    private FTMonitorManager() {
    }

    public static FTMonitorManager get() {
        synchronized (FTMonitorManager.class) {
            if (ftMonitorConfig == null) {
                ftMonitorConfig = new FTMonitorManager();
            }
            return ftMonitorConfig;
        }
    }

    void initWithConfig(FTRUMConfig config) {
        errorMonitorType = config.getExtraMonitorTypeWithError();
        deviceMetricsMonitorType = config.getDeviceMetricsMonitorType();
        initParams();
    }

    private void initParams() {
        if (isDeviceMetricsMonitorType(DeviceMetricsMonitorType.FPS)) {
            FpsUtils.get().start();
        }

    }

    /**
     * 判断某种类型的监控是否开启
     *
     * @param errorMonitorType
     * @return
     */
    public boolean isErrorMonitorType(int errorMonitorType) {
        //判断某一种监控项是否开启
        return (this.errorMonitorType | errorMonitorType) == this.errorMonitorType;
    }

    public boolean isDeviceMetricsMonitorType(int deviceMetricsMonitorType) {
        return (this.deviceMetricsMonitorType | deviceMetricsMonitorType) == this.deviceMetricsMonitorType;

    }

    public void addMonitor(String viewId) {
        if (deviceMetricsMonitorType == NOT_SET) return;
        synchronized (runnerMap) {
            MonitorRunnable runner = new MonitorRunnable();
            runnerMap.put(viewId, runner);
            runner.run();
        }
    }

    public void attachMonitorData(ViewBean bean) {
        String viewId = bean.getId();
        if (deviceMetricsMonitorType == NOT_SET) return;
        synchronized (runnerMap) {
            MonitorRunnable runnable = runnerMap.get(viewId);
            if (runnable != null) {
                MonitorInfoBean battery = runnable.getBatteryBean();
                if (battery.isValid()) {
                    bean.setBatteryCurrentMax((int) battery.maxValue);
                    bean.setBatteryCurrentAvg((int) battery.avgValue);
                }
                MonitorInfoBean cpu = runnable.getCpuBean();
                if (cpu.isValid()) {
                    bean.setCpuTickCountMax((long) cpu.maxValue);
                    bean.setCpuTickCountAvg((long) cpu.avgValue);
                }
                MonitorInfoBean fps = runnable.getFpsBean();
                if (fps.isValid()) {
                    bean.setFpsMini(fps.miniValue);
                    bean.setFpsAvg(fps.avgValue);
                }
                MonitorInfoBean memory = runnable.getMemoryBean();
                if (memory.isValid()) {
                    bean.setMemoryMax((long) memory.maxValue);
                    bean.setMemoryAvg((long) memory.avgValue);
                }

            }
        }
    }

    public void removeMonitor(String viewId) {
        if (deviceMetricsMonitorType == NOT_SET) return;
        synchronized (runnerMap) {
            MonitorRunnable runnable = runnerMap.get(viewId);
            if (runnable != null) {
                runnable.stop();
            }
            runnerMap.remove(viewId);
        }
    }


    /**
     * 清楚当前监控配置项
     */
    public static void release() {
        FpsUtils.get().release();
        ftMonitorConfig = null;
    }


}
