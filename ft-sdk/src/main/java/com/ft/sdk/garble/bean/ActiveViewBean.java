package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

public class ActiveViewBean extends ViewBean {

    public ActiveViewBean(String name, String viewReferrer, long loadTime, String sessionId) {
        this.viewName = name;
        this.viewReferrer = viewReferrer;
        this.loadTime = loadTime;
        this.sessionId = sessionId;
    }

    public void close() {
        this.isClose = true;
        timeSpent = Utils.getCurrentNanoTime() - startTime;
    }

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
        bean.fpsAvg = this.fpsAvg;
        bean.fpsMini = this.fpsMini;
        bean.property = this.property;
        return bean;
    }


}
