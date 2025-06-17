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
     * 启动耗时，单位纳秒
     */
    private long coldStartDuration = 0;

    /**
     * 启动开始时间点，单位纳秒
     */
    private long coldStartTimeLine = 0;


    private FTAppStartCounter() {
    }

    private static class SingletonHolder {
        private static final FTAppStartCounter INSTANCE = new FTAppStartCounter();
    }

    public static FTAppStartCounter get() {
        return FTAppStartCounter.SingletonHolder.INSTANCE;
    }


    /**
     * 记录冷启动时间段
     * * {@link Constants#KEY_RUM_ACTION_TYPE} = {@link  Constants#ACTION_TYPE_LAUNCH_COLD}
     *
     * @param coldStartEndTimeLine
     */
    void coldStart(long coldStartEndTimeLine) {
        this.coldStartTimeLine = Utils.getAppStartTimeNs();
        this.coldStartDuration = coldStartEndTimeLine - coldStartTimeLine;
        LogUtils.d(TAG, "coldStart:" + coldStartDuration);

    }

    /**
     * 上传冷启动时间
     */
    void coldStartUpload() {
        if (coldStartDuration <= 0 || coldStartTimeLine <= 0) return;
        FTAutoTrack.putRUMLaunchPerformance(true, coldStartDuration, coldStartTimeLine);
        coldStartDuration = 0;
    }

    /**
     * 上传热启动时间
     * <p>
     * {@link Constants#KEY_RUM_ACTION_TYPE} = {@link  Constants#ACTION_TYPE_LAUNCH_HOT}
     *
     * @param hotStartDuration 热启动时间段，单位纳秒
     */
    void hotStart(long hotStartDuration, long startTime) {
        FTAutoTrack.putRUMLaunchPerformance(false, hotStartDuration, startTime);
        LogUtils.d(TAG, "hotStart:" + hotStartDuration);
    }


    /**
     * 检测重传，应用于生命周期，早于 SDK {@link FTSdk#install(FTSDKConfig)}的情况，一般用于第三方框架使用 ，
     * 例如 flutter SDK，ReactNative SDK
     */
    void checkToReUpload() {
        if (coldStartDuration > 0) {
            coldStartUpload();
        }
    }

}
