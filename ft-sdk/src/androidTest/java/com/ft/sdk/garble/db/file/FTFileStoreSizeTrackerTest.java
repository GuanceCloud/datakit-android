package com.ft.sdk.garble.db.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;

import org.junit.Test;

import java.io.File;

public class FTFileStoreSizeTrackerTest {

    @Test
    public void trackerKeepsSizeInSyncWithFileMutations() throws Exception {
        File root = getTestRoot("tracker_delta");
        deleteRecursively(root);

        FTFileStorePaths paths = new FTFileStorePaths(root);
        FTFileStoreSizeTracker tracker = new FTFileStoreSizeTracker(paths);
        FTSyncFileDataStore syncStore = new FTSyncFileDataStore(paths, tracker);
        FTRumFileAggregateStore rumStore = new FTRumFileAggregateStore(paths, tracker);

        tracker.reset(syncStore.size() + rumStore.size());
        assertTrackerMatchesStoreSize(tracker, syncStore, rumStore);

        assertTrue(syncStore.insertFtOperation(
                createSyncData(DataType.LOG, "uuid-1", 1L, "small-payload"), false));
        assertTrackerMatchesStoreSize(tracker, syncStore, rumStore);

        assertTrue(syncStore.updateOrInsertSyncData(
                createSyncData(DataType.LOG, "uuid-1", 2L, createPayload(256))));
        assertTrackerMatchesStoreSize(tracker, syncStore, rumStore);

        ViewBean viewBean = createViewBean();
        rumStore.initSumView(viewBean);
        assertTrackerMatchesStoreSize(tracker, syncStore, rumStore);

        rumStore.closeView(viewBean.getId(), 10L, 20L, createPayload(128));
        assertTrackerMatchesStoreSize(tracker, syncStore, rumStore);

        syncStore.deleteOldestData(DataType.LOG, 1);
        assertTrackerMatchesStoreSize(tracker, syncStore, rumStore);

        rumStore.cleanCloseViewData();
        assertTrackerMatchesStoreSize(tracker, syncStore, rumStore);
    }

    @Test
    public void trackerUsesSharedMetadataForStaleProcessUpdates() throws Exception {
        File root = getTestRoot("tracker_metadata");
        deleteRecursively(root);

        FTFileStorePaths paths = new FTFileStorePaths(root);
        FTFileStoreSizeTracker firstTracker = new FTFileStoreSizeTracker(paths);
        FTFileStoreSizeTracker secondTracker = new FTFileStoreSizeTracker(paths);
        FTSyncFileDataStore firstSyncStore = new FTSyncFileDataStore(paths, firstTracker);
        FTSyncFileDataStore secondSyncStore = new FTSyncFileDataStore(paths, secondTracker);
        FTRumFileAggregateStore rumStore = new FTRumFileAggregateStore(paths, firstTracker);

        firstTracker.reset(firstSyncStore.size() + rumStore.size());
        assertTrue(secondTracker.initializeFromMetadata());
        assertEquals(firstTracker.currentSize(), secondTracker.currentSize());

        assertTrue(firstSyncStore.insertFtOperation(
                createSyncData(DataType.LOG, "first-uuid", 1L, createPayload(64)), false));
        long firstWriteSize = firstSyncStore.size() + rumStore.size();
        assertEquals(firstWriteSize, firstTracker.currentSize());

        assertTrue(secondSyncStore.insertFtOperation(
                createSyncData(DataType.LOG, "second-uuid", 2L, createPayload(128)), false));
        long secondWriteSize = firstSyncStore.size() + rumStore.size();
        assertEquals(secondWriteSize, secondTracker.currentSize());

        firstSyncStore.deleteOldestData(DataType.LOG, 1);
        long afterDeleteSize = firstSyncStore.size() + rumStore.size();
        assertEquals(afterDeleteSize, firstTracker.currentSize());
        assertTrue(afterDeleteSize < secondWriteSize);

        FTFileStoreSizeTracker reloadedTracker = new FTFileStoreSizeTracker(paths);
        assertTrue(reloadedTracker.initializeFromMetadata());
        assertEquals(afterDeleteSize, reloadedTracker.currentSize());
    }

    private void assertTrackerMatchesStoreSize(FTFileStoreSizeTracker tracker,
                                               FTSyncFileDataStore syncStore,
                                               FTRumFileAggregateStore rumStore) {
        assertEquals(syncStore.size() + rumStore.size(), tracker.currentSize());
    }

    private File getTestRoot(String name) {
        Context context = InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
        return new File(context.getFilesDir(), "ft_file_store_size_tracker_test/" + name);
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

    private SyncData createSyncData(DataType dataType, String uuid, long time, String dataString) {
        SyncData data = new SyncData(dataType);
        data.setUuid(uuid);
        data.setTime(time);
        data.setDataString(dataString);
        return data;
    }

    private String createPayload(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append('x');
        }
        return builder.toString();
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
