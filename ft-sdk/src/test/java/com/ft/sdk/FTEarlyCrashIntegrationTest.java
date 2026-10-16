package com.ft.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.threadpool.BaseThreadPool;
import com.ft.sdk.garble.threadpool.DataUploadThreadPool;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class FTEarlyCrashIntegrationTest {
    private SharedPreferences preferences;
    private Object originalTracker;
    private Object originalHandler;
    private FTExceptionHandler handler;
    private FTRUMConfig config;
    private RecordingScheduler scheduler;
    private ThreadPoolExecutor originalUploadExecutor;
    private ThreadPoolExecutor originalEventExecutor;
    private ThreadPoolExecutor eventExecutor;
    private boolean originalAutoSync;
    private boolean originalMainProcess;
    private int uploadRuns;

    @Before
    public void setUp() throws Exception {
        preferences = RuntimeEnvironment.getApplication().getSharedPreferences(
                "early-crash-integration", Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
        originalTracker = field(FTEarlyCrashTracker.class, "instance").get(null);
        originalHandler = field(FTExceptionHandler.class, "instance").get(null);
        field(FTExceptionHandler.class, "instance").set(null, null);
        handler = FTExceptionHandler.get();
        config = new FTRUMConfig().setRumAppId("early-crash-test").setEnableTrackAppCrash(true);
        field(FTExceptionHandler.class, "config").set(handler, config);
        startRun(System.nanoTime());

        // Keep the real scheduling/coalescing logic, but advance queued tasks explicitly.
        // Neither recording executor starts worker threads or performs network requests.
        originalUploadExecutor = DataUploadThreadPool.get().getExecutor();
        scheduler = new RecordingScheduler();
        field(BaseThreadPool.class, "executor").set(DataUploadThreadPool.get(), scheduler);
        DataUploadThreadPool.get().initRunnable(() -> uploadRuns++);
        originalEventExecutor = EventConsumerThreadPool.get().getExecutor();
        eventExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()) {
            @Override
            public void execute(Runnable command) {
                getQueue().add(command);
            }
        };
        field(BaseThreadPool.class, "executor").set(EventConsumerThreadPool.get(), eventExecutor);

        SyncTaskManager manager = SyncTaskManager.get();
        originalAutoSync = field(SyncTaskManager.class, "autoSync").getBoolean(manager);
        originalMainProcess = field(SyncTaskManager.class, "isMainProcess").getBoolean(manager);
        manager.setAutoSync(true);
        field(SyncTaskManager.class, "isMainProcess").setBoolean(manager, true);
        FTHttpConfigManager.get().setDatakitUrl("https://datakit.example.com");
    }

    @After
    public void tearDown() throws Exception {
        scheduler.shutdownNow();
        eventExecutor.shutdownNow();
        field(BaseThreadPool.class, "executor").set(DataUploadThreadPool.get(), originalUploadExecutor);
        field(BaseThreadPool.class, "executor").set(EventConsumerThreadPool.get(), originalEventExecutor);
        DataUploadThreadPool.get().initRunnable(null);
        SyncTaskManager.get().setAutoSync(originalAutoSync);
        field(SyncTaskManager.class, "isMainProcess").setBoolean(SyncTaskManager.get(), originalMainProcess);
        field(FTEarlyCrashTracker.class, "instance").set(null, originalTracker);
        field(FTExceptionHandler.class, "instance").set(null, originalHandler);
        FTHttpConfigManager.release();
        preferences.edit().clear().commit();
    }

    @Test
    public void javaCrashWritesMarkerBeforeStackCollection() {
        assertJavaMarkerAtStackCollection(true);
        assertTrue(hasMarker());
    }

    @Test
    public void javaCrashAfterWindowDoesNotWriteMarker() throws Exception {
        startRun(System.nanoTime() - TimeUnit.SECONDS.toNanos(6));
        assertJavaMarkerAtStackCollection(false);
    }

    @Test
    public void disabledCrashCollectionDoesNotWriteMarker() {
        config.setEnableTrackAppCrash(false);
        assertJavaMarkerAtStackCollection(false);
        handler.uploadNativeCrashBackground(new File("tombstone_native.dump"), AppState.RUN, false, null);
        assertFalse(hasMarker());
    }

    @Test
    public void disabledRumDoesNotWriteMarker() {
        config.setRumAppId(null);
        assertJavaMarkerAtStackCollection(false);
        handler.uploadNativeCrashBackground(new File("tombstone_native.dump"), AppState.RUN, false, null);
        assertFalse(hasMarker());
    }

    @Test
    public void liveNativeCrashWritesMarkerBeforeConsumerRuns() {
        handler.uploadNativeCrashBackground(new File("tombstone_native.dump"), AppState.RUN, false, null);
        assertEquals(1, eventExecutor.getQueue().size());
        assertTrue(hasMarker());
    }

    @Test
    public void historicalNativeAndAnrDoNotWriteMarker() {
        handler.uploadNativeCrashBackground(new File("tombstone_native.dump"), AppState.RUN, true, null);
        handler.uploadNativeCrashBackground(new File("tombstone_anr.dump"), AppState.RUN, false, null);
        assertEquals(2, eventExecutor.getQueue().size());
        assertFalse(hasMarker());
    }

    @Test
    public void shortDelayIsUsedOnceAndLaterSchedulingStaysAtTenSeconds() throws Exception {
        seedPreviousCrash();
        SyncTaskManager.get().executeSyncPoll();
        assertEquals(2000, scheduler.delays.get(0).longValue());
        scheduler.runNext();
        assertEquals(1, uploadRuns);

        SyncTaskManager.get().executeSyncPoll();
        assertEquals(10000, scheduler.delays.get(1).longValue());
        scheduler.runNext();
        assertEquals(2, uploadRuns);
    }

    @Test
    public void duplicateTriggersKeepExistingImmediateRescheduleBehavior() throws Exception {
        seedPreviousCrash();
        SyncTaskManager.get().executeSyncPoll();
        SyncTaskManager.get().executeSyncPoll();
        assertEquals(1, scheduler.delays.size());
        scheduler.runNext();
        assertEquals(0, scheduler.delays.get(1).longValue());
        scheduler.runNext();
        SyncTaskManager.get().executeSyncPoll();
        assertEquals(10000, scheduler.delays.get(2).longValue());
    }

    @Test
    public void automaticUploadGuardsRemainEffectiveAndDoNotConsumeFirstSchedule() throws Exception {
        seedPreviousCrash();
        SyncTaskManager.get().setAutoSync(false);
        SyncTaskManager.get().executeSyncPoll();
        assertTrue(scheduler.delays.isEmpty());

        SyncTaskManager.get().setAutoSync(true);
        FTHttpConfigManager.release();
        SyncTaskManager.get().executeSyncPoll();
        assertTrue(scheduler.delays.isEmpty());

        FTHttpConfigManager.get().setDatakitUrl("https://datakit.example.com");
        field(SyncTaskManager.class, "isMainProcess").setBoolean(SyncTaskManager.get(), false);
        SyncTaskManager.get().executeSyncPoll();
        assertTrue(scheduler.delays.isEmpty());

        field(SyncTaskManager.class, "isMainProcess").setBoolean(SyncTaskManager.get(), true);
        SyncTaskManager.get().executeSyncPoll();
        assertEquals(2000, scheduler.delays.get(0).longValue());
    }

    @Test
    public void manualUploadStaysImmediateAndConsumesFirstOpportunity() throws Exception {
        seedPreviousCrash();
        SyncTaskManager.get().executePoll();
        assertEquals(0, scheduler.delays.get(0).longValue());
        scheduler.runNext();
        SyncTaskManager.get().executeSyncPoll();
        assertEquals(10000, scheduler.delays.get(1).longValue());
    }

    private void seedPreviousCrash() throws Exception {
        preferences.edit().putBoolean(FTEarlyCrashTracker.EARLY_CRASH, true).commit();
        startRun(System.nanoTime());
        assertFalse(hasMarker());
    }

    private void startRun(long startedAtNs) throws Exception {
        FTEarlyCrashTracker tracker = new FTEarlyCrashTracker();
        tracker.initialize(preferences, startedAtNs);
        field(FTEarlyCrashTracker.class, "instance").set(null, tracker);
    }

    private void assertJavaMarkerAtStackCollection(boolean expected) {
        RuntimeException stopCollection = new RuntimeException("stop after observing crash entry");
        Throwable probe = new Throwable() {
            @Override
            public void printStackTrace(PrintWriter writer) {
                assertEquals(expected, hasMarker());
                throw stopCollection;
            }
        };
        try {
            handler.uncaughtException(Thread.currentThread(), probe);
            fail("Probe should stop before unrelated crash collection work");
        } catch (RuntimeException e) {
            assertTrue(e == stopCollection);
        }
    }

    private boolean hasMarker() {
        return preferences.getBoolean(FTEarlyCrashTracker.EARLY_CRASH, false);
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static final class RecordingScheduler extends ScheduledThreadPoolExecutor {
        final List<Long> delays = new ArrayList<>();
        final List<Runnable> commands = new ArrayList<>();

        RecordingScheduler() {
            super(1);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            delays.add(unit.toMillis(delay));
            commands.add(command);
            return null;
        }

        void runNext() {
            commands.remove(0).run();
        }
    }
}
