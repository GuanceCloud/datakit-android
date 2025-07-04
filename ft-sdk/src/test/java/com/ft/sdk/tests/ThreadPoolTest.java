package com.ft.sdk.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ft.sdk.garble.threadpool.DataProcessThreadPool;

import org.junit.Test;

/**
 * Thread pool related tests
 *
 * @author Brandon
 */
public class ThreadPoolTest {


    /**
     * Thread pool execution process, restart in the middle, verify if the current state is correct
     */
    @Test
    public void testReThreadPool() {

        DataProcessThreadPool.get().execute(() -> {
            while (true) {
            }
        });


        DataProcessThreadPool.get().shutDown();

        assertFalse(DataProcessThreadPool.get().poolRunning());


        DataProcessThreadPool.get().reStartPool();
        assertTrue(DataProcessThreadPool.get().poolRunning());
    }

}
