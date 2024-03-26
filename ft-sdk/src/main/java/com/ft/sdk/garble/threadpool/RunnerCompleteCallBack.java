package com.ft.sdk.garble.threadpool;

/**
 * 线程执行回调对象，{@link BaseThreadPool} 执行回调使用
 */
public interface RunnerCompleteCallBack {
    /**
     * Runner 调用结束后调用
     */
    void onComplete();

}
