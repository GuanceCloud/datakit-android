package com.ft.sdk.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.db.file.FTAtomicFileHelper;
import com.ft.sdk.garble.db.file.FTFileLock;
import com.ft.sdk.garble.db.file.FTFileStorePaths;
import com.ft.sdk.garble.db.file.FTRumFileAggregateStore;
import com.ft.sdk.garble.db.file.FTSyncFileDataStore;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.db.FTDataStoreManager;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FTFileStoreFoundationTest {
    private static final String DB_FLAT_MIGRATION_MARKER = "db_flat_migrated";

    @Test
    public void fileStorePathsCreateExpectedDirectories() throws Exception {
        File root = getTestRoot("paths");
        deleteRecursively(root);

        FTFileStorePaths paths = new FTFileStorePaths(root);
        paths.ensureReady();

        assertTrue(paths.getRootDir().isDirectory());
        assertTrue(paths.getSyncDir().isDirectory());
        assertTrue(paths.getRumViewDir().isDirectory());
        assertTrue(paths.getRumActionDir().isDirectory());
        assertEquals(new File(paths.getRootDir(), "store.lock"), paths.getLockFile());
    }

    @Test
    public void atomicFileHelperReplacesContent() throws Exception {
        File file = new File(getTestRoot("atomic"), "data.txt");
        deleteRecursively(file.getParentFile());

        FTAtomicFileHelper.writeUtf8(file, "first");
        assertEquals("first", FTAtomicFileHelper.readUtf8(file));

        FTAtomicFileHelper.writeUtf8(file, "second");
        assertEquals("second", FTAtomicFileHelper.readUtf8(file));
    }

    @Test
    public void fileLockSerializesUpdates() throws Exception {
        File root = getTestRoot("lock");
        deleteRecursively(root);

        FTFileLock lock = new FTFileLock(new File(root, "store.lock"));
        File counterFile = new File(root, "counter.txt");

        int threadCount = 4;
        int iterations = 25;
        List<Thread> threads = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < iterations; j++) {
                        try {
                            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                                @Override
                                public Void run() throws IOException {
                                    int value = readCounter(counterFile);
                                    FTAtomicFileHelper.writeUtf8(counterFile, String.valueOf(value + 1));
                                    return null;
                                }
                            });
                        } catch (Throwable e) {
                            synchronized (errors) {
                                errors.add(e);
                            }
                        }
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(0, errors.size());
        assertEquals(threadCount * iterations, readCounter(counterFile));
    }

    @Test
    public void rumFileAggregateStoreTracksActionAndViewCounts() throws Exception {
        File root = getTestRoot("rum");
        deleteRecursively(root);

        FTRumFileAggregateStore store = new FTRumFileAggregateStore(new FTFileStorePaths(root));
        ViewBean viewBean = createViewBean();
        ActionBean actionBean = createActionBean(viewBean);

        store.initSumView(viewBean);
        store.initSumAction(actionBean);
        store.increaseViewAction(viewBean.getId());
        store.increaseViewResource(viewBean.getId());
        store.increaseViewLongTask(viewBean.getId());
        store.increaseActionResource(actionBean.getId());
        store.increaseActionLongTask(actionBean.getId());
        store.closeAction(actionBean.getId(), 10L, true);

        List<ActionBean> actions = store.querySumAction(0);
        assertEquals(1, actions.size());
        assertEquals(1, actions.get(0).getResourceCount());
        assertEquals(1, actions.get(0).getLongTaskCount());

        List<ViewBean> views = store.querySumView(0);
        assertEquals(1, views.size());
        assertEquals(1, views.get(0).getActionCount());
        assertEquals(1, views.get(0).getResourceCount());
        assertEquals(1, views.get(0).getLongTaskCount());
    }

    @Test
    public void rumFileAggregateStoreUsesViewUploadFilterAndCleanup() throws Exception {
        File root = getTestRoot("rum_cleanup");
        deleteRecursively(root);

        FTRumFileAggregateStore store = new FTRumFileAggregateStore(new FTFileStorePaths(root));
        ViewBean viewBean = createViewBean();

        store.initSumView(viewBean);
        store.updateViewUpdateTime(viewBean.getId(), 100L);
        store.closeView(viewBean.getId(), 20L, 30L, viewBean.getAttrJsonString());
        assertEquals(1, store.querySumView(0).size());

        store.updateViewUploadTime(viewBean.getId(), 200L);
        assertEquals(0, store.querySumView(0).size());

        store.cleanCloseViewData();
        assertEquals(0, store.querySumView(0, true).size());
    }

    @Test
    public void fileDataStoreStoresQueriesAndDeletesSyncData() throws Exception {
        File root = getTestRoot("sync");
        deleteRecursively(root);

        FTSyncFileDataStore store = new FTSyncFileDataStore(new FTFileStorePaths(root));
        List<SyncData> dataList = new ArrayList<>();
        dataList.add(createSyncData(DataType.LOG, "uuid-1", 1L, "log-1"));
        dataList.add(createSyncData(DataType.LOG, "uuid-2", 2L, "log-2"));
        dataList.add(createSyncData(DataType.RUM_APP, "uuid-3", 3L, "rum-1"));

        assertTrue(store.insertFtOptList(dataList, false));
        assertEquals(2, store.queryTotalCount(DataType.LOG));
        assertEquals(1, store.queryTotalCount(DataType.RUM_APP));

        List<SyncData> logs = store.queryDataByDataByTypeLimit(1, DataType.LOG);
        assertEquals(1, logs.size());
        assertEquals("log-1", logs.get(0).getDataString());

        List<Long> ids = new ArrayList<>();
        ids.add(logs.get(0).getId());
        store.delete(ids);
        assertEquals(1, store.queryTotalCount(DataType.LOG));

        store.deleteOldestData(new DataType[]{DataType.LOG, DataType.RUM_APP}, 10);
        assertEquals(0, store.queryDataByDescLimit(0).size());
    }

    @Test
    public void fileDataStoreUpdatesSyncDataByUuid() throws Exception {
        File root = getTestRoot("sync_update");
        deleteRecursively(root);

        FTSyncFileDataStore store = new FTSyncFileDataStore(new FTFileStorePaths(root));
        SyncData first = createSyncData(DataType.RUM_APP, "view-uuid", 1L, "view-old");
        assertTrue(store.insertFtOperation(first, false));

        SyncData updated = createSyncData(DataType.RUM_APP, "view-uuid", 2L, "view-new");
        assertTrue(store.updateOrInsertSyncData(updated));

        List<SyncData> records = store.queryDataByDataByTypeLimit(0, DataType.RUM_APP);
        assertEquals(1, records.size());
        assertEquals("view-new", records.get(0).getDataString());
        assertEquals(2L, records.get(0).getTime());
    }

    @Test
    public void fileDataStoreUpdatesSampledTypeAndDeletesExpiredData() throws Exception {
        File root = getTestRoot("sync_sampled");
        deleteRecursively(root);

        FTSyncFileDataStore store = new FTSyncFileDataStore(new FTFileStorePaths(root));
        store.insertFtOperation(createSyncData(DataType.RUM_APP_ERROR_SAMPLED, "uuid-1", 10L, "error-1"), false);
        store.insertFtOperation(createSyncData(DataType.RUM_APP_ERROR_SAMPLED, "uuid-2", 30L, "error-2"), false);

        assertEquals(1, store.updateDataType(DataType.RUM_APP_ERROR_SAMPLED, 20L));
        assertEquals(1, store.queryTotalCount(DataType.RUM_APP));
        assertEquals(1, store.queryTotalCount(DataType.RUM_APP_ERROR_SAMPLED));

        assertEquals(1, store.deleteExpireCache(DataType.RUM_APP_ERROR_SAMPLED, 100L, 60L));
        assertEquals(0, store.queryTotalCount(DataType.RUM_APP_ERROR_SAMPLED));
    }

    @Test
    public void fileDataStoreConfigMigratesCurrentDbFlatCacheOnce() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
        FTFileStorePaths paths = new FTFileStorePaths(context);
        FTSDKConfig config = FTSDKConfig.builder()
                .enableFileDataStore()
                .setNeedTransformOldCache(true);

        try {
            FTDataStoreManager.release();
            FTDBManager.get().delete();
            deleteRecursively(paths.getRootDir());

            List<SyncData> dbData = new ArrayList<>();
            dbData.add(createSyncData(DataType.LOG, "db-uuid-1", 1L, "db-log-1"));
            dbData.add(createSyncData(DataType.RUM_APP, "db-uuid-2", 2L, "db-rum-1"));
            assertTrue(FTDBManager.get().insertFtOptList(dbData, false));
            assertEquals(2, FTDBManager.get().queryDataByDescLimit(0).size());

            FTDataStoreManager.init(config);

            List<SyncData> migratedData = FTDataStoreManager.get().queryDataByDescLimit(0);
            assertEquals(2, migratedData.size());
            assertEquals("db-log-1", migratedData.get(0).getDataString());
            assertEquals("db-rum-1", migratedData.get(1).getDataString());
            assertTrue(new File(paths.getRootDir(), DB_FLAT_MIGRATION_MARKER).exists());

            assertTrue(FTDBManager.get().insertFtOperation(
                    createSyncData(DataType.LOG, "db-uuid-3", 3L, "db-log-2"), false));
            FTDataStoreManager.release();
            FTDataStoreManager.init(config);

            List<SyncData> migratedAgainData = FTDataStoreManager.get().queryDataByDescLimit(0);
            assertEquals(2, migratedAgainData.size());
        } finally {
            FTDataStoreManager.release();
            FTDBManager.get().delete();
            deleteRecursively(paths.getRootDir());
        }
    }

    private int readCounter(File counterFile) throws IOException {
        if (!counterFile.exists()) {
            return 0;
        }
        String content = FTAtomicFileHelper.readUtf8(counterFile);
        if (content.length() == 0) {
            return 0;
        }
        return Integer.parseInt(content);
    }

    private File getTestRoot(String name) {
        Context context = InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
        return new File(context.getFilesDir(), "ft_file_store_foundation_test/" + name);
    }

    private ViewBean createViewBean() {
        ViewBean viewBean = new ViewBean();
        viewBean.setId("view-id");
        viewBean.setViewName("view-name");
        viewBean.setViewReferrer("view-referrer");
        viewBean.setSessionId("session-id");
        viewBean.setCollectType(CollectType.COLLECT_BY_SAMPLE);
        return viewBean;
    }

    private ActionBean createActionBean(ViewBean viewBean) {
        ActionBean actionBean = new ActionBean();
        actionBean.setId("action-id");
        actionBean.setActionName("action-name");
        actionBean.setActionType("click");
        actionBean.setViewId(viewBean.getId());
        actionBean.setViewName(viewBean.getViewName());
        actionBean.setViewReferrer(viewBean.getViewReferrer());
        actionBean.setSessionId(viewBean.getSessionId());
        actionBean.setCollectType(CollectType.COLLECT_BY_SAMPLE);
        return actionBean;
    }

    private SyncData createSyncData(DataType dataType, String uuid, long time, String dataString) {
        SyncData data = new SyncData(dataType);
        data.setUuid(uuid);
        data.setTime(time);
        data.setDataString(dataString);
        return data;
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
