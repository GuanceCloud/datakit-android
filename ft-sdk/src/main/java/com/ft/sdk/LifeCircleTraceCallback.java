package com.ft.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;


/**
 * create: by huangDianHua
 * time: 2020/6/17 17:50:45
 * description:处理当前应用退到后台10秒后重新进入
 */
class LifeCircleTraceCallback {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "LifeCircleTraceCallback";
    /**
     * 消息通道
     */
    public static final int MSG_CHECK_SLEEP_STATUS = 1;
    /**
     * 休眠延迟时间
     */
    public static final int DELAY_SLEEP_MILLIS = 10000;//10 秒
    /**
     * 是否已处于休眠状态
     */
    private boolean alreadySleep = true;
    /**
     * 判断第一次第一个页是否创建
     */
    private boolean mInited = false;//
    /**
     * 应用启动时间点
     */
    private long startTime = 0;

    /**
     * 缓存创建时间点
     */
    private final HashMap<Context, Long> mCreateMap = new HashMap<>();

    /**
     * 用于发送延迟消息，10 秒后执行休眠 {@link #alreadySleep} = true 操作
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CHECK_SLEEP_STATUS) {
                checkLongTimeSleep();
            }
        }
    };

    /**
     * {@link Activity#onStart()} 之前
     */
    public void onPreStart() {
        if (alreadySleep) {//表示从后台重新进入
            if (mInited) {
//                FTAutoTrack.startApp();
                startTime = Utils.getCurrentNanoTime();
            }
//            FTMonitor.get().checkForReStart();
        }


    }

    /**
     * {@link Activity#onCreate(Bundle)} 之前
     *
     * @param context
     */
    public void onPreOnCreate(Context context) {
        mCreateMap.put(context, Utils.getCurrentNanoTime());

        if (!mInited) {
//            FTAutoTrack.startApp();
            startTime = Utils.getCurrentNanoTime();
        }
    }

    /**
     * {@link Activity#onCreate(Bundle)} 之后
     *
     * @param context
     */
    public void onPostOnCreate(Context context) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        FTRUMConfig config = manager.getConfig();

        if (!mInited) {
            long now = Utils.getCurrentNanoTime();
            FTActivityManager.get().setAppState(AppState.RUN);
            long codeStartTime = FTAppStartCounter.get().getMarkCodeTimeLine();
            if (codeStartTime > 0) {
                FTAppStartCounter.get().codeStart(now - codeStartTime);
                if (manager.isRumEnable()) {
                    FTAppStartCounter.get().codeStartUpload();
                    FTAppStartCounter.get().resetCodeStartTimeline();
                }
            } else {
                if (manager.isRumEnable()) {
                    FTAppStartCounter.get().hotStart(now - startTime);
                }
            }


        }

        if (manager.isRumEnable()) {
            if (config.isEnableTraceUserView()) {
                Long startTime = mCreateMap.get(context);
                if (startTime != null) {
                    long duration = Utils.getCurrentNanoTime() - startTime;
                    String viewName = AopUtils.getClassName(context);
                    FTRUMGlobalManager.get().onCreateView(viewName, duration);
                }
            }
        }
    }

    /**
     * {@link Activity#onResume() }  之后
     *
     * @param context
     */
    public void onPostResume(Context context) {
        if (alreadySleep) {
            if (mInited) {
                if (FTRUMConfigManager.get().isRumEnable()) {
                    if (startTime > 0) {
                        long now = Utils.getCurrentNanoTime();
                        FTAppStartCounter.get().hotStart(now - startTime);

                    }

                }
            }
            alreadySleep = false;
        }
        //避免重复计算页面启动的统计计算
        if (!mInited) {
            mInited = true;
        }
    }

    /**
     * {@link Activity#onStop() }
     */
    public void onStop() {
        boolean appForeground = Utils.isAppForeground();
        if (!appForeground) {
            handler.removeMessages(MSG_CHECK_SLEEP_STATUS);
            //休眠一段时候后执行,为了区分短时间唤醒的行为
            handler.sendEmptyMessageDelayed(MSG_CHECK_SLEEP_STATUS, DELAY_SLEEP_MILLIS);
        }
    }

    /**
     * {@link Activity#onDestroy() }
     *
     * @param context
     */
    public void onPostDestroy(Context context) {
        mCreateMap.remove(context);
        if (mCreateMap.isEmpty()) {
            mInited = false;
            FTAppStartCounter.get().resetCodeStartTimeline();
            LogUtils.d(TAG, "Application all close");
        }
    }

    /**
     * 检测是否长时间休眠
     */
    private void checkLongTimeSleep() {
        boolean appForeground = Utils.isAppForeground();
        if (!appForeground) {
            alreadySleep = true;
        }
    }

}
