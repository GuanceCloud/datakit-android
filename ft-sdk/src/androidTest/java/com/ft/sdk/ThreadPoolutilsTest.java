package com.ft.sdk;

import com.ft.sdk.garble.utils.ThreadPoolUtils;

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
        ThreadPoolUtils.get().execute(new Runnable() {
            @Override
            public void run() {
                while (true){}
            }
        });
        ThreadPoolUtils.get().shutDown();
    }

    @Test
    public void testThreadPool(){
        assertFalse(ThreadPoolUtils.get().poolRunning());
    }

    @Test
    public void testReThreadPool(){
        ThreadPoolUtils.get().reStartPool();
        assertTrue(ThreadPoolUtils.get().poolRunning());
    }
}
