package com.ft.sdk;

import com.ft.sdk.garble.bean.MonitorInfoBean;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.threadpool.MonitorRunnable;
import com.ft.sdk.garble.utils.FpsUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:26
 * Description:监控项配置
 */
public class FTMonitorManager {

    /**
     * 一秒，单位纳秒
     */
    private static final long ONE_SECOND_NANO_TIMES = 1000000000;

    /**
     *
     */
    private volatile static FTMonitorManager ftMonitorConfig;

    /**
     *
     */
    private int errorMonitorType = ErrorMonitorType.NO_SET;
    /**
     *
     */
    private int deviceMetricsMonitorType = DeviceMetricsMonitorType.NO_SET;


    /**
     *
     */
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

    /**
     * 初始化
     *
     * @param config
     */
    void initWithConfig(FTRUMConfig config) {
        errorMonitorType = config.getExtraMonitorTypeWithError();
        deviceMetricsMonitorType = config.getDeviceMetricsMonitorType();
        initParams();
    }

    /**
     * 初始化参数
     */
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
    public boolean isErrorMonitorType(ErrorMonitorType errorMonitorType) {
        //判断某一种监控项是否开启
        return (this.errorMonitorType | errorMonitorType.getValue()) == this.errorMonitorType;
    }

    /**
     * @param deviceMetricsMonitorType
     * @return
     */
    public boolean isDeviceMetricsMonitorType(DeviceMetricsMonitorType deviceMetricsMonitorType) {
        return (this.deviceMetricsMonitorType | deviceMetricsMonitorType.getValue()) == this.deviceMetricsMonitorType;

    }

    /**
     * 注册 viewId，为 viewId
     *
     * @param viewId View 唯一 ID ,{@link ViewBean#id}
     */
    public void addMonitor(String viewId) {
        if (deviceMetricsMonitorType == DeviceMetricsMonitorType.NO_SET) return;
        synchronized (runnerMap) {
            MonitorRunnable runner = new MonitorRunnable();
            runnerMap.put(viewId, runner);
            runner.run();
        }
    }

    /**
     * 附加监控指标数据, fps, cpu, memory
     *
     * @param bean
     */
    public void attachMonitorData(ViewBean bean) {
        String viewId = bean.getId();
        if (deviceMetricsMonitorType == DeviceMetricsMonitorType.NO_SET) return;
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
                    if (cpu.maxValue > 0) {
                        long tickCount = (long) cpu.maxValue - (long) cpu.miniValue;
                        bean.setCpuTickCount(tickCount);
                        long now = Utils.getCurrentNanoTime();
                        bean.setCpuTickCountPerSecond((double) (tickCount * ONE_SECOND_NANO_TIMES) / (double) (now - bean.getStartTime()));
                    }

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

    /**
     * 移除监控，在一个页面结束后，移除
     *
     * @param viewId
     */
    public void removeMonitor(String viewId) {
        if (deviceMetricsMonitorType == DeviceMetricsMonitorType.NO_SET) return;
        synchronized (runnerMap) {
            MonitorRunnable runnable = runnerMap.get(viewId);
            if (runnable != null) {
                runnable.stop();
            }
            runnerMap.remove(viewId);
        }
    }


    /**
     * 清除当前监控配置项
     */
    public static void release() {
        FpsUtils.release();
        ftMonitorConfig = null;
    }


}
