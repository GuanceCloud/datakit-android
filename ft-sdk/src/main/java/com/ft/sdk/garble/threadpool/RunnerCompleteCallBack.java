package com.ft.sdk.garble.threadpool;

/**
 * Thread execution callback object, used by {@link BaseThreadPool} for execution callbacks
 */
public interface RunnerCompleteCallBack {
    /**
     * Called after Runner call ends
     */
    void onComplete();

}
