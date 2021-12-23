package com.ft.sdk.tests;

import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * BY huangDianHua
 * DATE:2019-12-18 14:20
 * Description:
 */
public class ThreadPoolutilsTest {
    @Before
    public void threadPool(){
        DataUploaderThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                while (true){}
            }
        });
        DataUploaderThreadPool.get().shutDown();
    }

    @Test
    public void testThreadPool(){
        assertFalse(DataUploaderThreadPool.get().poolRunning());
    }

    @Test
    public void testReThreadPool(){
        DataUploaderThreadPool.get().reStartPool();
        assertTrue(DataUploaderThreadPool.get().poolRunning());
    }
}
