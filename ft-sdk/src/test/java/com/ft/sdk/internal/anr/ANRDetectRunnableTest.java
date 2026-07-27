package com.ft.sdk.internal.anr;

import com.ft.sdk.ExtraLogCatSetting;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ANRDetectRunnableTest {

    @Test
    public void legacyPublicConstructorRemainsAvailable() throws Exception {
        Constructor<ANRDetectRunnable> constructor =
                ANRDetectRunnable.class.getConstructor(ExtraLogCatSetting.class);

        Assert.assertTrue(Modifier.isPublic(constructor.getModifiers()));
    }

    @Test
    public void healthyMainThreadMaintainsDetectInterval() throws Exception {
        CountDownLatch firstPost = new CountDownLatch(1);
        CountDownLatch secondPost = new CountDownLatch(1);
        AtomicInteger postCount = new AtomicInteger();
        Thread mainThread = new Thread("fake-main");
        ANRDetectRunnable runnable = new ANRDetectRunnable(
                null,
                new ANRDetectRunnable.MainThreadPoster() {
                    @Override
                    public boolean post(Runnable callback) {
                        if (postCount.incrementAndGet() == 1) {
                            firstPost.countDown();
                        } else {
                            secondPost.countDown();
                        }
                        callback.run();
                        return true;
                    }

                    @Override
                    public Thread getThread() {
                        return mainThread;
                    }
                },
                (stack, occurredAtNanoseconds, threadName) ->
                        Assert.fail("healthy main reported ANR"),
                1_000L);
        Thread detector = new Thread(runnable, "anr-detector-test");
        detector.start();
        try {
            Assert.assertTrue(firstPost.await(1, TimeUnit.SECONDS));
            Assert.assertFalse("healthy callback shortened the detect interval",
                    secondPost.await(100, TimeUnit.MILLISECONDS));
        } finally {
            runnable.shutdown();
            detector.join(1_000L);
        }

        Assert.assertFalse(detector.isAlive());
    }

    @Test
    public void reportsOnceAndStopsForDetectorLifetime() throws Exception {
        BlockingQueue<Runnable> mainCallbacks = new LinkedBlockingQueue<>();
        BlockingQueue<Integer> reports = new LinkedBlockingQueue<>();
        Thread mainThread = new Thread("fake-main");
        AtomicInteger reportCount = new AtomicInteger();
        ANRDetectRunnable runnable = new ANRDetectRunnable(
                null,
                new FakeMainThreadPoster(mainCallbacks, mainThread),
                (stack, occurredAtNanoseconds, threadName) -> {
                    Assert.assertTrue(occurredAtNanoseconds > 0);
                    Assert.assertEquals("fake-main", threadName);
                    reports.add(reportCount.incrementAndGet());
                },
                25L);
        Thread detector = new Thread(runnable, "anr-detector-test");
        detector.start();
        try {
            Runnable callback = mainCallbacks.poll(1, TimeUnit.SECONDS);
            Assert.assertNotNull(callback);
            Assert.assertEquals(Integer.valueOf(1), reports.poll(1, TimeUnit.SECONDS));

            callback.run();
            detector.join(500L);

            Assert.assertFalse(detector.isAlive());
            Assert.assertNull(mainCallbacks.poll(100, TimeUnit.MILLISECONDS));
            Assert.assertNull(reports.poll(100, TimeUnit.MILLISECONDS));
            Assert.assertEquals(1, reportCount.get());
        } finally {
            runnable.shutdown();
            detector.join(1_000L);
        }
    }

    @Test
    public void shutdownWakesDetectorBeforeTheThreshold() throws Exception {
        BlockingQueue<Runnable> mainCallbacks = new LinkedBlockingQueue<>();
        AtomicInteger reports = new AtomicInteger();
        ANRDetectRunnable runnable = new ANRDetectRunnable(
                null,
                new FakeMainThreadPoster(mainCallbacks, new Thread("fake-main")),
                (stack, occurredAtNanoseconds, threadName) -> reports.incrementAndGet(),
                5_000L);
        Thread detector = new Thread(runnable, "anr-detector-test");
        detector.start();
        Assert.assertNotNull(mainCallbacks.poll(1, TimeUnit.SECONDS));

        runnable.shutdown();
        detector.join(1000L);

        Assert.assertFalse(detector.isAlive());
        Assert.assertEquals(0, reports.get());
    }

    private static final class FakeMainThreadPoster implements ANRDetectRunnable.MainThreadPoster {
        private final BlockingQueue<Runnable> callbacks;
        private final Thread thread;

        private FakeMainThreadPoster(BlockingQueue<Runnable> callbacks, Thread thread) {
            this.callbacks = callbacks;
            this.thread = thread;
        }

        @Override
        public boolean post(Runnable runnable) {
            return callbacks.offer(runnable);
        }

        @Override
        public Thread getThread() {
            return thread;
        }
    }
}
