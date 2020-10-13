package com.ft.sdk.garble.manager;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.bean.OP;

public class FTWebViewEventTracker {
    private long whitePageDuration = -1;
    private long finishDuration = -1;

    private long startTimeline;
    private long finishTimeLine;

    private long loadingTimeLine;


    public void pageStarted(String url) {
        startTimeline = System.currentTimeMillis();
    }


    public void pageLoading(String url) {
        loadingTimeLine = System.currentTimeMillis();
        long now = loadingTimeLine;
        whitePageDuration = loadingTimeLine - startTimeline;
        FTAutoTrack.putWebViewTimeCost(now, OP.WEBVIEW_LOADING, url, whitePageDuration);
    }


    public void pageFinished(String url) {
        finishTimeLine = System.currentTimeMillis();

        finishDuration = finishTimeLine - startTimeline;
        reset();

        long now = finishTimeLine;
        FTAutoTrack.putWebViewTimeCost(now, OP.WEBVIEW_LOAD_COMPLETED, url, finishDuration);

    }

    private void reset() {
        whitePageDuration = -1;
        finishDuration = -1;
    }


}
