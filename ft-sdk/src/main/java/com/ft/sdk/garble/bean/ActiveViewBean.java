package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

/**
 * Currently active state {@link ViewBean}
 * <p>
 * Only one ActiveViewBean will exist in an application at the same time
 *
 * @author Brandon
 */
public class ActiveViewBean extends ViewBean {

    /**
     * @param name
     * @param viewReferrer
     * @param loadTime
     * @param sessionId
     */
    public ActiveViewBean(String name, String viewReferrer, long loadTime, String sessionId) {
        this.viewName = name;
        this.viewReferrer = viewReferrer;
        this.loadTime = loadTime;
        this.sessionId = sessionId;
    }

    /**
     * Close active state
     */
    public void close() {
        this.isClose = true;
        timeSpent = Utils.getCurrentNanoTime() - startTime;
    }

    /**
     * Convert to {@link ViewBean}
     *
     * @return
     */
    public ViewBean convertToViewBean() {
        ViewBean bean = new ViewBean();
        bean.id = this.id;
        bean.viewReferrer = this.viewReferrer;
        bean.viewName = this.viewName;
        bean.longTaskCount = this.longTaskCount;
        bean.errorCount = this.errorCount;
        bean.actionCount = this.actionCount;
        bean.isClose = this.isClose;
        bean.startTime = this.startTime;
        bean.loadTime = this.loadTime;
        bean.timeSpent = this.timeSpent;
        bean.sessionId = this.sessionId;
        bean.memoryMax = this.memoryMax;
        bean.memoryAvg = this.memoryAvg;
        bean.cpuTickCountPerSecond = this.cpuTickCountPerSecond;
        bean.cpuTickCount = this.cpuTickCount;
        bean.batteryCurrentMax = this.batteryCurrentMax;
        bean.batteryCurrentAvg = this.batteryCurrentAvg;
        bean.lastErrorTime = this.lastErrorTime;
        bean.fpsAvg = this.fpsAvg;
        bean.fpsMini = this.fpsMini;
        bean.property = this.property;
        bean.tags = this.tags;
        bean.collectType = this.collectType;
        return bean;
    }


}
