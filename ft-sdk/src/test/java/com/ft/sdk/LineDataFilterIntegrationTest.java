package com.ft.sdk;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.filter.FTDataFilterManager;
import com.ft.sdk.garble.utils.LogUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineDataFilterIntegrationTest {

    @After
    public void tearDown() {
        FTDataFilterManager.release();
        FTHttpConfigManager.release();
        LogUtils.setDebug(false);
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
                "{ source in ['custom_log'] and message in ['drop'] }"
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
                "{ source in ['custom_log'] and message in ['drop'] }"
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
                "{ source in ['custom_log'] and message in ['drop'] }"
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

    @Test
    public void uploadRecheckDropsRemoteFilteredRowsAndBypassesForRemainingRows() throws Exception {
        FTDataFilterManager.get().init(FTSDKConfig.builder().setEnableDataFilter(true));
        setRemoteFilterState("remote-a", supportedRemoteRules(), true);

        List<SyncData> batch = new ArrayList<>();
        SyncData dropped = syncData(1, "custom_log,sdk_data_id=drop_uuid message=\"drop\" 100\n");
        SyncData uploaded = syncData(2, "custom_log,sdk_data_id=keep_uuid message=\"keep\" 101\n");
        batch.add(dropped);
        batch.add(uploaded);

        FTDataFilterManager.UploadFilterResult result =
                FTDataFilterManager.get().prepareForUpload(DataType.LOG, batch);

        Assert.assertTrue(result.isDisableServerFilter());
        Assert.assertEquals(1, result.getFilteredDataList().size());
        Assert.assertEquals(dropped, result.getFilteredDataList().get(0));
        Assert.assertEquals(1, result.getUploadDataList().size());
        Assert.assertEquals(uploaded, result.getUploadDataList().get(0));
    }

    @Test
    public void uploadRecheckKeepsServerFilterWhenAnyRowCannotBeParsed() throws Exception {
        FTDataFilterManager.get().init(FTSDKConfig.builder().setEnableDataFilter(true));
        setRemoteFilterState("remote-a", supportedRemoteRules(), true);

        List<SyncData> batch = new ArrayList<>();
        SyncData invalid = syncData(1, "bad line protocol\n");
        SyncData uploaded = syncData(2, "custom_log,sdk_data_id=keep_uuid message=\"keep\" 101\n");
        batch.add(invalid);
        batch.add(uploaded);

        FTDataFilterManager.UploadFilterResult result =
                FTDataFilterManager.get().prepareForUpload(DataType.LOG, batch);

        Assert.assertFalse(result.isDisableServerFilter());
        Assert.assertTrue(result.getFilteredDataList().isEmpty());
        Assert.assertEquals(2, result.getUploadDataList().size());
    }

    @Test
    public void unsupportedRemoteRulesSkipUploadRecheckAndServerFilterBypass() throws Exception {
        FTDataFilterManager.get().init(FTSDKConfig.builder().setEnableDataFilter(true));
        setRemoteFilterState("remote-a", unsupportedRemoteRules(), false);

        List<SyncData> batch = new ArrayList<>();
        SyncData wouldDropIfParsed = syncData(1,
                "custom_log,sdk_data_id=drop_uuid message=\"drop\" 100\n");
        batch.add(wouldDropIfParsed);

        FTDataFilterManager.UploadFilterResult result =
                FTDataFilterManager.get().prepareForUpload(DataType.LOG, batch);

        Assert.assertFalse(result.isDisableServerFilter());
        Assert.assertTrue(result.getFilteredDataList().isEmpty());
        Assert.assertEquals(1, result.getUploadDataList().size());
        Assert.assertEquals(wouldDropIfParsed, result.getUploadDataList().get(0));
    }

    @Test
    public void remoteRulesMustBeSdkParseableBeforeServerFilterBypass() throws Exception {
        Method canDisable = FTDataFilterManager.class
                .getDeclaredMethod("canDisableServerFilter", Map.class);
        canDisable.setAccessible(true);
        FTDataFilterManager manager = FTDataFilterManager.get();

        Assert.assertTrue((Boolean) canDisable.invoke(manager, supportedRemoteRules()));
        Assert.assertFalse((Boolean) canDisable.invoke(manager, unsupportedRemoteRules()));
    }

    private HashMap<String, String[]> supportedRemoteRules() {
        HashMap<String, String[]> filters = new HashMap<>();
        filters.put("logging", new String[]{
                "{ source in ['custom_log'] and message in ['drop'] }"
        });
        return filters;
    }

    private HashMap<String, String[]> unsupportedRemoteRules() {
        HashMap<String, String[]> filters = new HashMap<>();
        filters.put("logging", new String[]{
                "{ message = 'drop' }"
        });
        return filters;
    }

    private void setRemoteFilterState(String id, HashMap<String, String[]> rules,
                                      boolean canDisableServerFilter) throws Exception {
        FTDataFilterManager manager = FTDataFilterManager.get();
        Field enabled = FTDataFilterManager.class.getDeclaredField("enabled");
        enabled.setAccessible(true);
        enabled.setBoolean(manager, true);

        Class<?> filterClass = Class.forName("com.ft.sdk.garble.filter.FTDataFilter");
        Method compile = filterClass.getDeclaredMethod("compile", Map.class);
        compile.setAccessible(true);
        Object compiledFilter = compile.invoke(null, rules);

        Class<?> stateClass = Class.forName("com.ft.sdk.garble.filter.FTDataFilterManager$RemoteFilterState");
        Method synced = stateClass.getDeclaredMethod("synced", String.class, filterClass, boolean.class);
        synced.setAccessible(true);
        Object state = synced.invoke(null, id, compiledFilter, canDisableServerFilter);

        Field remoteFilterState = FTDataFilterManager.class.getDeclaredField("remoteFilterState");
        remoteFilterState.setAccessible(true);
        remoteFilterState.set(manager, state);
    }

    private SyncData syncData(long id, String lineProtocol) {
        SyncData data = new SyncData(DataType.LOG);
        data.setId(id);
        data.setUuid("uuid_" + id);
        data.setTime(System.nanoTime());
        data.setDataString(lineProtocol);
        return data;
    }
}
