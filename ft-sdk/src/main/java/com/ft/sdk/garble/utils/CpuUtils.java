package com.ft.sdk.garble.utils;

import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

/**
 * BY huangDianHua
 * DATE:2020-01-08 18:58
 * Description: Get device CPU related data indicators
 */
public class CpuUtils {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "CpuUtils";

    private CpuUtils() {
    }

    /**
     * File /proc/stat
     */
    private RandomAccessFile mProcStatFile;

    /**
     * File /proc/{pid}/stat
     */
    private RandomAccessFile mAppStatFile;


    /**
     * File /proc/{pid}/stat
     */
    private RandomAccessFile mSelfStatFile;

    /**
     * CPU last jump count
     */
    private Long mLastCpuTime;

    /**
     * App CPU jump count from last time
     */
    private Long mLastAppCpuTime;
    private static CpuUtils cpuUtils;

    public synchronized static CpuUtils get() {
        if (cpuUtils == null) {
            cpuUtils = new CpuUtils();
        }
        cpuUtils.mLastCpuTime = 0L;
        cpuUtils.mLastAppCpuTime = 0L;
        return cpuUtils;
    }

    /**
     * Get current pid CPU usage rate for Android O and higher versions
     *
     * @return
     */
    public float getCPUUsageForO() {
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
                    return Float.parseFloat(cpu) / Runtime.getRuntime().availableProcessors();
                }
            }
        } catch (IOException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return 0;
    }

    /**
     * Get CPU index
     *
     * @param line
     * @return
     */
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
     * Get current pid CPU usage rate for versions below Android O
     *
     * @return
     */
    public float getCPUUsage() {
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
            String[] procStats = procStatString.split(" ");
            String[] appStats = appStatString.split(" ");
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
     * Get app CPU jump count
     *
     * @return
     */
    public long getAppCPUTickCount() {
        try {
            if (mSelfStatFile == null) {
                mSelfStatFile = new RandomAccessFile("/proc/" + android.os.Process.myPid() + "/stat", "r");
            } else {
                mSelfStatFile.seek(0);
            }
            String statString = mSelfStatFile.readLine();
            String[] procStats = statString.split(" ");
            return Long.parseLong(procStats[13]);

        } catch (Exception e) {

        }
        return -1;
    }


}
