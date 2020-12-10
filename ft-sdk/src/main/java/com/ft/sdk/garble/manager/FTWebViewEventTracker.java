package com.ft.sdk.garble.manager;

import com.ft.sdk.garble.utils.Utils;

public class FTWebViewEventTracker {
    private long whitePageDuration = -1;
    private long finishDuration = -1;

    private long startTimeline;
    private long finishTimeLine;

    private long loadingTimeLine;


    public void pageStarted(String url) {
        startTimeline = Utils.getCurrentNanoTime();
    }


    public void pageLoading(String url) {
        loadingTimeLine = Utils.getCurrentNanoTime();
        long now = loadingTimeLine;
        whitePageDuration = loadingTimeLine - startTimeline;
    }


    public void pageFinished(String url) {
        finishTimeLine = Utils.getCurrentNanoTime();

        finishDuration = finishTimeLine - startTimeline;

        long now = finishTimeLine;

        reset();
    }

    private void reset() {
        whitePageDuration = -1;
        finishDuration = -1;
    }


}
