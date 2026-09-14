package com.ft.sdk;

import org.junit.Test;
import java.util.regex.Pattern;
import static org.junit.Assert.*;

public class FTVisitorIdConfigTest {
    @org.junit.Before public void disableAndroidLogging() {
        com.ft.sdk.garble.utils.LogUtils.setDebug(false);
    }
    @Test public void validatesHeaderAndTtlBoundaries() {
        for (String name : new String[]{null, "", "X-Visitor", "bad header", "x\r\ny", "cookie",
                "traceparent", "tracestate", "x-datadog-origin", "x-b3-traceid", "sw8", "b3",
                "x-rum-resource-id", "x-api-key", "x-xsrf-token", "x-signature", "content-length"}) {
            try {
                FTVisitorIdManagerTest.enabled().setHeader(name).validatedCopy();
                fail("Accepted reserved/invalid header: " + name);
            } catch (IllegalArgumentException expected) { }
        }
        for (double ttl : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE}) {
            try {
                FTVisitorIdManagerTest.enabled().setTtlHours(ttl).validatedCopy();
                fail("Accepted invalid TTL: " + ttl);
            } catch (IllegalArgumentException expected) { }
        }
        assertEquals(86400000, FTVisitorIdManagerTest.enabled().validatedCopy().getTtlMillis());
        assertEquals(1800000, FTVisitorIdManagerTest.enabled().setTtlHours(.5).validatedCopy().getTtlMillis());
    }

    @Test public void prefixMatchesOnlySameOrigin() {
        FTVisitorIdConfig config = new FTVisitorIdConfig().setHeader("suid")
                .addMatch("https://api.example.com").validatedCopy();
        assertTrue(config.matches("https://api.example.com/api"));
        assertTrue(config.matches("https://api.example.com:443/api"));
        assertFalse(config.matches("https://api.example.com.evil.test/api"));
        assertFalse(config.matches("https://api.example.com@evil.test/api"));
        assertFalse(config.matches("https://api.example.com:444/api"));
        assertFalse(config.matches("http://api.example.com/api"));
    }

    @Test public void regexAndCallbacksAreIndependentAndExceptionsDoNotAbortMatching() {
        FTVisitorIdConfig config = FTVisitorIdManagerTest.enabled()
                .addMatch((FTVisitorIdConfig.UrlMatcher) url -> { throw new IllegalStateException(); })
                .addMatch(Pattern.compile("^https://other\\.example/api/"))
                .addMatch((FTVisitorIdConfig.UrlMatcher) url -> url.endsWith("/special"))
                .validatedCopy();
        assertTrue(config.matches("https://other.example/api/one"));
        assertTrue(config.matches("https://third.example/special"));
        assertFalse(config.matches("https://unmatched.example/"));
    }

    @Test public void enabledConfigurationNeedsMatchingRules() {
        try {
            new FTVisitorIdConfig().setHeader("x-visitor").validatedCopy();
            fail("Accepted missing matchers");
        } catch (IllegalArgumentException expected) { }
        try {
            FTVisitorIdManagerTest.enabled().addMatch("").validatedCopy();
            fail("Accepted empty prefix");
        } catch (IllegalArgumentException expected) { }
    }
}
