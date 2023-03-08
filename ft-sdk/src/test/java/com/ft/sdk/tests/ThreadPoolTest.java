package com.ft.sdk.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;

import org.junit.Test;

/**
 * 线程池相关测试
 *
 * @author Brandon
 */
public class ThreadPoolTest {


    /**
     * 线程池执行过程，中介重启，验证当下状态是否正确
     */
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

}
