package com.ft.sdk;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/** Set on FTRUMConfig to enable anonymous visitor headers, independent of RUM and Trace sampling. */
public class FTVisitorIdConfig {
    /** Matches a complete HTTP(S) URL. Exceptions are treated as a non-match. */
    public interface UrlMatcher {
        boolean matches(String url);
    }

    private String header;
    private double ttlHours = 24;
    private final List<UrlMatcher> matchers = new ArrayList<>();

    /** Sets the required lowercase custom header name. */
    public FTVisitorIdConfig setHeader(String header) {
        this.header = header;
        return this;
    }

    /** Sets a positive fixed lifetime in hours; defaults to 24, without sliding renewal. */
    public FTVisitorIdConfig setTtlHours(double ttlHours) {
        this.ttlHours = ttlHours;
        return this;
    }

    /** Adds a complete URL prefix, constrained to the same scheme, host and port. */
    public FTVisitorIdConfig addMatch(final String prefix) {
        matchers.add(prefix == null || prefix.isEmpty() ? null : new UrlMatcher() {
            @Override
            public boolean matches(String url) {
                return url.startsWith(prefix) && sameOrigin(prefix, url);
            }
        });
        return this;
    }

    /** Adds a regular expression searched against the complete URL. */
    public FTVisitorIdConfig addMatch(final Pattern pattern) {
        matchers.add(pattern == null ? null : new UrlMatcher() {
            @Override
            public boolean matches(String url) {
                return pattern.matcher(url).find();
            }
        });
        return this;
    }

    /** Adds a custom URL predicate. */
    public FTVisitorIdConfig addMatch(UrlMatcher matcher) {
        matchers.add(matcher);
        return this;
    }

    String getHeader() {
        return header;
    }

    long getTtlMillis() {
        return (long) (ttlHours * 3600000);
    }

    FTVisitorIdConfig validatedCopy() {
        FTVisitorIdConfig copy = new FTVisitorIdConfig();
        if (!validHeader(header)) {
            throw new IllegalArgumentException("Visitor ID header must be a lowercase custom HTTP token, outside authentication, signature and tracing headers");
        }
        if (Double.isNaN(ttlHours) || Double.isInfinite(ttlHours)
                || ttlHours * 3600000 < 1 || ttlHours * 3600000 >= Long.MAX_VALUE) {
            throw new IllegalArgumentException("Visitor ID TTL must be a positive, representable number of hours");
        }
        if (matchers.isEmpty() || matchers.contains(null)) {
            throw new IllegalArgumentException("Visitor ID requires non-empty URL matching rules");
        }
        copy.header = header;
        copy.ttlHours = ttlHours;
        copy.matchers.addAll(matchers);
        return copy;
    }

    boolean matches(String url) {
        for (UrlMatcher matcher : matchers) {
            try {
                if (matcher.matches(url)) return true;
            } catch (RuntimeException ignored) {
                com.ft.sdk.garble.utils.LogUtils.w("FTVisitorId", "Visitor ID URL matcher failed");
            }
        }
        return false;
    }

    static boolean sameOrigin(String first, String second) {
        try {
            URI a = new URI(first);
            URI b = new URI(second);
            return a.getHost() != null && b.getHost() != null
                    && ("http".equalsIgnoreCase(a.getScheme()) || "https".equalsIgnoreCase(a.getScheme()))
                    && a.getScheme().equalsIgnoreCase(b.getScheme())
                    && a.getHost().equalsIgnoreCase(b.getHost()) && port(a) == port(b);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static int port(URI uri) {
        return uri.getPort() >= 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
    }

    private static boolean validHeader(String header) {
        if (header == null || !header.matches("[!#$%&'*+.^_`|~0-9a-z-]+")) return false;
        for (String prefix : new String[]{"proxy-", "sec-", "x-datadog-", "x-b3-"}) {
            if (header.startsWith(prefix)) return false;
        }
        for (String fragment : new String[]{"authorization", "authentication", "api-key", "api_key",
                "apikey", "auth-token", "auth_token", "access-token", "access_token", "session-token",
                "session_token", "refresh-token", "refresh_token", "id-token", "id_token",
                "security-token", "security_token", "csrf", "xsrf", "jwt", "signature"}) {
            if (header.contains(fragment)) return false;
        }
        return !Arrays.asList("accept-charset", "accept-encoding", "access-control-request-headers",
                "access-control-request-method", "connection", "content-length", "cookie", "cookie2",
                "date", "dnt", "expect", "host", "keep-alive", "origin", "permissions-policy",
                "referer", "te", "trailer", "transfer-encoding", "upgrade", "via", "b3", "traceparent",
                "tracestate", "uber-trace-id", "sw8", "x-gc-trace-id", "x-gc-span-id",
                "x-rum-resource-id").contains(header);
    }
}
