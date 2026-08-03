package com.ft.sdk.garble.db.file;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.InsertResult;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class HistoricalSyncDataFileStoreTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void sameDedupeKeyIsPersistedOnlyOnce() throws Exception {
        FTFileStorePaths paths = new FTFileStorePaths(temporaryFolder.newFolder());
        FTSyncFileDataStore store = new FTSyncFileDataStore(paths);

        assertEquals(InsertResult.INSERTED,
                store.prepareHistoricalRum("historical-anr:v1:key", data("first")));
        assertEquals(InsertResult.ALREADY_EXISTS,
                store.prepareHistoricalRum("historical-anr:v1:key", data("second")));
        assertEquals(InsertResult.INSERTED,
                store.commitHistoricalRum("first", DataType.RUM_APP));
        assertEquals(1, store.queryDataByDescLimit(0).size());
        assertEquals("first", store.queryDataByDescLimit(0).get(0).getDataString());
    }

    @Test
    public void dataTypeRewriteKeepsHistoricalDedupeKey() throws Exception {
        FTFileStorePaths paths = new FTFileStorePaths(temporaryFolder.newFolder());
        FTSyncFileDataStore store = new FTSyncFileDataStore(paths);

        assertEquals(InsertResult.INSERTED,
                store.prepareHistoricalRum(
                        "historical-anr:v1:key",
                        data(DataType.RUM_APP_ERROR_SAMPLED, "first")));
        assertEquals(InsertResult.INSERTED,
                store.commitHistoricalRum("first", DataType.RUM_APP_ERROR_SAMPLED));

        store.updateDataType(DataType.RUM_APP_ERROR_SAMPLED, 123);

        assertEquals(InsertResult.ALREADY_EXISTS,
                store.prepareHistoricalRum(
                        "historical-anr:v1:key",
                        data(DataType.RUM_APP, "second")));
        assertEquals(1, store.queryDataByDescLimit(0).size());
    }

    @Test
    public void stableUuidPreventsDuplicateAfterDedupeMetadataIsLost() throws Exception {
        FTFileStorePaths paths = new FTFileStorePaths(temporaryFolder.newFolder());
        FTSyncFileDataStore store = new FTSyncFileDataStore(paths);
        SyncData resyncedFromDatabase = data("stable-uuid");
        resyncedFromDatabase.setUuid("stable-uuid");
        store.replaceAll(java.util.Collections.singletonList(resyncedFromDatabase));

        SyncData retry = data("retry");
        retry.setUuid("stable-uuid");
        assertEquals(InsertResult.ALREADY_EXISTS,
                store.prepareHistoricalRum("historical-anr:v1:key", retry));
        assertEquals(1, store.queryDataByDescLimit(0).size());
    }

    private static SyncData data(String body) {
        return data(DataType.RUM_APP, body);
    }

    private static SyncData data(DataType type, String body) {
        SyncData data = new SyncData(type);
        data.setTime(123);
        data.setUuid(body);
        data.setDataString(body);
        return data;
    }
}
