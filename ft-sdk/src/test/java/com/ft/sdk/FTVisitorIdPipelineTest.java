package com.ft.sdk;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.threadpool.BaseThreadPool;
import com.ft.sdk.garble.threadpool.DataProcessThreadPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class FTVisitorIdPipelineTest {
    @Test public void queuedNativeAndRequestEventsKeepSnapshotsAcrossResetAndShutdown() throws Exception {
        com.ft.sdk.garble.utils.LogUtils.setDebug(false);
        FTVisitorIdManager manager = FTVisitorIdManager.get();
        manager.stop();
        manager.init(FTVisitorIdManagerTest.enabled(), new FTVisitorIdManagerTest.MemoryStore(),
                new FTVisitorIdManagerTest.TestClock());
        FTTrackInner tracker = FTTrackInner.getInstance();
        LinkedBlockingQueue<String> lines = new LinkedBlockingQueue<>();
        SyncDataHelper helper = new SyncDataHelper() {
            @Override public String getBodyContent(String measurement, HashMap<String, Object> tags,
                    HashMap<String, Object> fields, long time, DataType type, String uuid, String visitorId) {
                lines.add(super.getBodyContent(measurement, tags, fields, time, type, uuid, visitorId));
                // Exercise real queue, SyncData and serialization; discard before database/network work.
                return "";
            }
        };
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
        helper.initRUMConfig(new FTRUMConfig());
        Field helperField = FTTrackInner.class.getDeclaredField("dataHelper");
        helperField.setAccessible(true);
        Object originalHelper = helperField.get(tracker);
        Field executorField = BaseThreadPool.class.getDeclaredField("executor");
        executorField.setAccessible(true);
        DataProcessThreadPool pool = DataProcessThreadPool.get();
        Object originalExecutor = executorField.get(pool);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        CountDownLatch releaseQueue = new CountDownLatch(1);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(3);
        try {
            helperField.set(tracker, helper);
            executorField.set(pool, executor);
            executor.execute(() -> {
                entered.countDown();
                try { releaseQueue.await(5, TimeUnit.SECONDS); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            String requestId = manager.getId();
            tracker.rum(1, "action", new HashMap<>(), fields(), completed::countDown,
                    CollectType.COLLECT_BY_SAMPLE);
            manager.reset();
            assertNotEquals(requestId, manager.getId());
            tracker.rum(2, "resource", new HashMap<>(), fields(), completed::countDown,
                    CollectType.COLLECT_BY_SAMPLE, requestId);
            tracker.rum(3, "error", new HashMap<>(), fields(), completed::countDown,
                    CollectType.COLLECT_BY_ERROR_SAMPLE, requestId);
            manager.stop();
            releaseQueue.countDown();
            assertTrue(completed.await(5, TimeUnit.SECONDS));
            for (String measurement : new String[]{"action", "resource", "error"}) {
                String line = lines.poll(5, TimeUnit.SECONDS);
                assertNotNull(line);
                assertTrue(line.startsWith(measurement + ","));
                assertTrue(line.contains("visitor_id=" + requestId));
            }
        } finally {
            releaseQueue.countDown();
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            executorField.set(pool, originalExecutor);
            helperField.set(tracker, originalHelper);
            manager.stop();
        }
    }

    private HashMap<String, Object> fields() {
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "kept");
        return fields;
    }
}
