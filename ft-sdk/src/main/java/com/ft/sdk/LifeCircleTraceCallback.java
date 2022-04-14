package com.ft.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.manager.FTMainLoopLogMonitor;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;


/**
 * create: by huangDianHua
 * time: 2020/6/17 17:50:45
 * description:处理当前应用退到后台10秒后重新进入
 */
class LifeCircleTraceCallback {
    private static final String TAG = "AppRestartCallback";
    public static final int MSG_CHECK_SLEEP_STATUS = 1;
    public static final int DELAY_MILLIS = 10000;//10 秒
    private boolean alreadySleep = true;
    private boolean mInited = false;//判断第一次第一个页是否创建
    private long startTime = 0;

    private final HashMap<Context, Long> mCreateMap = new HashMap<>();

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
     * start 之前
     */
    public void onPreStart() {
        if (alreadySleep) {//表示从后台重新进入
            if (mInited) {
//                FTAutoTrack.startApp();
                startTime = Utils.getCurrentNanoTime();
            }
            FTMonitor.get().checkForReStart();
        }


    }

    public void onPreOnCreate(Context context) {
        mCreateMap.put(context, Utils.getCurrentNanoTime());

        if (!mInited) {
//            FTAutoTrack.startApp();
            startTime = Utils.getCurrentNanoTime();
        }
    }

    public void onPostOnCreate(Context context) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        FTRUMConfig config = manager.getConfig();

        if (!mInited) {
            long now = Utils.getCurrentNanoTime();
            FTActivityManager.get().setAppState(AppState.RUN);
            FTAppStartCounter.get().codeStart(now - startTime);
            if (manager.isRumEnable()) {
                FTAppStartCounter.get().codeStartUpload();
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
     * resume 之后
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
            FTMainLoopLogMonitor.getInstance().resume();

            alreadySleep = false;
        }
        //避免重复计算页面启动的统计计算
        if (!mInited) {
            mInited = true;
        }
    }

    public void onStop() {
        boolean appForeground = FTActivityManager.get().isAppForeground();
        if (!appForeground) {
            handler.removeMessages(MSG_CHECK_SLEEP_STATUS);
            handler.sendEmptyMessageDelayed(MSG_CHECK_SLEEP_STATUS, DELAY_MILLIS);
        }
    }

    public void onPostDestroy(Context context) {
        mCreateMap.remove(context);
        if (mCreateMap.isEmpty()) {
            mInited = false;
            LogUtils.d(TAG, "Application all close");
        }
    }

    /**
     * 检测是否长时间休眠
     */
    private void checkLongTimeSleep() {
        boolean appForeground = FTActivityManager.get().isAppForeground();
        if (!appForeground) {
//            FTAutoTrack.sleepApp(DELAY_MILLIS);
            alreadySleep = true;
            FTMainLoopLogMonitor.getInstance().pause();
        }
    }

}
