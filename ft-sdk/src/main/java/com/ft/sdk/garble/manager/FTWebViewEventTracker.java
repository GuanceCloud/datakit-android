package com.ft.sdk.garble.manager;

import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.bean.OP;

public class FTWebViewEventTracker {
    private long whitePageDuration = -1;
    private long finishDuration = -1;

    private long startTimeline;
    private long finishTimeLine;

    private long loadingTimeLine;


    public void pageStarted() {
        startTimeline = System.currentTimeMillis();
    }


    public void pageLoading() {
        loadingTimeLine = System.currentTimeMillis();
        long now = loadingTimeLine;
        whitePageDuration = loadingTimeLine - startTimeline;
    }


    public void pageFinished() {
        finishTimeLine = System.currentTimeMillis();

        finishDuration = finishTimeLine - startTimeline;
        reset();

    }

    private void reset() {
        whitePageDuration = -1;
        finishDuration = -1;
    }


}
