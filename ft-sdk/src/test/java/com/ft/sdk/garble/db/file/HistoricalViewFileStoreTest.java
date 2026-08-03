package com.ft.sdk.garble.db.file;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
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
public class HistoricalViewFileStoreTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void persistedViewRetainsProcessRunIdentity() throws Exception {
        FTFileDataStore store = new FTFileDataStore(
                new FTFileStorePaths(temporaryFolder.newFolder()));
        ViewBean view = new ViewBean();
        view.setId("view");
        view.setSessionId("session");
        view.setViewName("Home");
        view.setProcessName("com.example.app:worker");
        view.setProcessRunId("run-id");
        view.setProcessStartMs(1234);
        view.setCollectType(CollectType.COLLECT_BY_ERROR_SAMPLE);

        store.initSumView(view);

        ViewBean restored = store.querySumView(0, true).get(0);
        assertEquals("com.example.app:worker", restored.getProcessName());
        assertEquals("run-id", restored.getProcessRunId());
        assertEquals(1234, restored.getProcessStartMs());
        assertEquals(CollectType.COLLECT_BY_ERROR_SAMPLE, restored.getCollectType());
    }

    @Test
    public void historicalRumPersistenceIsIdempotentAsOneStoreOperation() throws Exception {
        FTFileDataStore store = new FTFileDataStore(
                new FTFileStorePaths(temporaryFolder.newFolder()));
        ViewBean view = new ViewBean();
        view.setId("view");
        store.initSumView(view);

        assertEquals(InsertResult.INSERTED,
                store.prepareHistoricalRum("dedupe", "view", data("first")));
        assertEquals(InsertResult.ALREADY_EXISTS,
                store.prepareHistoricalRum("dedupe", "view", data("second")));

        assertEquals(0, store.queryDataByDescLimit(0).size());
        assertEquals(InsertResult.INSERTED,
                store.commitHistoricalRum("dedupe", DataType.RUM_APP));

        assertEquals(1, store.queryDataByDescLimit(0).size());
        assertEquals("first", store.queryDataByDescLimit(0).get(0).getDataString());
        assertEquals(1, store.querySumView(0, true).get(0).getErrorCount());
    }

    private SyncData data(String body) {
        SyncData data = new SyncData(DataType.RUM_APP);
        data.setTime(1);
        data.setUuid(body);
        data.setDataString(body);
        return data;
    }
}
