package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * View 指标数据，以下数据为页面页面生命周期内容产生的数据指标，从 {@link FTRUMInnerManager#startView(String)} 到
 * <p>
 * {@link FTRUMInnerManager#stopView()}  }
 *
 * @author Brandon
 */
public class ViewBean {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "ViewBean";
    /**
     * View 唯一ID， {@link Constants#KEY_RUM_VIEW_ID}
     */
    String id = Utils.randomUUID();
    /**
     * 页面来源，页面的父级，{@link Constants#KEY_RUM_VIEW_REFERRER}
     */
    String viewReferrer;
    /**
     * 页面名称，{@link Constants#KEY_RUM_VIEW_NAME}
     */
    String viewName;

    /**
     * longtask 捕获到的次数，{@link Constants#KEY_RUM_ACTION_LONG_TASK_COUNT}
     */
    int longTaskCount;

    /**
     * 网络资源请求的次数,{@link Constants#KEY_RUM_VIEW_RESOURCE_COUNT}
     */
    int resourceCount;
    /**
     * 发生错误的次数，{@link Constants#KEY_RUM_VIEW_ERROR_COUNT}
     */
    int errorCount;

    /**
     * 捕获 Action 的次数 {@link  Constants#KEY_RUM_VIEW_ACTION_COUNT}
     */
    int actionCount;

    /**
     * 是否处于激活状态， {@link  Constants#KEY_RUM_VIEW_IS_ACTIVE}
     */
    boolean isClose = false;
    /**
     * 开始时间，单位纳秒
     */
    long startTime = Utils.getCurrentNanoTime();

    /**
     * 页面加载时间，单位纳秒，{@link Constants#KEY_RUM_VIEW_LOAD}
     */
    long loadTime = 0;

    /**
     * 页面停留时间，单位纳秒，{@link Constants#KEY_RUM_VIEW_TIME_SPENT}
     */
    long timeSpent = 0;

    /**
     * 最小帧数, {@link  Constants#KEY_FPS_MINI}
     */
    double fpsMini;

    /**
     * 页面平均帧数,{@link  Constants#KEY_FPS_AVG}
     */
    double fpsAvg;

    /**
     * cpu 每秒跳动次数,{@link  Constants#KEY_CPU_TICK_COUNT_PER_SECOND}
     */
    double cpuTickCountPerSecond = -1;

    /**
     * cpu 跳动次数,{@link  Constants#KEY_CPU_TICK_COUNT}
     */
    long cpuTickCount = -1;

    /**
     * 平均内存,{@link  Constants#KEY_MEMORY_AVG}
     */
    long memoryAvg;

    /**
     * 最大内存，,{@link  Constants#KEY_MEMORY_MAX}
     */
    long memoryMax;

    /**
     * 平均电池消耗,{@link  Constants#KEY_BATTERY_CURRENT_AVG}
     */
    int batteryCurrentAvg;
    /**
     * 电池最最大消耗,{@link  Constants#KEY_BATTERY_CURRENT_MAX}
     */
    int batteryCurrentMax;

    /**
     * {@link FTRUMInnerManager#sessionId},{@link Constants#KEY_RUM_SESSION_ID}
     */
    String sessionId;


    /**
     * 页面更新次数,{@link  Constants#KEY_SDK_VIEW_UPDATE_TIME}
     */
    long viewUpdateTime = 0;


    public long getViewUpdateTime() {
        return viewUpdateTime;
    }

    public void setViewUpdateTime(long viewUpdateTime) {
        this.viewUpdateTime = viewUpdateTime;
    }

    /**
     * 页面附加属性,{@link  Constants#KEY_RUM_PROPERTY}
     */
    HashMap<String, Object> property = new HashMap<>();

    public HashMap<String, Object> getProperty() {
        return property;
    }

    HashMap<String, Object> tags = new HashMap<>();

    public HashMap<String, Object> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, Object> tags) {
        this.tags = tags;
    }

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

    public double getCpuTickCountPerSecond() {
        return cpuTickCountPerSecond;
    }

    public void setCpuTickCountPerSecond(double cpuTickCountPerSecond) {
        this.cpuTickCountPerSecond = cpuTickCountPerSecond;
    }

    public long getCpuTickCount() {
        return cpuTickCount;
    }

    public void setCpuTickCount(long cpuTickCount) {
        this.cpuTickCount = cpuTickCount;
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

    /**
     * 将指标数据转化为 json 字符
     *
     * @return
     */
    public String getAttrJsonString() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.KEY_BATTERY_CURRENT_AVG, batteryCurrentAvg);
        map.put(Constants.KEY_BATTERY_CURRENT_MAX, batteryCurrentMax);
        map.put(Constants.KEY_FPS_AVG, fpsAvg);
        map.put(Constants.KEY_FPS_MINI, fpsMini);
        map.put(Constants.KEY_CPU_TICK_COUNT, cpuTickCountPerSecond);
        map.put(Constants.KEY_CPU_TICK_COUNT_PER_SECOND, cpuTickCount);
        map.put(Constants.KEY_MEMORY_AVG, memoryAvg);
        map.put(Constants.KEY_MEMORY_MAX, memoryMax);
        map.put(Constants.KEY_RUM_PROPERTY, property);
        map.put(Constants.KEY_RUM_TAGS, tags);
        return Utils.hashMapObjectToJson(map);
    }

    /**
     * 从 json 字符转化 View 指标数据
     *
     * @param jsonString
     */
    public void setFromAttrJsonString(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            this.batteryCurrentAvg = json.optInt(Constants.KEY_BATTERY_CURRENT_AVG);
            this.batteryCurrentMax = json.getInt(Constants.KEY_BATTERY_CURRENT_MAX);
            this.fpsAvg = json.optDouble(Constants.KEY_FPS_AVG);
            this.fpsMini = json.optDouble(Constants.KEY_FPS_MINI);
            this.memoryAvg = json.optLong(Constants.KEY_MEMORY_AVG);
            this.memoryMax = json.optLong(Constants.KEY_MEMORY_MAX);
            this.cpuTickCountPerSecond = json.optDouble(Constants.KEY_CPU_TICK_COUNT);
            this.cpuTickCount = json.optLong(Constants.KEY_CPU_TICK_COUNT_PER_SECOND);

            JSONObject jsonProperty = json.optJSONObject(Constants.KEY_RUM_PROPERTY);
            if (jsonProperty != null) {
                Iterator<String> keys = jsonProperty.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    this.property.put(key, jsonProperty.opt(key));
                }
            }

            JSONObject jsonTags = json.optJSONObject(Constants.KEY_RUM_TAGS);
            if (jsonTags != null) {
                Iterator<String> keys = jsonTags.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    this.tags.put(key, jsonTags.opt(key));
                }
            }
        } catch (JSONException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
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
                ", fpsMini=" + fpsMini +
                ", fpsAvg=" + fpsAvg +
                ", cpuTickCountPerSecond=" + cpuTickCountPerSecond +
                ", cpuTickCount=" + cpuTickCount +
                ", memoryAvg=" + memoryAvg +
                ", memoryMax=" + memoryMax +
                ", batteryCurrentAvg=" + batteryCurrentAvg +
                ", batteryCurrentMax=" + batteryCurrentMax +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
