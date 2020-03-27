package com.ft.sdk.garble.utils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GpuUtils {
    /**
     * 最大频率路径
     */
    private static final String GPU_MAX_FREQ_HUAWEI = "/sys/class/devfreq/gpufreq/max_freq";
    private static final String GPU_MAX_FREQ_MTK = "/proc/gpufreq/gpufreq_opp_dump";
    private static final String GPU_MAX_FREQ_QUALCOMM = "/sys/class/kgsl/kgsl-3d0/devfreq/max_freq";

    /**
     * 高通平台 GPU 使用率路径
     */
    private static final String GPU_USE_QUALCOMM = "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage";
    /**
     * MTK GPU 使用率路径
     */
    private static final String GPU_USE_MTK = "/d/ged/hal/gpu_utilization";

    /**
     * GPU 供应商，型号
     */
    public static String GPU_VENDOR_RENDERER = Constants.UNKNOWN;

    /**
     * 获得GPU使用率
     *
     * @return
     */
    public static String getGpuUseRate() {
        String rate = readOneLineFile(GPU_USE_QUALCOMM);
        if (rate == null) {
            rate = readOneLineFile(GPU_USE_MTK);
        }
        if (Utils.isNullOrEmpty(rate)) {
            return Constants.UNKNOWN;
        }

        return rate+"%";
    }

    /**
     * 获取GPU的最大频率
     *
     * @return
     */
    public static String getGpuMaxFreq() {
        String maxFreq = readOneLineFile(GPU_MAX_FREQ_HUAWEI);
        if (maxFreq == null) {
            maxFreq = readOneLineFile(GPU_MAX_FREQ_QUALCOMM);
        }
        if (maxFreq == null) {
            List<String> datas = readFile(GPU_MAX_FREQ_MTK);
            if (datas != null && !datas.isEmpty()) {
                maxFreq = datas.get(0);
            }
        }
        if (Utils.isNullOrEmpty(maxFreq)) {
            return Constants.UNKNOWN;
        }
        return maxFreq+"Hz";
    }

    /**
     * 读取一行数据
     *
     * @param path
     * @return
     */
    private static String readOneLineFile(String path) {
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(path));
            line = br.readLine();
            br.close();
        } catch (Exception e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    /**
     * 读取所有行数据
     *
     * @param path
     * @return
     */
    public static List<String> readFile(String path) {
        List<String> result = new ArrayList<>();

        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            br.close();
        } catch (IOException e) {
        }

        return result;
    }
}
