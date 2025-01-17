package com.ft.sdk.garble.utils;

import android.view.Choreographer;

import java.util.concurrent.TimeUnit;

/**
 * create: by huangDianHua
 * time: 2020/4/29 10:32:51
 * description: FPS 信息监控
 */
public class FpsUtils {
    private static FpsUtils fpsUtils;
    Metronome metronome;

    private FpsUtils() {
        metronome = new Metronome();
    }

    private double mFps;

    /**
     * 获取 FPS 数值
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
     * 释放
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
            //将回调的时间转换成毫秒
            long currentTimeMillis = TimeUnit.NANOSECONDS.toMillis(frameTimeNanos);

            if (frameStartTime > 0) {
                // 计算上一次帧刷新和这一次帧刷新之间的时间差
                final long timeSpan = currentTimeMillis - frameStartTime;
                //累计刷新次数
                framesRendered++;
                //当时间大于设定的周期就计算帧率
                if (timeSpan > interval) {
                    //通过刷新次数除以间隔时间得到fps
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

            //逐帧请求
            choreographer.postFrameCallback(this);
        }
    }

    public interface Audience {
        void heartbeat(double fps);
    }
}
