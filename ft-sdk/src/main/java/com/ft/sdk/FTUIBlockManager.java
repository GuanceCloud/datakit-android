package com.ft.sdk;

import android.view.Choreographer;

import com.ft.sdk.garble.FTRUMConfig;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.manager.FTMainLoopLogMonitor;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;


public class FTUIBlockManager {

    static Choreographer.FrameCallback callback = new Choreographer.FrameCallback() {

        @Override
        public void doFrame(long frameTimeNanos) {

//            FTMainLoopLogMonitor.getInstance().removeMonitor();

            FTMainLoopLogMonitor.getInstance().startMonitor();

            if (isStop) return;

            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    private static boolean isStop = true;


    public static void start(FTSDKConfig config) {
        if (!config.isEnableTrackAppUIBlock()) {
            return;
        }

        isStop = false;

        FTMainLoopLogMonitor.getInstance().setLogCallBack((log, duration) -> {

            if (FTRUMConfig.get().isRumEnable()) {
                FTAutoTrack.putRUMuiBlock(log, duration);
            }

        });


        Choreographer.getInstance().postFrameCallback(callback);
    }

    public static void release() {
        FTMainLoopLogMonitor.release();
        isStop = true;
    }
}