package com.ft.sdk;

import com.ft.sdk.garble.bean.DataType;
import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;

public class FTVisitorIdContextTest {
    @org.junit.Before public void disableAndroidLogging() {
        com.ft.sdk.garble.utils.LogUtils.setDebug(false);
    }
    @Test public void trustedSnapshotWinsOverOrdinaryContexts() {
        SyncDataHelper helper = new SyncDataHelper();
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false)
                .addGlobalContext("visitor_id", "base")
                .setLineDataModifier((measurement, data) -> {
                    assertEquals("p11111111111111111111111111111111", data.get("visitor_id"));
                    HashMap<String, Object> changed = new HashMap<>();
                    changed.put("message", "changed");
                    return changed;
                }));
        helper.initRUMConfig(new FTRUMConfig().addGlobalContext("visitor_id", "rum"));
        helper.appendGlobalContext("visitor_id", "dynamic-base");
        helper.appendRUMGlobalContext("visitor_id", "dynamic-rum");
        HashMap<String, Object> tags = new HashMap<>();
        tags.put("visitor_id", "tag");
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("visitor_id", "property");
        fields.put("message", "original");
        String line = helper.getBodyContent("error", tags, fields, 1, DataType.RUM_APP, "uuid",
                "p11111111111111111111111111111111");
        assertTrue(line.contains("visitor_id=p11111111111111111111111111111111"));
        assertTrue(line.contains("message=\"changed\""));
        assertEquals(line.indexOf("visitor_id="), line.lastIndexOf("visitor_id="));
        assertFalse(line.contains("modifier"));
    }

    @Test public void finalModifiersCanRewriteOrRemoveSnapshotWithoutChangingManager() {
        FTVisitorIdManager manager = FTVisitorIdManager.get();
        manager.init(FTVisitorIdManagerTest.enabled(), new FTVisitorIdManagerTest.MemoryStore(),
                new FTVisitorIdManagerTest.TestClock());
        try {
            String id = manager.getId();
            for (boolean lineModifier : new boolean[]{false, true}) {
                for (String replacement : new String[]{"redacted", ""}) {
                    SyncDataHelper helper = new SyncDataHelper();
                    FTSDKConfig config = FTSDKConfig.builder().setEnableDataFilter(false);
                    if (lineModifier) {
                        config.setLineDataModifier((measurement, data) -> {
                            assertEquals(id, data.get("visitor_id"));
                            data.put("visitor_id", replacement);
                            return data;
                        });
                    } else {
                        config.setDataModifier((key, value) -> {
                            if (!key.equals("visitor_id")) return value;
                            assertEquals(id, value);
                            return replacement;
                        });
                    }
                    helper.initBaseConfig(config);
                    helper.initRUMConfig(new FTRUMConfig());
                    HashMap<String, Object> tags = new HashMap<>();
                    tags.put("visitor_id", "untrusted");
                    HashMap<String, Object> fields = new HashMap<>();
                    fields.put("visitor_id", "untrusted-field");
                    fields.put("message", "kept");
                    String line = helper.getBodyContent("resource", tags, fields, 1,
                            DataType.RUM_APP, "uuid", id);
                    if (replacement.isEmpty()) assertFalse(line.contains("visitor_id="));
                    else assertTrue(line.contains("visitor_id=redacted"));
                    assertTrue(line.contains("message=\"kept\""));
                    assertEquals(id, manager.getId());
                }
            }
        } finally { manager.stop(); }
    }

    @Test public void lineModifierRunsAfterDataModifierAndNullKeepsExistingValue() {
        for (String replacement : new String[]{"line-value", "", null}) {
            SyncDataHelper helper = new SyncDataHelper();
            helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false)
                    .setDataModifier((key, value) -> key.equals("visitor_id") ? "data-value" : null)
                    .setLineDataModifier((measurement, data) -> {
                        assertEquals("data-value", data.get("visitor_id"));
                        HashMap<String, Object> changes = new HashMap<>();
                        changes.put("visitor_id", replacement);
                        return changes;
                    }));
            helper.initRUMConfig(new FTRUMConfig());
            String line = helper.getBodyContent("error", new HashMap<>(), new HashMap<>(), 1,
                    DataType.RUM_APP_ERROR_SAMPLED, "uuid", "request-snapshot");
            if ("".equals(replacement)) assertFalse(line.contains("visitor_id="));
            else assertTrue(line.contains("visitor_id=" + (replacement == null ? "data-value" : replacement)));
        }
    }

    @Test public void noSnapshotDoesNotInventVisitorOrOverrideExistingContext() {
        SyncDataHelper helper = new SyncDataHelper();
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
        helper.initRUMConfig(new FTRUMConfig());
        String line = helper.getBodyContent("view", new HashMap<>(), new HashMap<>(), 1,
                DataType.RUM_APP, "uuid");
        assertFalse(line.contains("visitor_id"));
        HashMap<String, Object> tags = new HashMap<>();
        tags.put("visitor_id", "customer");
        line = helper.getBodyContent("view", tags, new HashMap<>(), 1, DataType.RUM_APP, "uuid");
        assertTrue(line.contains("visitor_id=customer"));
    }

    @Test public void nativeSnapshotDoesNotOverwriteBrowserVisitor() {
        SyncDataHelper helper = new SyncDataHelper();
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
        helper.initRUMConfig(new FTRUMConfig().addGlobalContext("visitor_id", "native-global"));
        helper.appendRUMGlobalContext("visitor_id", "native-dynamic");
        HashMap<String, Object> tags = new HashMap<>();
        tags.put("visitor_id", "browser");
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("duration", 1);
        String line = helper.getBodyContent("resource", tags, fields, 1,
                DataType.RUM_WEBVIEW, "uuid", "native");
        assertTrue(line.contains("visitor_id=browser"));
        assertFalse(line.contains("visitor_id=native"));
        tags.clear();
        line = helper.getBodyContent("resource", tags, fields, 1,
                DataType.RUM_WEBVIEW_ERROR_SAMPLED, "uuid", "native");
        assertFalse(line.contains("visitor_id="));
    }
}
