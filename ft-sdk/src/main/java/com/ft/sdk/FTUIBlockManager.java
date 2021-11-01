package com.ft.sdk;

import android.view.Choreographer;

import com.ft.sdk.garble.manager.FTMainLoopLogMonitor;


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


    public static void start(FTRUMConfig config) {
        if (!config.isRumEnable()
                && !config.isEnableTrackAppUIBlock()) {
            return;
        }

        isStop = false;

        FTMainLoopLogMonitor.getInstance().setLogCallBack((log, duration) -> {
            FTRUMGlobalManager.get().addLongTask(log, duration);
        });


        Choreographer.getInstance().postFrameCallback(callback);
    }

    public static void release() {
        FTMainLoopLogMonitor.release();
        isStop = true;
    }
}