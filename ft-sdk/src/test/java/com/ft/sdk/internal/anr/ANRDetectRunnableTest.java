package com.ft.sdk.internal.anr;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ANRDetectRunnableTest {

    @Test
    public void reportsOncePerEpisodeAndRearmsAfterMainThreadRecovery() throws Exception {
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

        Runnable firstEpisodeCallback = mainCallbacks.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull(firstEpisodeCallback);
        Assert.assertEquals(Integer.valueOf(1), reports.poll(1, TimeUnit.SECONDS));
        Assert.assertNull(reports.poll(100, TimeUnit.MILLISECONDS));

        firstEpisodeCallback.run();
        Runnable secondEpisodeCallback = mainCallbacks.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull(secondEpisodeCallback);
        Assert.assertEquals(Integer.valueOf(2), reports.poll(1, TimeUnit.SECONDS));

        runnable.shutdown();
        detector.join(1000L);
        Assert.assertFalse(detector.isAlive());
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
