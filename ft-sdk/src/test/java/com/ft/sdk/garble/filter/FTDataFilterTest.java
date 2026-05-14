package com.ft.sdk.garble.filter;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class FTDataFilterTest {

    @Test
    public void loggingSourceAndFieldInList() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ source = \"test1\" and ( f1 in [\"1\", \"2\", \"3\"] )}"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("f1", "2");

        Assert.assertTrue(filter.isFiltered(DataType.LOG, "test1", tags, fields));
        Assert.assertFalse(filter.isFiltered(DataType.LOG, "test2", tags, fields));
    }

    @Test
    public void rumUsesMeasurementAsSource() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("rum", new String[]{
                "{ source = 'resource' and app_id = 'appid_xxx' }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_RUM_APP_ID, "appid_xxx");

        Assert.assertTrue(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_RESOURCE,
                tags, new HashMap<String, Object>()));
        Assert.assertFalse(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_ACTION,
                tags, new HashMap<String, Object>()));
    }

    @Test
    public void regexAndNilOperators() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ source = re('test.*') and missing = nil }",
                "{ message match ['drop.*'] }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "keep");
        Assert.assertTrue(filter.isFiltered(DataType.LOG, "test-log",
                new HashMap<String, Object>(), fields));

        fields.put("message", "drop this");
        Assert.assertTrue(filter.isFiltered(DataType.LOG, "other",
                new HashMap<String, Object>(), fields));

        fields.put("message", "please drop this");
        Assert.assertTrue(filter.isFiltered(DataType.LOG, "other",
                new HashMap<String, Object>(), fields));
    }

    @Test
    public void unsupportedCategoryIsIgnored() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("metric", new String[]{"{ measurement = 'cpu' }"});
        FTDataFilter filter = FTDataFilter.compile(rules);

        Assert.assertFalse(filter.isFiltered(DataType.LOG, "cpu",
                new HashMap<String, Object>(), new HashMap<String, Object>()));
    }

    @Test
    public void commaAndNumberComparison() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("rum", new String[]{
                "{ source = 'resource', duration >= 1000 }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("duration", 1200L);

        Assert.assertTrue(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_RESOURCE,
                new HashMap<String, Object>(), fields));

        fields.put("duration", 800L);
        Assert.assertFalse(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_RESOURCE,
                new HashMap<String, Object>(), fields));
    }
}
