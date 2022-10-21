package com.ft.sdk.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;

import org.junit.Test;

/**
 *
 */
public class ThreadPoolTest {


    @Test
    public void testReThreadPool() {

        DataUploaderThreadPool.get().execute(() -> {
            while (true) {
            }
        });


        DataUploaderThreadPool.get().shutDown();

        assertFalse(DataUploaderThreadPool.get().poolRunning());


        DataUploaderThreadPool.get().reStartPool();
        assertTrue(DataUploaderThreadPool.get().poolRunning());
    }

    @Test
    public void eventThreadPoolConsumer() {

    }
}
