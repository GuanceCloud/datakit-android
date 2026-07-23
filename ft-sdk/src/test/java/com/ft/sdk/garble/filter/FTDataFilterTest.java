package com.ft.sdk.garble.filter;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

public class FTDataFilterTest {

    @Test
    public void loggingSourceAndFieldInList() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ source in ['test1'] and f1 in ['1', '2', '3'] }"
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
                "{ source in ['resource'] and app_id in ['appid_xxx'] }"
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
    public void anyAndAllRelationsAreSupported() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ source in ['custom_log'] and ( message in ['drop'] or level in ['error'] ) }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "keep");
        fields.put("level", "error");

        Assert.assertTrue(filter.isFiltered(DataType.LOG, "custom_log",
                new HashMap<String, Object>(), fields));

        fields.put("level", "info");
        Assert.assertFalse(filter.isFiltered(DataType.LOG, "custom_log",
                new HashMap<String, Object>(), fields));
    }

    @Test
    public void notInAndNotMatchOperatorsUseSpacedSyntax() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ env not in ['prod'] and message not match ['ignore.*'] }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> tags = new HashMap<>();
        tags.put("env", "test");
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "keep");

        Assert.assertTrue(filter.isFiltered(DataType.LOG, "test",
                tags, fields));

        tags.put("env", "prod");
        Assert.assertFalse(filter.isFiltered(DataType.LOG, "test",
                tags, fields));

        tags.put("env", "test");
        fields.put("message", "ignore this");
        Assert.assertFalse(filter.isFiltered(DataType.LOG, "test",
                tags, fields));
    }

    @Test
    public void realLoggingRuleSyntaxSupportsCompactNegativeOperators() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{  `source` in [ 'df_rum_ios_log' ,  'df_rum_android_log' ]  and ( `status` in [ 'ok' ,  'info' ,  'debug' ]  and  `message` match [ '.*password.*' ]  and  `env` notin [ 'prod' ,  'gray' ]  and  `message` notmatch [ '.*error.*' ] )}"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> tags = new HashMap<>();
        tags.put("env", "test");
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("status", "debug");
        fields.put("message", "contains password");

        Assert.assertTrue(filter.isFiltered(DataType.LOG, "df_rum_android_log",
                tags, fields));

        tags.put("env", "gray");
        Assert.assertFalse(filter.isFiltered(DataType.LOG, "df_rum_android_log",
                tags, fields));

        tags.put("env", "test");
        fields.put("message", "contains password error");
        Assert.assertFalse(filter.isFiltered(DataType.LOG, "df_rum_android_log",
                tags, fields));
    }

    @Test
    public void realRumResourceRulesDropHttpErrors() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("rum", new String[]{
                "{  `app_id` in [ 'appid_test' ]  and ( `resource_status` in [ '404' ]  or  `resource_status` match [ '5..' ] )}",
                "{  `app_id` in [ 'appid_test' ]  and ( `source` in [ 'resource' ]  and  `resource_status_group` notmatch [ '2xx' ] )}"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_RUM_APP_ID, "appid_test");
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(Constants.KEY_RUM_RESOURCE_STATUS, 404);
        fields.put(Constants.KEY_RUM_RESOURCE_STATUS_GROUP, "4xx");

        Assert.assertTrue(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_RESOURCE,
                tags, fields));

        fields.put(Constants.KEY_RUM_RESOURCE_STATUS, 503);
        fields.put(Constants.KEY_RUM_RESOURCE_STATUS_GROUP, "5xx");
        Assert.assertTrue(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_RESOURCE,
                tags, fields));

        fields.put(Constants.KEY_RUM_RESOURCE_STATUS, 200);
        fields.put(Constants.KEY_RUM_RESOURCE_STATUS_GROUP, "2xx");
        Assert.assertFalse(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_RESOURCE,
                tags, fields));
    }

    @Test
    public void matchSupportsRegexListAndBareRegexValue() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ message match ['drop.*', 'timeout.*'] }",
                "{ message match error.* }",
                "{ message match .*password.* }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "please drop this");
        Assert.assertTrue(filter.isFiltered(DataType.LOG, "test",
                new HashMap<String, Object>(), fields));

        fields.put("message", "error happened");
        Assert.assertTrue(filter.isFiltered(DataType.LOG, "test",
                new HashMap<String, Object>(), fields));

        fields.put("message", "contains password");
        Assert.assertTrue(filter.isFiltered(DataType.LOG, "test",
                new HashMap<String, Object>(), fields));

        fields.put("message", "keep");
        Assert.assertFalse(filter.isFiltered(DataType.LOG, "test",
                new HashMap<String, Object>(), fields));
    }

    @Test
    public void fieldNamesAreCaseSensitive() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("rum", new String[]{
                "{ AppID in ['appid_xxx'] }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> tags = new HashMap<>();
        tags.put("appid", "appid_xxx");
        Assert.assertFalse(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_RESOURCE,
                tags, new HashMap<String, Object>()));

        tags.put("AppID", "appid_xxx");
        Assert.assertTrue(filter.isFiltered(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_RESOURCE,
                tags, new HashMap<String, Object>()));
    }

    @Test
    public void bareCommaSeparatedValuesAreSupported() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ source in custom_log, other_log }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        Assert.assertTrue(filter.isFiltered(DataType.LOG, "other_log",
                new HashMap<String, Object>(), new HashMap<String, Object>()));
        Assert.assertFalse(filter.isFiltered(DataType.LOG, "third_log",
                new HashMap<String, Object>(), new HashMap<String, Object>()));
    }

    @Test
    public void inOperatorCanMatchCollectionFieldValues() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ flags in ['debug'] }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("flags", Arrays.asList("network", "debug"));

        Assert.assertTrue(filter.isFiltered(DataType.LOG, "test",
                new HashMap<String, Object>(), fields));
    }

    @Test
    public void unsupportedRealParserRulesAreIgnored() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{}",
                "{ message = 'drop' }",
                "{ message != 'keep' }",
                "{ duration >= 1000 }",
                "{ source = re('test.*') }",
                "{ missing in [null] }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "drop");
        fields.put("duration", 1200L);

        Assert.assertFalse(filter.isFiltered(DataType.LOG, "test-log",
                new HashMap<String, Object>(), fields));
    }

    @Test
    public void invalidRegexInMatchListIsIgnored() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ message match ['g(-z]+ng wrong regex'] }",
                "{ message not match ['g(-z]+ng wrong regex'] }",
                "{ message not match [] }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("message", "abc123");

        Assert.assertFalse(filter.isFiltered(DataType.LOG, "test",
                new HashMap<String, Object>(), fields));
    }

    @Test
    public void missingFieldsDoNotMatchNegativeOperators() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("logging", new String[]{
                "{ missing not in ['hello'] }",
                "{ missing not match ['hello.*'] }"
        });
        FTDataFilter filter = FTDataFilter.compile(rules);

        Assert.assertFalse(filter.isFiltered(DataType.LOG, "test",
                new HashMap<String, Object>(), new HashMap<String, Object>()));
    }

    @Test
    public void unsupportedCategoryIsIgnored() {
        HashMap<String, String[]> rules = new HashMap<>();
        rules.put("metric", new String[]{"{ measurement in ['cpu'] }"});
        FTDataFilter filter = FTDataFilter.compile(rules);

        Assert.assertFalse(filter.isFiltered(DataType.LOG, "cpu",
                new HashMap<String, Object>(), new HashMap<String, Object>()));
    }
}
