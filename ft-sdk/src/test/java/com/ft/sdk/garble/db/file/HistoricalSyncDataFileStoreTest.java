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
                store.insertSyncDataIfAbsent("historical-anr:v1:key", data("first")));
        assertEquals(InsertResult.ALREADY_EXISTS,
                store.insertSyncDataIfAbsent("historical-anr:v1:key", data("second")));
        assertEquals(1, store.queryDataByDescLimit(0).size());
        assertEquals("first", store.queryDataByDescLimit(0).get(0).getDataString());
    }

    private static SyncData data(String body) {
        SyncData data = new SyncData(DataType.RUM_APP);
        data.setTime(123);
        data.setUuid(body);
        data.setDataString(body);
        return data;
    }
}
