package com.ft.sdk.garble.utils;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

/**
 * author: huangDianHua
 * time: 2020/9/28 15:15:34
 * description: Anr 检测程序
 */
public class AnrWatch extends Thread {
    private static final String TAG = "AnrWatch";
    private int timeout = 5000;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean stoped = false;

    public synchronized void stopAnrWatch(){
        stoped = true;
    }
    /**
     * Anr 检测任务
     */
    private class AnrChecker implements Runnable {
        private boolean mCompleted;//任务是否完成
        private long mStartTime;//任务开始时间
        private long executeTime = SystemClock.uptimeMillis();//任务执行时间

        @Override
        public void run() {
            synchronized (AnrWatch.this) {
                mCompleted = true;
                executeTime = SystemClock.uptimeMillis();
            }
        }

        /**
         * 开启一个任务
         */
        void schedule() {
            mCompleted = false;
            mStartTime = SystemClock.uptimeMillis();
            mainHandler.postAtFrontOfQueue(this);
        }

        /**
         * 判断当前任务是否被阻塞
         *
         * @return
         */
        boolean isBlocked() {
            return !mCompleted || executeTime - mStartTime >= 5000;
        }
    }

    public interface AnrListener {
        void onAnrHappened();
    }

    private AnrChecker anrChecker = new AnrChecker();

    private AnrListener anrListener;

    private AnrWatch(Builder builder) {
        super("ANR-Watch-Thread");
        this.timeout = builder.timeout;
        this.anrListener = builder.anrListener;
    }

    @Override
    public void run() {
        while (!isInterrupted() && !stoped) {
            synchronized (this) {
                anrChecker.schedule();//开启检测任务
                long waitTime = timeout;
                long start = SystemClock.uptimeMillis();
                while (waitTime > 0) {//线程让出一段时间片，直到剩余的时间片小于 0
                    try {
                        wait(waitTime);
                    } catch (InterruptedException e) {
                    }
                    waitTime = timeout - (SystemClock.uptimeMillis() - start);
                }
                if (!anrChecker.isBlocked()) {//检测任务是否执行完成，执行完成后则进入下一次检测
                    continue;
                }
            }
            if (Debug.isDebuggerConnected()) {//如果是debug，则不需要处理
                continue;
            }

            if (anrListener != null) {//回调 anr 发生
                anrListener.onAnrHappened();
            }
        }
    }

    public static class Builder {
        private int timeout;
        private AnrListener anrListener;

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder anrListener(AnrListener anrListener) {
            this.anrListener = anrListener;
            return this;
        }

        public AnrWatch build() {
            return new AnrWatch(this);
        }
    }
}

