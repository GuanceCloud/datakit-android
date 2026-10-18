package com.ft.sdk.garble.db;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.*;
import com.ft.sdk.garble.db.file.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ViewTimingPersistenceTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private String databaseName;
    private Object oldApplication;
    private Field application;

    @Before public void setup() throws Exception {
        application = FTApplication.class.getDeclaredField("instance");
        application.setAccessible(true);
        oldApplication = application.get(null);
        application.set(null, RuntimeEnvironment.getApplication());
        databaseName = FTDBConfig.DATABASE_NAME;
        FTDBConfig.DATABASE_NAME = "view-timing-" + System.nanoTime() + ".db";
        FTDBManager.release();
        FTContentProvider provider = Robolectric.buildContentProvider(FTContentProvider.class).create().get();
        ShadowContentResolver.registerProviderInternal(FTContentProvider.getUriViewData().getAuthority(), provider);
    }

    @After public void cleanup() throws Exception {
        FTDBManager.release();
        RuntimeEnvironment.getApplication().deleteDatabase(FTDBConfig.DATABASE_NAME);
        FTDBConfig.DATABASE_NAME = databaseName;
        application.set(null, oldApplication);
    }

    @Test public void bothStoresKeepNullableViewMetricsAndExplicitActionStopsWithoutSchemaChanges() throws Exception {
        for (FTDataStore store : new FTDataStore[]{FTDBManager.get(),
                new FTFileDataStore(new FTFileStorePaths(folder.newFolder()))}) {
            ActiveViewBean active = new ActiveViewBean("screen", "parent", -1, "session");
            active.setCollectType(CollectType.COLLECT_BY_SAMPLE);
            ViewBean view = active.convertToViewBean();
            view.setId("abcdef0123456789abcdef0123456789");
            store.initSumView(view);
            ViewBean restored = store.querySumView(0, true).get(0);
            assertNull(restored.networkSettledTime);
            assertNull(restored.interactionToNextViewTime);
            view.networkSettledTime = 0L;
            view.interactionToNextViewTime = 250_000_000L;
            store.updateViewExtraAttr(view.getId(), view.getAttrJsonString());
            restored = store.querySumView(0, true).get(0);
            assertEquals(Long.valueOf(0), restored.networkSettledTime);
            assertEquals(Long.valueOf(250_000_000), restored.interactionToNextViewTime);
            view.networkSettledTime = null;
            store.closeView(view.getId(), -1, 1000, view.getAttrJsonString());
            restored = store.querySumView(0, true).get(0);
            assertNull(restored.networkSettledTime);
            assertEquals(Long.valueOf(250_000_000), restored.interactionToNextViewTime);

            ActiveActionBean action = new ActiveActionBean("tap", "click", "session",
                    view.getId(), "screen", "parent", true);
            action.setCollectType(CollectType.COLLECT_BY_SAMPLE);
            store.initSumAction(action.convertToActionBean());
            store.increaseActionError(action.getId());
            action.interactionStopTimeNano = action.getStartTimeNanoForDuration() + 100;
            store.updateActionExtraAttr(action.getId(), action.getAttrJsonString());
            store.closeAction(action.getId(), 300, true);
            ActionBean persisted = store.querySumAction(0).get(0);
            assertEquals(action.getInteractionTimeNano(), persisted.getInteractionTimeNano());
            assertEquals(1, persisted.getErrorCount());
            assertEquals(300, persisted.getDuration());
        }
    }
}
