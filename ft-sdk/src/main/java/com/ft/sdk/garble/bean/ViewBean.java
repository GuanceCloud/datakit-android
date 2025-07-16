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
 * View metric data. The following data are metrics generated during the page lifecycle,
 * from {@link FTRUMInnerManager#startView(String)} to
 * <p>
 * {@link FTRUMInnerManager#stopView()}  }
 *
 * @author Brandon
 */
public class ViewBean {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "ViewBean";
    /**
     * Unique View ID, {@link Constants#KEY_RUM_VIEW_ID}
     */
    String id = Utils.randomUUID();
    /**
     * Page source, parent page, {@link Constants#KEY_RUM_VIEW_REFERRER}
     */
    String viewReferrer;
    /**
     * Page name, {@link Constants#KEY_RUM_VIEW_NAME}
     */
    String viewName;

    /**
     * Number of longtask captures, {@link Constants#KEY_RUM_ACTION_LONG_TASK_COUNT}
     */
    int longTaskCount;

    /**
     * Number of network resource requests, {@link Constants#KEY_RUM_VIEW_RESOURCE_COUNT}
     */
    int resourceCount;
    /**
     * Number of errors occurred, {@link Constants#KEY_RUM_VIEW_ERROR_COUNT}
     */
    int errorCount;

    /**
     * Number of captured Actions {@link  Constants#KEY_RUM_VIEW_ACTION_COUNT}
     */
    int actionCount;

    /**
     * Whether it is active, {@link  Constants#KEY_RUM_VIEW_IS_ACTIVE}
     */
    boolean isClose = false;
    /**
     * Start time, unit: nanoseconds
     */
    long startTime = Utils.getCurrentNanoTime();

    /**
     * Page load time, unit: nanoseconds, {@link Constants#KEY_RUM_VIEW_LOAD}
     */
    long loadTime = 0;

    /**
     * Page stay time, unit: nanoseconds, {@link Constants#KEY_RUM_VIEW_TIME_SPENT}
     */
    long timeSpent = 0;

    /**
     * Minimum FPS, {@link  Constants#KEY_FPS_MINI}
     */
    double fpsMini;

    /**
     * Average FPS, {@link  Constants#KEY_FPS_AVG}
     */
    double fpsAvg;

    /**
     * CPU ticks per second, {@link  Constants#KEY_CPU_TICK_COUNT_PER_SECOND}
     */
    double cpuTickCountPerSecond = -1;

    /**
     * CPU tick count, {@link  Constants#KEY_CPU_TICK_COUNT}
     */
    long cpuTickCount = -1;

    /**
     * Average memory, {@link  Constants#KEY_MEMORY_AVG}
     */
    long memoryAvg;

    /**
     * Maximum memory, {@link  Constants#KEY_MEMORY_MAX}
     */
    long memoryMax;

    /**
     * Average battery consumption, {@link  Constants#KEY_BATTERY_CURRENT_AVG}
     */
    int batteryCurrentAvg;
    /**
     * Maximum battery consumption, {@link  Constants#KEY_BATTERY_CURRENT_MAX}
     */
    int batteryCurrentMax;

    /**
     * {@link FTRUMInnerManager#sessionId},{@link Constants#KEY_RUM_SESSION_ID}
     */
    String sessionId;


    /**
     * Page update count, {@link  Constants#KEY_SDK_VIEW_UPDATE_TIME}
     */
    long viewUpdateTime = 0;

    /**
     * Error time of the page
     */
    long lastErrorTime = 0;

    public void setLastErrorTime(long lastErrorTime) {
        this.lastErrorTime = lastErrorTime;
    }

    public long getLastErrorTime() {
        return lastErrorTime;
    }

    public long getViewUpdateTime() {
        return viewUpdateTime;
    }

    public void setViewUpdateTime(long viewUpdateTime) {
        this.viewUpdateTime = viewUpdateTime;
    }

    /**
     * Page additional properties, {@link  Constants#KEY_RUM_PROPERTY}
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

    public String collectType;

    public CollectType getCollectType() {
        return CollectType.fromValue(collectType);
    }

    public void setCollectType(CollectType collectType) {
        this.collectType = collectType.getValue();
    }

    /**
     * Convert metric data to json string
     *
     * @return
     */
    public String getAttrJsonString() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.KEY_COLLECT_TYPE, collectType);
        map.put(Constants.KEY_SESSION_ERROR_TIMESTAMP, lastErrorTime);
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
     * Convert View metric data from json string
     *
     * @param jsonString
     */
    public void setFromAttrJsonString(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            this.lastErrorTime = json.optLong(Constants.KEY_SESSION_ERROR_TIMESTAMP);
            this.collectType = json.optString(Constants.KEY_COLLECT_TYPE);
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
