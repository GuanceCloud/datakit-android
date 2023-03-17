package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

/**
 * 启动计时
 *
 * <a href="https://docs.guance.com/real-user-monitoring/explorer/">查看器</a>
 * <p>
 * {@link Constants#MEASUREMENT} 为 {@link Constants#FT_MEASUREMENT_RUM_ACTION}
 * {@link Constants#KEY_RUM_ACTION_TYPE} 为 {@link  Constants#ACTION_NAME_LAUNCH_COLD,
 * Constants#ACTION_TYPE_LAUNCH_HOT}
 * <p>
 * 生命周期概念参考内容官方文档
 * <a href="https://developer.android.com/topic/performance/vitals/launch-time?hl=zh-cn">启动时间</a>
 *
 * <p>
 * 冷启动，对应 {@link Constants#ACTION_NAME_LAUNCH_COLD}
 * 温启动，热启动，对应{@link Constants#ACTION_TYPE_LAUNCH_HOT}
 *
 * @author Brandon
 */
class FTAppStartCounter {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "AppStartCounter";
    /**
     * 时间段，单位纳秒
     */
    private long codeStartTime = 0;
    /**
     * 时间线，单位纳秒
     */
    private long codeStartTimeLine = 0;

    private FTAppStartCounter() {
    }

    private static class SingletonHolder {
        private static final FTAppStartCounter INSTANCE = new FTAppStartCounter();
    }

    public static FTAppStartCounter get() {
        return FTAppStartCounter.SingletonHolder.INSTANCE;
    }

    /**
     * 标记应用冷启动时间
     */
    void markCodeStartTimeLine() {
        codeStartTimeLine = Utils.getCurrentNanoTime();
        LogUtils.d(TAG, "markCodeStartTimeLine");
    }

    /**
     * 获取冷启动时间
     *
     * @return 返回冷启动时间线，单位纳秒
     */
    long getMarkCodeTimeLine() {
        return codeStartTimeLine;
    }

    /**
     * 重置冷启动时间线
     */
    void resetCodeStartTimeline() {
        codeStartTimeLine = 0;
        LogUtils.d(TAG, "resetCodeStartTimeline");
    }

    /**
     * 记录冷启动时间段
     *
     * @param codeStartTime 冷启动时间段，单位纳秒
     */
    void codeStart(long codeStartTime) {
        this.codeStartTime = codeStartTime;
        LogUtils.d(TAG, "codeStart:" + codeStartTime);

    }

    /**
     * 上传冷启动时间
     */
    void codeStartUpload() {
        if (codeStartTime <= 0 || codeStartTimeLine <= 0) return;
        FTAutoTrack.putRUMLaunchPerformance(true, codeStartTime, codeStartTimeLine);
        codeStartTime = 0;
    }

    /**
     * 上传热启动时间
     *
     * {@link Constants#KEY_RUM_ACTION_TYPE} = {@link  Constants#KEYRUM}
     *
     * @param hotStartTime 热启动时间段，单位纳秒
     */
    void hotStart(long hotStartTime) {
        FTAutoTrack.putRUMLaunchPerformance(false, hotStartTime, Utils.getCurrentNanoTime());
    }


    /**
     * 检测重传，应用于生命周期，早于 SDK {@link FTSdk#install(FTSDKConfig)}的情况，一般用于第三方框架使用 ，
     * 例如 flutter SDK，ReactNative SDK
     */
    void checkToReUpload() {
        if (codeStartTime > 0) {
            codeStartUpload();
            resetCodeStartTimeline();
        }
    }

}
