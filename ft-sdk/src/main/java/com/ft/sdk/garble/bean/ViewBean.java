package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class ViewBean {
    String id = UUID.randomUUID().toString();
    String viewReferrer;
    String viewName;

    int longTaskCount;
    int resourceCount;
    int errorCount;
    int actionCount;

    boolean isClose = false;
    long startTime = Utils.getCurrentNanoTime();
    long loadTime = 0;
    long timeSpent = 0;

    double fpsMini;
    double fpsAvg;

    long cpuTickCountAvg;
    long cpuTickCountMax;

    long memoryAvg;
    long memoryMax;

    int batteryCurrentAvg;
    int batteryCurrentMax;

    String sessionId;

    public String getViewReferrer() {
        return viewReferrer;
    }

    public String getViewName() {
        return viewName;
    }

    public String getId() {
        return id;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isClose() {
        return isClose;
    }

    public int getLongTaskCount() {
        return longTaskCount;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    public long getLoadTime() {
        return loadTime;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getActionCount() {
        return actionCount;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setViewReferrer(String viewReferrer) {
        this.viewReferrer = viewReferrer;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void setLongTaskCount(int longTaskCount) {
        this.longTaskCount = longTaskCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public void setActionCount(int actionCount) {
        this.actionCount = actionCount;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setLoadTime(long loadTime) {
        this.loadTime = loadTime;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public double getFpsMini() {
        return fpsMini;
    }

    public void setFpsMini(double fpsMini) {
        this.fpsMini = fpsMini;
    }

    public double getFpsAvg() {
        return fpsAvg;
    }

    public void setFpsAvg(double fpsAvg) {
        this.fpsAvg = fpsAvg;
    }

    public long getCpuTickCountAvg() {
        return cpuTickCountAvg;
    }

    public void setCpuTickCountAvg(long cpuTickCountAvg) {
        this.cpuTickCountAvg = cpuTickCountAvg;
    }

    public long getCpuTickCountMax() {
        return cpuTickCountMax;
    }

    public void setCpuTickCountMax(long cpuTickCountMax) {
        this.cpuTickCountMax = cpuTickCountMax;
    }

    public long getMemoryAvg() {
        return memoryAvg;
    }

    public void setMemoryAvg(long memoryAvg) {
        this.memoryAvg = memoryAvg;
    }

    public long getMemoryMax() {
        return memoryMax;
    }

    public void setMemoryMax(long memoryMax) {
        this.memoryMax = memoryMax;
    }

    public int getBatteryCurrentAvg() {
        return batteryCurrentAvg;
    }

    public void setBatteryCurrentAvg(int batteryCurrentAvg) {
        this.batteryCurrentAvg = batteryCurrentAvg;
    }

    public int getBatteryCurrentMax() {
        return batteryCurrentMax;
    }

    public void setBatteryCurrentMax(int batteryCurrentMax) {
        this.batteryCurrentMax = batteryCurrentMax;
    }

    public String getAttrJsonString() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.KEY_BATTERY_CURRENT_AVG, batteryCurrentAvg);
        map.put(Constants.KEY_BATTERY_CURRENT_MAX, batteryCurrentMax);
        map.put(Constants.KEY_FPS_AVG, fpsAvg);
        map.put(Constants.KEY_FPS_MINI, fpsMini);
        map.put(Constants.KEY_CPU_TICK_COUNT_AVG, cpuTickCountAvg);
        map.put(Constants.KEY_CPU_TICK_COUNT_MAX, cpuTickCountMax);
        map.put(Constants.KEY_MEMORY_AVG, memoryAvg);
        map.put(Constants.KEY_MEMORY_MAX, memoryMax);
        return new Gson().toJson(map);
    }

    public void setFromAttrJsonString(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            this.batteryCurrentAvg = json.optInt(Constants.KEY_BATTERY_CURRENT_AVG);
            this.batteryCurrentMax = json.getInt(Constants.KEY_BATTERY_CURRENT_MAX);
            this.fpsAvg = json.optDouble(Constants.KEY_FPS_AVG);
            this.fpsMini = json.getInt(Constants.KEY_FPS_MINI);
            this.memoryAvg = json.optLong(Constants.KEY_MEMORY_AVG);
            this.memoryMax = json.optLong(Constants.KEY_MEMORY_MAX);
            this.cpuTickCountAvg = json.optLong(Constants.KEY_CPU_TICK_COUNT_AVG);
            this.cpuTickCountMax = json.optLong(Constants.KEY_CPU_TICK_COUNT_MAX);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @NonNull
    @Override
    public String toString() {
        return "ViewBean{" +
                "id='" + id + '\'' +
                ", viewReferrer='" + viewReferrer + '\'' +
                ", viewName='" + viewName + '\'' +
                ", longTaskCount=" + longTaskCount +
                ", resourceCount=" + resourceCount +
                ", errorCount=" + errorCount +
                ", actionCount=" + actionCount +
                ", isClose=" + isClose +
                ", startTime=" + startTime +
                ", loadTime=" + loadTime +
                ", timeSpent=" + timeSpent +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }

}
