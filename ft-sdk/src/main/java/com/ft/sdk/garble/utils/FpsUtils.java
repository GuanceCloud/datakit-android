package com.ft.sdk.garble.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;

import java.util.concurrent.TimeUnit;

/**
 * create: by huangDianHua
 * time: 2020/4/29 10:32:51
 * description: FPS information monitoring
 */
public class FpsUtils {
    private static FpsUtils fpsUtils;
    Metronome metronome;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private FpsUtils() {
        // Metronome will be initialized lazily on main thread when needed
    }

    private double mFps;

    /**
     * Get FPS value
     * @return
     */
    public double getFps() {
        return mFps;
    }

    public static FpsUtils get() {
        if (fpsUtils == null) {
            fpsUtils = new FpsUtils();
        }
        return fpsUtils;
    }

    /**
     * Start FPS monitoring with automatic main thread adaptation
     */
    public void start() {
        runOnMainThread(() -> {
            // Initialize Metronome on main thread to ensure Choreographer.getInstance() is called safely
            if (metronome == null) {
                metronome = new Metronome();
            }
            metronome.start();
            metronome.addListener(new Audience() {
                @Override
                public void heartbeat(double fps) {
                    mFps = fps;
                }
            });
        });
    }

    /**
     * Release resources with automatic main thread adaptation
     */
    public static void release() {
        runOnMainThread(() -> {
            if (fpsUtils != null) {
                if (fpsUtils.metronome != null) {
                    fpsUtils.metronome.stop();
                }
            }
        });
    }

    /**
     * Ensure code runs on main thread, automatically switch if not already on main thread
     * @param runnable Code to execute on main thread
     */
    private static void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Already on main thread, execute directly
            runnable.run();
        } else {
            // Not on main thread, switch to main thread for execution
            mainHandler.post(runnable);
        }
    }


    /**
     *
     */
    private static class Metronome implements Choreographer.FrameCallback {

        private final Choreographer choreographer;

        private long frameStartTime = 0;
        private int framesRendered = 0;

        private int interval = 1000;
        private Audience audience;

        public Metronome() {
            choreographer = Choreographer.getInstance();
        }

        /**
         * Start FPS monitoring with automatic main thread adaptation
         */
        public void start() {
            runOnMainThread(() -> {
                choreographer.removeFrameCallback(this);
                choreographer.postFrameCallback(this);
            });
        }

        /**
         * Stop FPS monitoring with automatic main thread adaptation
         */
        public void stop() {
            runOnMainThread(() -> {
                frameStartTime = 0;
                framesRendered = 0;
                choreographer.removeFrameCallback(this);
            });
        }

        public void addListener(Audience l) {
            this.audience = l;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        /**
         * Ensure code runs on main thread, automatically switch if not already on main thread
         * @param runnable Code to execute on main thread
         */
        private void runOnMainThread(Runnable runnable) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // Already on main thread, execute directly
                runnable.run();
            } else {
                // Not on main thread, switch to main thread for execution
                mainHandler.post(runnable);
            }
        }

        @Override
        public void doFrame(long frameTimeNanos) {
            //Convert callback time to milliseconds
            long currentTimeMillis = TimeUnit.NANOSECONDS.toMillis(frameTimeNanos);

            if (frameStartTime > 0) {
                // Calculate time difference between last frame refresh and this frame refresh
                final long timeSpan = currentTimeMillis - frameStartTime;
                //Accumulate refresh count
                framesRendered++;
                //When time is greater than the set period, calculate frame rate
                if (timeSpan > interval) {
                    //Get fps by dividing refresh count by interval time
                    final double fps = framesRendered * 1000 / (double) timeSpan;

                    frameStartTime = currentTimeMillis;
                    framesRendered = 0;

                    if (audience != null) {
                        audience.heartbeat(fps);
                    }
                }
            } else {
                frameStartTime = currentTimeMillis;
            }

            //Request frame by frame
            choreographer.postFrameCallback(this);
        }
    }

    public interface Audience {
        void heartbeat(double fps);
    }
}
