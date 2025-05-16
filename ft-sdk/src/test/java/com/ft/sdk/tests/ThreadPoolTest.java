package com.ft.sdk.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ft.sdk.garble.threadpool.DataProcessThreadPool;

import org.junit.Test;

/**
 * 线程池相关测试
 *
 * @author Brandon
 */
public class ThreadPoolTest {


    /**
     * 线程池执行过程，中间重启，验证当下状态是否正确
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
