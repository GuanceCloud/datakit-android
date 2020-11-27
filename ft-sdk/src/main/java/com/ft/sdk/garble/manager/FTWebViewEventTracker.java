package com.ft.sdk.garble.manager;

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
    }


    public void pageFinished(String url) {
        finishTimeLine = System.currentTimeMillis();

        finishDuration = finishTimeLine - startTimeline;

        long now = finishTimeLine;

        reset();
    }

    private void reset() {
        whitePageDuration = -1;
        finishDuration = -1;
    }


}
