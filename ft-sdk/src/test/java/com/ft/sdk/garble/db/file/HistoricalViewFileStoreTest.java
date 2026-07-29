package com.ft.sdk.garble.db.file;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.ViewBean;

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
    public void historicalErrorCountIsAppliedOncePerDedupeKey() throws Exception {
        FTFileDataStore store = new FTFileDataStore(
                new FTFileStorePaths(temporaryFolder.newFolder()));
        ViewBean view = new ViewBean();
        view.setId("view");
        view.setSessionId("session");
        view.setViewName("Home");
        store.initSumView(view);

        store.increaseViewErrorOnce("view", "historical-anr:v1:exit");
        store.increaseViewErrorOnce("view", "historical-anr:v1:exit");

        assertEquals(1, store.querySumView(0, true).get(0).getErrorCount());
    }
}
