package com.ft.sdk;

import com.ft.sdk.internal.issue.FTIssueFieldEnricher;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class FTIssueFieldEnricherTest {

    @Test
    public void additionalFieldsAcceptOnlySupportedValuesAndRespectSdkFields() {
        Map<String, Object> supplied = new HashMap<>();
        supplied.put("string", "value");
        supplied.put("boolean", true);
        supplied.put("integer", 7);
        supplied.put("double", 2.5d);
        supplied.put("decimal", new BigDecimal("1.25"));
        supplied.put("nan", Double.NaN);
        supplied.put("infinity", Float.POSITIVE_INFINITY);
        supplied.put("object", new Object());
        supplied.put("collection", Arrays.asList("value"));
        supplied.put("null", null);
        supplied.put("", "empty-key");
        supplied.put("error.custom", "reserved");
        supplied.put("error_message", "reserved");

        FTIssueFieldEnricher enricher = new FTIssueFieldEnricher(issue -> supplied);
        HashMap<String, Object> additional = enricher.provideAdditionalFields(newIssue());
        HashMap<String, Object> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        tags.put("integer", "sdk-tag");
        fields.put("string", "sdk-field");

        enricher.mergeAdditionalFields(additional, tags, fields,
                java.util.Collections.<String>emptySet());

        Assert.assertEquals("sdk-field", fields.get("string"));
        Assert.assertFalse(fields.containsKey("integer"));
        Assert.assertEquals(true, fields.get("boolean"));
        Assert.assertEquals(2.5d, (Double) fields.get("double"), 0d);
        Assert.assertEquals(1.25d, (Double) fields.get("decimal"), 0d);
        Assert.assertFalse(additional.containsKey("nan"));
        Assert.assertFalse(additional.containsKey("infinity"));
        Assert.assertFalse(additional.containsKey("object"));
        Assert.assertFalse(additional.containsKey("collection"));
        Assert.assertFalse(additional.containsKey("null"));
        Assert.assertFalse(additional.containsKey(""));
        Assert.assertFalse(additional.containsKey("error.custom"));
        Assert.assertFalse(additional.containsKey("error_message"));
    }

    @Test
    public void additionalFieldsEnforceLimitsAndDefensivelyCopyTheProviderMap() {
        Map<String, Object> supplied = new HashMap<>();
        for (int i = 0; i < 40; i++) {
            supplied.put("field_" + i, i);
        }
        supplied.put(repeat("k", 101), "too-long-key");
        supplied.put(repeat("中", 34), "too-long-utf8-key");
        supplied.put("too_long_string", repeat("v", 4097));
        supplied.put("too_long_utf8_string", repeat("中", 1366));

        FTIssueFieldEnricher enricher = new FTIssueFieldEnricher(issue -> supplied);
        HashMap<String, Object> additional = enricher.provideAdditionalFields(newIssue());
        supplied.clear();

        Assert.assertEquals(32, additional.size());
        Assert.assertFalse(additional.containsKey("too_long_string"));
        Assert.assertFalse(additional.containsKey("too_long_utf8_string"));
        Assert.assertFalse(additional.containsKey(repeat("k", 101)));
        Assert.assertFalse(additional.containsKey(repeat("中", 34)));
    }

    @Test
    public void additionalFieldsFailOpenForThrowableAndOversizedPayload() {
        FTIssueFieldEnricher throwing = new FTIssueFieldEnricher(issue -> {
            throw new AssertionError("provider failure");
        });
        Assert.assertTrue(throwing.provideAdditionalFields(newIssue()).isEmpty());

        Map<String, Object> supplied = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            supplied.put("large_" + i, repeat("v", 4096));
        }
        HashMap<String, Object> additional =
                new FTIssueFieldEnricher(issue -> supplied).provideAdditionalFields(newIssue());

        Assert.assertTrue(additional.size() < supplied.size());
        Assert.assertTrue(estimatedBytes(additional) <= FTIssueFieldEnricher.MAX_TOTAL_BYTES);
    }

    @Test
    public void additionalFieldsAreIsolatedAcrossConcurrentCalls() throws Exception {
        Map<String, Object> shared = new HashMap<>();
        shared.put("field", "value");
        FTIssueFieldEnricher enricher = new FTIssueFieldEnricher(issue -> shared);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<HashMap<String, Object>>> calls = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                calls.add(() -> enricher.provideAdditionalFields(newIssue()));
            }
            List<Future<HashMap<String, Object>>> futures = executor.invokeAll(calls);
            HashMap<String, Object> first = futures.get(0).get();
            first.put("mutated", true);
            for (int i = 1; i < futures.size(); i++) {
                Assert.assertNotSame(first, futures.get(i).get());
                Assert.assertFalse(futures.get(i).get().containsKey("mutated"));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void nullEmptyAndSlowProvidersKeepIssueProcessingOpen() throws Exception {
        Assert.assertTrue(new FTIssueFieldEnricher(issue -> null)
                .provideAdditionalFields(newIssue()).isEmpty());
        Assert.assertTrue(new FTIssueFieldEnricher(issue -> new HashMap<String, Object>())
                .provideAdditionalFields(newIssue()).isEmpty());

        FTIssueFieldEnricher slow = new FTIssueFieldEnricher(issue -> {
            try {
                Thread.sleep(60L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return java.util.Collections.<String, Object>singletonMap("field", "value");
        });

        Assert.assertEquals("value", slow.provideAdditionalFields(newIssue()).get("field"));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void malformedRawMapEntriesAreDroppedIndividually() {
        Map rawMap = new HashMap();
        rawMap.put(123, "non-string-key");
        rawMap.put("bad_number", new Number() {
            @Override
            public int intValue() {
                return 0;
            }

            @Override
            public long longValue() {
                return 0;
            }

            @Override
            public float floatValue() {
                return 0;
            }

            @Override
            public double doubleValue() {
                throw new AssertionError("malformed number");
            }
        });
        rawMap.put("valid", 1);

        HashMap<String, Object> additional =
                new FTIssueFieldEnricher(issue -> rawMap).provideAdditionalFields(newIssue());

        Assert.assertEquals(1, additional.size());
        Assert.assertEquals(1, additional.get("valid"));
    }

    @Test
    public void providerMapIsCopiedWithoutTrustingItsIsEmptyImplementation() {
        HashMap<String, Object> supplied = new HashMap<String, Object>() {
            @Override
            public boolean isEmpty() {
                throw new AssertionError("untrusted map implementation");
            }
        };
        supplied.put("valid", true);

        HashMap<String, Object> additional =
                new FTIssueFieldEnricher(issue -> supplied).provideAdditionalFields(newIssue());

        Assert.assertEquals(true, additional.get("valid"));
    }

    private static FTIssueInfo newIssue() {
        return new FTIssueInfo(FTIssueCategory.CRASH, "java_crash", "message", "stack",
                123L, "run", "thread", false);
    }

    private static int estimatedBytes(Map<String, Object> fields) {
        int bytes = 0;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            bytes += entry.getKey().getBytes(StandardCharsets.UTF_8).length;
            bytes += String.valueOf(entry.getValue()).getBytes(StandardCharsets.UTF_8).length;
        }
        return bytes;
    }

    private static String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
