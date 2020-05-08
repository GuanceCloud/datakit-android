package com.ft.sdk.garble.utils;

import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2020-01-08 18:58
 * Description:
 */
public class CpuUtils {
    private CpuUtils() {
    }
    public final int DEVICEINFO_UNKNOWN = -1;
    private RandomAccessFile mProcStatFile;
    private RandomAccessFile mAppStatFile;
    private Long mLastCpuTime;
    private Long mLastAppCpuTime;
    private static CpuUtils cpuUtils;
    private int maxFreq = DEVICEINFO_UNKNOWN;

    //常见获取 CPU 温度的系统文件路径,TODO 当获取不到温度时尝试扩展这个文件路径集合
    //参考1：https://blog.csdn.net/willway_wang/article/details/87599122
    //参考2：https://github.com/kamgurgul/cpu-info
    private final List<String> CPU_TEMP_FILE_PATHS = Arrays.asList(
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
            "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/devices/platform/tegra_tmon/temp1_input",
            "/sys/kernel/debug/tegra_thermal/temp_tj",
            "/sys/devices/platform/s5p-tmu/temperature",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/hwmon/hwmon0/device/temp1_input",
            "/sys/devices/virtual/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone3/temp",
            "/sys/class/thermal/thermal_zone4/temp",
            "/sys/class/hwmon/hwmonX/temp1_input",
            "/sys/devices/platform/s5p-tmu/curr_temp"
    );

    public synchronized static CpuUtils get() {
        if (cpuUtils == null) {
            cpuUtils = new CpuUtils();
        }
        cpuUtils.mLastCpuTime = 0L;
        cpuUtils.mLastAppCpuTime = 0L;
        return cpuUtils;
    }

    /**
     * 获取 CPU 核数
     * https://blog.csdn.net/gundumw100/article/details/69997479
     *
     * @return
     */
    public int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return 1;
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = DEVICEINFO_UNKNOWN;
        } catch (NullPointerException e) {
            cores = DEVICEINFO_UNKNOWN;
        }
        return cores;
    }

    private final FileFilter CPU_FILTER = pathname -> {
        String path = pathname.getName();
        //regex is slow, so checking char by char.
        if (path.startsWith("cpu")) {
            for (int i = 3; i < path.length(); i++) {
                if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                    return false;
                }
            }
            return true;
        }
        return false;
    };


    /**
     * 获取 CPU 最大频率
     *
     * @return
     */
    public String getCPUMaxFreqKHz() {
        if(maxFreq != DEVICEINFO_UNKNOWN){
            return maxFreq+"Hz";
        }
        try {
            for (int i = 0; i < getNumberOfCPUCores(); i++) {
                String filename =
                        "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
                File cpuInfoMaxFreqFile = new File(filename);
                if (cpuInfoMaxFreqFile.exists()) {
                    byte[] buffer = new byte[128];
                    FileInputStream stream = new FileInputStream(cpuInfoMaxFreqFile);
                    try {
                        stream.read(buffer);
                        int endIndex = 0;
                        //Trim the first number out of the byte buffer.
                        while (buffer[endIndex] >= '0' && buffer[endIndex] <= '9'
                                && endIndex < buffer.length) endIndex++;
                        String str = new String(buffer, 0, endIndex);
                        Integer freqBound = Integer.parseInt(str);
                        if (freqBound > maxFreq) maxFreq = freqBound;
                    } catch (NumberFormatException e) {
                        //Fall through and use /proc/cpuinfo.
                    } finally {
                        stream.close();
                    }
                }
            }
            if (maxFreq == DEVICEINFO_UNKNOWN) {
                FileInputStream stream = new FileInputStream("/proc/cpuinfo");
                try {
                    int freqBound = parseFileForValue("cpu MHz", stream);
                    freqBound *= 1000; //MHz -> kHz
                    if (freqBound > maxFreq) maxFreq = freqBound;
                } finally {
                    stream.close();
                }
            }
        } catch (IOException e) {
            maxFreq = DEVICEINFO_UNKNOWN; //Fall through and return unknown.
        }
        if(maxFreq >0){
            return maxFreq+"Hz";
        }
        return Constants.UNKNOWN;
    }

    private int parseFileForValue(String textToMatch, FileInputStream stream) {
        byte[] buffer = new byte[1024];
        try {
            int length = stream.read(buffer);
            for (int i = 0; i < length; i++) {
                if (buffer[i] == '\n' || i == 0) {
                    if (buffer[i] == '\n') i++;
                    for (int j = i; j < length; j++) {
                        int textIndex = j - i;
                        //Text doesn't match query at some point.
                        if (buffer[j] != textToMatch.charAt(textIndex)) {
                            break;
                        }
                        //Text matches query here.
                        if (textIndex == textToMatch.length() - 1) {
                            return extractValue(buffer, j);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //Ignore any exceptions and fall through to return unknown value.
        } catch (NumberFormatException e) {
        }
        return DEVICEINFO_UNKNOWN;
    }

    private int extractValue(byte[] buffer, int index) {
        while (index < buffer.length && buffer[index] != '\n') {
            if (buffer[index] >= '0' && buffer[index] <= '9') {
                int start = index;
                index++;
                while (index < buffer.length && buffer[index] >= '0' && buffer[index] <= '9') {
                    index++;
                }
                String str = new String(buffer, 0, start, index - start);
                return Integer.parseInt(str);
            }
            index++;
        }
        return DEVICEINFO_UNKNOWN;
    }

    /**
     * 获取 Android O 及更高版本的CPU使用率
     *
     * @return
     */
    public float getCpuDataForO() {
        java.lang.Process process = null;
        try {
            process = Runtime.getRuntime().exec("top -n 1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int cpuIndex = -1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                int tempIndex = getCPUIndex(line);
                if (tempIndex != -1) {
                    cpuIndex = tempIndex;
                    continue;
                }
                if (line.startsWith(String.valueOf(Process.myPid()))) {
                    if (cpuIndex == -1) {
                        continue;
                    }
                    String[] param = line.split("\\s+");
                    if (param.length <= cpuIndex) {
                        continue;
                    }
                    String cpu = param[cpuIndex];
                    if (cpu.endsWith("%")) {
                        cpu = cpu.substring(0, cpu.lastIndexOf("%"));
                    }
                    float rate = Float.parseFloat(cpu) / Runtime.getRuntime().availableProcessors();
                    return rate;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return 0;
    }

    private int getCPUIndex(String line) {
        if (line.contains("CPU")) {
            String[] titles = line.split("\\s+");
            for (int i = 0; i < titles.length; i++) {
                if (titles[i].contains("CPU")) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 获取 Android O 以下版本的CPU使用率
     *
     * @return
     */
    public float getCPUData() {
        long cpuTime;
        long appTime;
        float value = 0.0f;
        try {
            if (mProcStatFile == null || mAppStatFile == null) {
                mProcStatFile = new RandomAccessFile("/proc/stat", "r");
                mAppStatFile = new RandomAccessFile("/proc/" + android.os.Process.myPid() + "/stat", "r");
            } else {
                mProcStatFile.seek(0L);
                mAppStatFile.seek(0L);
            }
            String procStatString = mProcStatFile.readLine();
            String appStatString = mAppStatFile.readLine();
            String procStats[] = procStatString.split(" ");
            String appStats[] = appStatString.split(" ");
            cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3])
                    + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5])
                    + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7])
                    + Long.parseLong(procStats[8]);
            appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
            if (mLastCpuTime == null && mLastAppCpuTime == null) {
                mLastCpuTime = cpuTime;
                mLastAppCpuTime = appTime;
                return value;
            }
            value = ((float) (appTime - mLastAppCpuTime) / (float) (cpuTime - mLastCpuTime)) * 100f;
            mLastCpuTime = cpuTime;
            mLastAppCpuTime = appTime;
        } catch (Exception e) {

        }
        return value;
    }

    /**
     * 获得 CPU 的温度
     *
     * @return
     */
    public String getCpuTemperature() {
        double currentTemp = 0.0D;
        for (String path : CPU_TEMP_FILE_PATHS) {
            try {
                Double temp = readOnLine(new File(path));
                if (isTemperatureValid(temp)) {
                    currentTemp = temp;
                } else if (isTemperatureValid(temp / (double) 1000)) {
                    currentTemp = temp / (double) 1000;
                }
                if (currentTemp != 0) {
                    break;
                }
            } catch (Exception e) {
            }
        }
        if(currentTemp == 0){
            return Constants.UNKNOWN;
        }
        return currentTemp+"℃";
    }

    private double readOnLine(File file) {
        FileInputStream fileInputStream = null;
        String s = "";
        try {
            fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            s = bufferedReader.readLine();
            fileInputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
        } catch (Exception e) {
        }
        double result = 0;
        try {
            result = Double.parseDouble(s);
        } catch (NumberFormatException e) {
        }
        return result;
    }

    private boolean isTemperatureValid(double temp) {
        return temp >= -30.0D && temp <= 250.0D;
    }
}
