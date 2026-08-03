package com.ft.sdk.garble.db;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.file.FTFileDataStore;
import com.ft.sdk.garble.db.file.FTFileStorePaths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class HistoricalExitShadowStoreTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shadowMirrorsCombinedHistoricalRumPersistence() throws Exception {
        FTFileDataStore primary = new FTFileDataStore(
                new FTFileStorePaths(temporaryFolder.newFolder("combined-primary")));
        FTFileDataStore shadow = new FTFileDataStore(
                new FTFileStorePaths(temporaryFolder.newFolder("combined-shadow")));
        FTShadowDataStore store = new FTShadowDataStore(primary, shadow);
        ViewBean view = new ViewBean();
        view.setId("view");
        store.initSumView(view);

        assertEquals(InsertResult.INSERTED,
                store.prepareHistoricalRum("dedupe", "view", data("first")));
        assertEquals(InsertResult.ALREADY_EXISTS,
                store.prepareHistoricalRum("dedupe", "view", data("second")));

        store.updateDataType(DataType.RUM_APP_ERROR_SAMPLED, 123);
        assertEquals(0, primary.queryDataByDescLimit(0).size());
        assertEquals(0, shadow.queryDataByDescLimit(0).size());
        assertEquals(InsertResult.INSERTED,
                store.commitHistoricalRum("dedupe", DataType.RUM_APP));

        assertEquals(1, primary.queryDataByDescLimit(0).size());
        assertEquals(1, shadow.queryDataByDescLimit(0).size());
        assertEquals(1, primary.querySumView(0, true).get(0).getErrorCount());
        assertEquals(1, shadow.querySumView(0, true).get(0).getErrorCount());
    }

    private SyncData data(String body) {
        SyncData data = new SyncData(DataType.RUM_APP);
        data.setTime(1);
        data.setUuid(body);
        data.setDataString(body);
        return data;
    }
}
