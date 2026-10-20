package com.ft.sdk.garble.bean;

import com.ft.sdk.FTInitialResourceIdentifier;
import com.ft.sdk.FTTimeBasedInitialResourceIdentifier;
import com.ft.sdk.internal.metrics.NetworkSettledMetric;

/**
 * Currently active state {@link ViewBean}
 * <p>
 * Only one ActiveViewBean will exist in an application at the same time
 *
 * @author Brandon
 */
public class ActiveViewBean extends ViewBean {

    public final NetworkSettledMetric networkSettledMetric;

    /**
     * @param name
     * @param viewReferrer
     * @param loadTime
     * @param sessionId
     */
    public ActiveViewBean(String name, String viewReferrer, long loadTime, String sessionId) {
        this(name, viewReferrer, loadTime, sessionId, new FTTimeBasedInitialResourceIdentifier());
    }

    public ActiveViewBean(String name, String viewReferrer, long loadTime, String sessionId,
                          FTInitialResourceIdentifier identifier) {
        networkSettledMetric = new NetworkSettledMetric(getStartTimeNanoForDuration(), identifier);
        this.viewName = name;
        this.viewReferrer = viewReferrer;
        this.loadTime = loadTime;
        this.sessionId = sessionId;
    }

    /**
     * Close active state
     */
    public void close() {
        if (isClose) return;
        networkSettledMetric.stop();
        this.isClose = true;
        timeSpent = System.nanoTime()- startTimeNanoForDuration;
    }

    @Override
    public void setClose(boolean close) {
        if (close) networkSettledMetric.stop();
        super.setClose(close);
    }

    /**
     * Convert to {@link ViewBean}
     *
     * @return
     */
    public ViewBean convertToViewBean() {
        ViewBean bean = new ViewBean();
        bean.networkSettledTime = networkSettledMetric.resolve();
        bean.interactionToNextViewTime = this.interactionToNextViewTime;
        bean.id = this.id;
        bean.viewReferrer = this.viewReferrer;
        bean.viewName = this.viewName;
        bean.longTaskCount = this.longTaskCount;
        bean.longTaskDuration = this.longTaskDuration;
        bean.errorCount = this.errorCount;
        bean.actionCount = this.actionCount;
        bean.isClose = this.isClose;
        bean.startTime = this.startTime;
        bean.startTimeNanoForDuration = this.startTimeNanoForDuration;
        bean.loadTime = this.loadTime;
        bean.timeSpent = this.timeSpent;
        bean.sessionId = this.sessionId;
        bean.sessionStartReason = this.sessionStartReason;
        bean.appStateStart = this.appStateStart;
        bean.appStateProcess = this.appStateProcess;
        bean.closedAppState = this.closedAppState;
        bean.processName = this.processName;
        bean.processRunId = this.processRunId;
        bean.processStartMs = this.processStartMs;
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
        bean.sessionReplayErrorSampled = this.sessionReplayErrorSampled;
        return bean;
    }


}
