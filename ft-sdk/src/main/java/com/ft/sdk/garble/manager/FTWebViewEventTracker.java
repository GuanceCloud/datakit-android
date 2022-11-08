package com.ft.sdk.garble.manager;

import com.ft.sdk.garble.utils.Utils;

/**
 * Web 事件捕获 {@link com.ft.sdk.FTWebViewClient} 页面事件逻辑处理
 * @author Brandon
 *
 */
public class FTWebViewEventTracker {
    private long whitePageDuration = -1;
    private long finishDuration = -1;

    private long startTimeline;
    private long finishTimeLine;

    private long loadingTimeLine;

    /**
     * 页面加载开始
     * @param url 请求资源地址
     */
    public void pageStarted(String url) {
        startTimeline = Utils.getCurrentNanoTime();
    }

    /**
     * 页面 loading
     * @param url 请求资源地址
     */
    public void pageLoading(String url) {
        loadingTimeLine = Utils.getCurrentNanoTime();
        long now = loadingTimeLine;
        whitePageDuration = loadingTimeLine - startTimeline;
    }

    /**
     * 页面加载结束
     * @param url 请求资源地址
     */
    public void pageFinished(String url) {
        finishTimeLine = Utils.getCurrentNanoTime();

        finishDuration = finishTimeLine - startTimeline;

        long now = finishTimeLine;

        reset();
    }

    /**
     *  重置内容
     */
    private void reset() {
        whitePageDuration = -1;
        finishDuration = -1;
    }


}
