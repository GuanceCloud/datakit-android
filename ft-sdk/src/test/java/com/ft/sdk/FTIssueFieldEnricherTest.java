package com.ft.sdk;

import com.ft.sdk.internal.issue.FTIssueFieldEnricher;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class FTIssueFieldEnricherTest {

    @Test
    public void providerMapTraversalIsBoundedByFieldLimit() {
        AtomicInteger visitedEntries = new AtomicInteger();
        Map<String, Object> supplied = mapBackedBy(new Iterator<Map.Entry<String, Object>>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < 10_000;
            }

            @Override
            public Map.Entry<String, Object> next() {
                int current = index++;
                visitedEntries.incrementAndGet();
                return new AbstractMap.SimpleImmutableEntry<>("field_" + current, current);
            }
        }, 10_000);

        HashMap<String, Object> additional =
                new FTIssueFieldEnricher(issue -> supplied).provideAdditionalFields(newIssue());

        Assert.assertEquals(FTIssueFieldEnricher.MAX_FIELD_COUNT, additional.size());
        Assert.assertEquals(FTIssueFieldEnricher.MAX_FIELD_COUNT, visitedEntries.get());
    }

    @Test
    public void invalidEntriesConsumeProviderMapScanBudget() {
        AtomicInteger visitedEntries = new AtomicInteger();
        Map<String, Object> supplied = mapBackedBy(new Iterator<Map.Entry<String, Object>>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < 60;
            }

            @Override
            public Map.Entry<String, Object> next() {
                int current = index++;
                visitedEntries.incrementAndGet();
                return current < FTIssueFieldEnricher.MAX_FIELD_COUNT
                        ? new AbstractMap.SimpleImmutableEntry<>("", current)
                        : new AbstractMap.SimpleImmutableEntry<>("valid_" + current, current);
            }
        }, 60);

        HashMap<String, Object> additional =
                new FTIssueFieldEnricher(issue -> supplied).provideAdditionalFields(newIssue());

        Assert.assertTrue(additional.isEmpty());
        Assert.assertEquals(FTIssueFieldEnricher.MAX_FIELD_COUNT, visitedEntries.get());
    }

    @Test
    public void iteratorFailureDiscardsAllProviderFieldsForCurrentIssue() {
        Map<String, Object> supplied = mapBackedBy(new Iterator<Map.Entry<String, Object>>() {
            private boolean first = true;

            @Override
            public boolean hasNext() {
                if (!first) {
                    throw new AssertionError("iterator failure");
                }
                return true;
            }

            @Override
            public Map.Entry<String, Object> next() {
                first = false;
                return new AbstractMap.SimpleImmutableEntry<>("accepted_before_failure", true);
            }
        }, 2);

        HashMap<String, Object> additional =
                new FTIssueFieldEnricher(issue -> supplied).provideAdditionalFields(newIssue());

        Assert.assertTrue(additional.isEmpty());
    }

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
    public void additionalFieldsEnforceByteLimitsAndDefensivelyCopyTheProviderMap() {
        Map<String, Object> supplied = new LinkedHashMap<>();
        supplied.put("valid", true);
        supplied.put(repeat("k", 101), "too-long-key");
        supplied.put(repeat("中", 34), "too-long-utf8-key");
        supplied.put("too_long_string", repeat("v", 4097));
        supplied.put("too_long_utf8_string", repeat("中", 1366));

        FTIssueFieldEnricher enricher = new FTIssueFieldEnricher(issue -> supplied);
        HashMap<String, Object> additional = enricher.provideAdditionalFields(newIssue());
        supplied.clear();

        Assert.assertEquals(1, additional.size());
        Assert.assertEquals(true, additional.get("valid"));
        Assert.assertFalse(additional.containsKey("too_long_string"));
        Assert.assertFalse(additional.containsKey("too_long_utf8_string"));
        Assert.assertFalse(additional.containsKey(repeat("k", 101)));
        Assert.assertFalse(additional.containsKey(repeat("中", 34)));
    }

    @Test
    public void oversizedStringValidationDoesNotAllocateAFullUtf8Copy() throws Exception {
        ThreadAllocationProbe allocationProbe = threadAllocationProbeOrSkip();

        String oversized = repeat("a", 2 * 1024 * 1024);
        FTIssueFieldEnricher enricher = new FTIssueFieldEnricher(
                issue -> java.util.Collections.<String, Object>singletonMap(
                        "oversized", oversized));
        enricher.provideAdditionalFields(newIssue());

        long before = allocationProbe.currentThreadAllocatedBytes();
        HashMap<String, Object> additional = enricher.provideAdditionalFields(newIssue());
        long allocatedBytes = allocationProbe.currentThreadAllocatedBytes() - before;

        Assert.assertTrue(additional.isEmpty());
        // A full UTF-8 copy is about 2 MiB; 512 KiB tolerates test overhead while catching it.
        Assert.assertTrue("validation allocated " + allocatedBytes + " bytes",
                allocatedBytes < 512 * 1024);
    }

    @Test
    public void stringByteLimitMatchesUtf8EncodingAtBoundary() {
        Map<String, Object> supplied = new LinkedHashMap<>();
        supplied.put("ascii_at_limit", repeat("a", 4096));
        supplied.put("ascii_over_limit", repeat("a", 4097));
        supplied.put("cjk_at_limit", repeat("中", 1364) + "abcd");
        supplied.put("cjk_over_limit", repeat("中", 1365) + "ab");
        supplied.put("emoji_at_limit", repeat("😀", 1024));
        supplied.put("emoji_over_limit", repeat("😀", 1024) + "a");
        supplied.put("unpaired_surrogate_at_limit", repeat("a", 4095) + "\uD800");
        supplied.put("unpaired_surrogate_over_limit", repeat("a", 4096) + "\uD800");

        HashMap<String, Object> additional =
                new FTIssueFieldEnricher(issue -> supplied).provideAdditionalFields(newIssue());

        Assert.assertTrue(additional.containsKey("ascii_at_limit"));
        Assert.assertFalse(additional.containsKey("ascii_over_limit"));
        Assert.assertTrue(additional.containsKey("cjk_at_limit"));
        Assert.assertFalse(additional.containsKey("cjk_over_limit"));
        Assert.assertTrue(additional.containsKey("emoji_at_limit"));
        Assert.assertFalse(additional.containsKey("emoji_over_limit"));
        Assert.assertTrue(additional.containsKey("unpaired_surrogate_at_limit"));
        Assert.assertFalse(additional.containsKey("unpaired_surrogate_over_limit"));
    }

    @Test
    public void additionalFieldsFailOpenForThrowableAndOversizedPayload() {
        FTIssueFieldEnricher throwing = new FTIssueFieldEnricher(issue -> {
            throw new AssertionError("provider failure");
        });
        Assert.assertTrue(throwing.provideAdditionalFields(newIssue()).isEmpty());

        Map<String, Object> supplied = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            supplied.put("large_" + i, repeat("v", 4096));
        }
        HashMap<String, Object> additional =
                new FTIssueFieldEnricher(issue -> supplied).provideAdditionalFields(newIssue());

        Assert.assertEquals(6, additional.size());
        Assert.assertEquals(25 * 1024, FTIssueFieldEnricher.MAX_TOTAL_BYTES);
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
    public void providerMapIsTraversedWithoutTrustingSizeOrIsEmpty() {
        HashMap<String, Object> supplied = new HashMap<String, Object>() {
            @Override
            public int size() {
                throw new AssertionError("untrusted map implementation");
            }

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

    private static ThreadAllocationProbe threadAllocationProbeOrSkip() {
        try {
            Class<?> managementFactory = Class.forName("java.lang.management.ManagementFactory");
            Object allocationBean = managementFactory.getMethod("getThreadMXBean").invoke(null);
            Class<?> allocationBeanClass = Class.forName("com.sun.management.ThreadMXBean");
            Assume.assumeTrue(allocationBeanClass.isInstance(allocationBean));
            Method isSupported =
                    allocationBeanClass.getMethod("isThreadAllocatedMemorySupported");
            Assume.assumeTrue((Boolean) isSupported.invoke(allocationBean));
            Method isEnabled =
                    allocationBeanClass.getMethod("isThreadAllocatedMemoryEnabled");
            if (!(Boolean) isEnabled.invoke(allocationBean)) {
                allocationBeanClass.getMethod(
                                "setThreadAllocatedMemoryEnabled", boolean.class)
                        .invoke(allocationBean, true);
            }
            Method getAllocatedBytes =
                    allocationBeanClass.getMethod("getThreadAllocatedBytes", long.class);
            return new ThreadAllocationProbe(allocationBean, getAllocatedBytes);
        } catch (ReflectiveOperationException | SecurityException unavailable) {
            Assume.assumeNoException(unavailable);
            throw new AssertionError(unavailable);
        }
    }

    private static Map<String, Object> mapBackedBy(
            Iterator<Map.Entry<String, Object>> iterator, int size) {
        return new AbstractMap<String, Object>() {
            @Override
            public Set<Entry<String, Object>> entrySet() {
                return new AbstractSet<Entry<String, Object>>() {
                    @Override
                    public Iterator<Entry<String, Object>> iterator() {
                        return iterator;
                    }

                    @Override
                    public int size() {
                        return size;
                    }
                };
            }
        };
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

    private static final class ThreadAllocationProbe {
        private final Object allocationBean;
        private final Method getAllocatedBytes;

        private ThreadAllocationProbe(Object allocationBean, Method getAllocatedBytes) {
            this.allocationBean = allocationBean;
            this.getAllocatedBytes = getAllocatedBytes;
        }

        private long currentThreadAllocatedBytes() throws Exception {
            return (Long) getAllocatedBytes.invoke(
                    allocationBean, Thread.currentThread().getId());
        }
    }
}
