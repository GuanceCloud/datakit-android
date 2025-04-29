package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

/**
 * 当前激活状态 {@link ViewBean}
 * <p>
 * 一个应用同一时间只会存在一个 ActiveViewBean
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
     * 关闭激活状态
     */
    public void close() {
        this.isClose = true;
        timeSpent = Utils.getCurrentNanoTime() - startTime;
    }

    /**
     * 转化为 {@link ViewBean}
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
        bean.hasReplay = this.hasReplay;
        bean.property = this.property;
        bean.tags = this.tags;
        bean.collectType = this.collectType;
        return bean;
    }


}
