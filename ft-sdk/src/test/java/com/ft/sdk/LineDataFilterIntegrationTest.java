package com.ft.sdk;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.filter.FTDataFilterManager;
import com.ft.sdk.garble.utils.LogUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class LineDataFilterIntegrationTest {

    @After
    public void tearDown() {
        FTDataFilterManager.release();
        FTHttpConfigManager.release();
        LogUtils.setDebug(true);
    }

    @Test
    public void dataFilterIsEnabledByDefault() {
        FTSDKConfig config = FTSDKConfig.builder();

        Assert.assertTrue(config.isEnableDataFilter());
        Assert.assertEquals(30 * 60, config.getDataFilterUpdateInterval());
    }

    @Test
    public void lineModifierRunsBeforeLocalFilter() throws Exception {
        HashMap<String, String[]> filters = new HashMap<>();
        filters.put("logging", new String[]{
                "{ source = 'custom_log' and message = 'drop' }"
        });

        FTSDKConfig config = FTSDKConfig.builder()
                .setEnableDataFilter(true)
                .setDataFilters(filters)
                .setLineDataModifier(new LineDataModifier() {
                    @Override
                    public Map<String, Object> modify(String measurement, HashMap<String, Object> data) {
                        HashMap<String, Object> changed = new HashMap<>();
                        changed.put("message", "drop");
                        return changed;
                    }
                });

        LogUtils.setDebug(false);
        FTDataFilterManager.get().init(config);
        SyncDataHelper helper = new SyncDataHelper();
        helper.initBaseConfig(config);

        HashMap<String, Object> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "keep");

        boolean filtered = helper.appLineModifier(DataType.LOG, "custom_log",
                "test-filter-uuid", tags, fields);

        Assert.assertTrue(filtered);
        Assert.assertEquals("drop", fields.get("message"));
    }

    @Test
    public void invalidRemoteFilterPullDoesNotDisableLocalFilter() throws Exception {
        HashMap<String, String[]> filters = new HashMap<>();
        filters.put("logging", new String[]{
                "{ source = 'custom_log' and message = 'drop' }"
        });

        FTSDKConfig config = FTSDKConfig.builder("bad-dataway-url", "test-token")
                .setEnableDataFilter(true)
                .setDataFilters(filters);

        LogUtils.setDebug(false);
        FTHttpConfigManager.get().initParams(config);
        FTDataFilterManager.get().init(config);

        HashMap<String, Object> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "drop");

        boolean filtered = new SyncDataHelper().appLineModifier(DataType.LOG, "custom_log",
                "test-filter-uuid", tags, fields);

        Assert.assertTrue(filtered);
        Assert.assertFalse(FTDataFilterManager.get().shouldDisableServerFilter());
    }

    @Test
    public void invalidDatakitRemoteFilterPullDoesNotDisableLocalFilter() throws Exception {
        HashMap<String, String[]> filters = new HashMap<>();
        filters.put("logging", new String[]{
                "{ source = 'custom_log' and message = 'drop' }"
        });

        FTSDKConfig config = FTSDKConfig.builder("bad-datakit-url")
                .setEnableDataFilter(true)
                .setDataFilters(filters);

        LogUtils.setDebug(false);
        FTHttpConfigManager.get().initParams(config);
        FTDataFilterManager.get().init(config);

        HashMap<String, Object> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "drop");

        boolean filtered = new SyncDataHelper().appLineModifier(DataType.LOG, "custom_log",
                "test-filter-uuid", tags, fields);

        Assert.assertTrue(filtered);
        Assert.assertFalse(FTDataFilterManager.get().shouldDisableServerFilter());
    }
}
