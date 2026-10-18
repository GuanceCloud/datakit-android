package com.ft.sdk;

import com.ft.sdk.garble.threadpool.RunnerCompleteCallBack;

/** Internal storage outcome; ordinary completion callbacks keep their existing semantics. */
interface RUMWriteResultCallback extends RunnerCompleteCallBack {
    void onResult(boolean stored);

    @Override
    default void onComplete() {
    }
}
