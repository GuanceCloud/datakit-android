package com.ft.sdk.garble.utils;

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

    private FpsUtils() {
        metronome = new Metronome();
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
     *
     */
    public void start() {
        metronome.start();
        metronome.addListener(new Audience() {
            @Override
            public void heartbeat(double fps) {
                mFps = fps;
            }
        });
    }

    /**
     * Release
     */
    public static void release() {
        if (fpsUtils != null) {
            if (fpsUtils.metronome != null) {
                fpsUtils.metronome.stop();
            }
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
         *
         */
        public void start() {
            choreographer.removeFrameCallback(this);
            choreographer.postFrameCallback(this);
        }

        /**
         *
         */
        public void stop() {
            frameStartTime = 0;
            framesRendered = 0;
            choreographer.removeFrameCallback(this);
        }

        public void addListener(Audience l) {
            this.audience = l;
        }

        public void setInterval(int interval) {
            this.interval = interval;
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
