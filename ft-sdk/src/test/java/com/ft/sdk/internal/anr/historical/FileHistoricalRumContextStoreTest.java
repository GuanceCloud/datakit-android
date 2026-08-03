package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.file.FTFileStorePaths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class FileHistoricalRumContextStoreTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void viewContextSurvivesStoreRecreationAndTracksCloseState() throws Exception {
        FTFileStorePaths paths = new FTFileStorePaths(temporaryFolder.newFolder("store"));
        FileHistoricalRumContextStore firstStore = new FileHistoricalRumContextStore(paths);
        ViewBean view = view("process-one", "run-one", "view-one");

        firstStore.save(view);

        FileHistoricalRumContextStore reopened = new FileHistoricalRumContextStore(paths);
        List<HistoricalRumContext> openContexts = reopened.load("process-one");
        assertEquals(1, openContexts.size());
        HistoricalRumContext open = openContexts.get(0);
        assertEquals("run-one", open.getProcessRunId());
        assertEquals("session", open.getSessionId());
        assertEquals("view-one", open.getViewId());
        assertFalse(open.isViewClosed());
        assertEquals(CollectType.COLLECT_BY_SAMPLE, open.getCollectType());

        view.setClose(true);
        view.setTimeSpent(2_000_000_000L);
        reopened.save(view);

        HistoricalRumContext closed = new FileHistoricalRumContextStore(paths)
                .load("process-one").get(0);
        assertTrue(closed.isViewClosed());
        assertEquals(12_000L, closed.getViewEndMs());
        assertTrue(new FileHistoricalRumContextStore(paths)
                .load("another-process").isEmpty());
    }

    @Test
    public void corruptContextDoesNotHideValidContextsOrLookEmpty() throws Exception {
        FTFileStorePaths paths = new FTFileStorePaths(temporaryFolder.newFolder("corrupt"));
        FileHistoricalRumContextStore store = new FileHistoricalRumContextStore(paths);
        store.save(view("process-one", "run-one", "valid-view"));
        paths.ensureReady();
        File corrupt = new File(paths.getHistoricalViewDir(), "broken.json");
        try (FileOutputStream output = new FileOutputStream(corrupt)) {
            output.write("not-json".getBytes(StandardCharsets.UTF_8));
        }

        try {
            store.load("process-one");
        } catch (HistoricalRumContextLoadException e) {
            assertEquals(1, e.getLoadedContexts().size());
            assertEquals("valid-view", e.getLoadedContexts().get(0).getViewId());
            return;
        }
        throw new AssertionError("Expected corrupt context to remain retryable");
    }

    private ViewBean view(String processName, String processRunId, String viewId) {
        ViewBean view = new ViewBean();
        view.setId(viewId);
        view.setProcessName(processName);
        view.setProcessRunId(processRunId);
        view.setProcessStartMs(9_000L);
        view.setSessionId("session");
        view.setViewName("Home");
        view.setStartTime(10_000_000_000L);
        view.setCollectType(CollectType.COLLECT_BY_SAMPLE);
        return view;
    }
}
