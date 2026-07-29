package com.ft.sdk.internal.anr.historical;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.db.file.FTFileStorePaths;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class FileHistoricalExitLedgerTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void leaseAllowsOnlyOneOwnerAndCanBeTakenOverAfterExpiry() throws Exception {
        FTFileStorePaths paths = new FTFileStorePaths(temporaryFolder.newFolder());
        FileHistoricalExitLedger firstProcess = new FileHistoricalExitLedger(paths);
        FileHistoricalExitLedger secondProcess = new FileHistoricalExitLedger(paths);
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app", 123, 20_000, ProcessExitRecord.REASON_ANR, 100, null);
        HistoricalRumContext context = new HistoricalRumContext(
                "com.example.app", "old-run", 1_000, "session", "view", "Home",
                10_000_000_000L, 0, false);

        assertEquals(HistoricalExitLedger.ClaimResult.ACQUIRED,
                firstProcess.tryClaim("exit", "owner-one", 100, 200,
                        exit, context, "dedupe").getResult());
        assertEquals(HistoricalExitLedger.ClaimResult.SKIPPED,
                secondProcess.tryClaim("exit", "owner-two", 150, 250,
                        exit, context, "dedupe").getResult());
        assertEquals(HistoricalExitLedger.ClaimResult.ACQUIRED,
                secondProcess.tryClaim("exit", "owner-two", 201, 301,
                        exit, context, "dedupe").getResult());

        secondProcess.markCommitted("exit", "owner-two", 220);

        assertEquals(HistoricalExitLedger.ClaimResult.SKIPPED,
                firstProcess.tryClaim("exit", "owner-one", 500, 600,
                        exit, context, "dedupe").getResult());
    }

    @Test
    public void leaseTakeoverPreservesTheFirstRumContextSnapshot() throws Exception {
        FTFileStorePaths paths = new FTFileStorePaths(temporaryFolder.newFolder());
        FileHistoricalExitLedger ledger = new FileHistoricalExitLedger(paths);
        ProcessExitRecord exit = new ProcessExitRecord(
                "com.example.app", 123, 20_000, ProcessExitRecord.REASON_ANR, 100, null);
        HistoricalRumContext original = new HistoricalRumContext(
                "com.example.app", "old-run", 1_000, "session-one", "view-one", "Home",
                10_000_000_000L, 0, false);
        HistoricalRumContext recomputed = new HistoricalRumContext(
                "com.example.app", "new-run", 2_000, "session-two", "view-two", "Checkout",
                11_000_000_000L, 0, false, CollectType.NOT_COLLECT);

        ledger.tryClaim("exit", "owner-one", 100, 200, exit, original, "dedupe");
        HistoricalExitLedger.Claim takeover =
                ledger.tryClaim("exit", "owner-two", 201, 301, exit, recomputed, "dedupe");

        File[] files = paths.getHistoricalExitDir().listFiles();
        assertEquals(1, files == null ? 0 : files.length);
        JSONObject persisted = new JSONObject(new String(
                Files.readAllBytes(files[0].toPath()), StandardCharsets.UTF_8));
        assertEquals("session-one", persisted.getString("session_id"));
        assertEquals("view-one", persisted.getString("view_id"));
        assertEquals("Home", persisted.getString("view_name"));
        assertEquals("session-one", takeover.getContext().getSessionId());
        assertEquals("view-one", takeover.getContext().getViewId());
        assertEquals(CollectType.COLLECT_BY_SAMPLE, takeover.getContext().getCollectType());
    }
}
