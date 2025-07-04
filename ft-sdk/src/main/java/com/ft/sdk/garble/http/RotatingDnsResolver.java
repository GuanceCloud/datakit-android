package com.ft.sdk.garble.http;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Dns;
/**
 * From [DataDog/dd-sdk-android] (https://github.com/DataDog/dd-sdk-android/blob/develop/dd-sdk-android-core/src/main/kotlin/com/datadog/android/core/internal/data/upload/RotatingDnsResolver.kt)
 * Licensed under the Apache License, Version 2.0 (https://github.com/DataDog/dd-sdk-android/blob/develop/LICENSE)
 */
public class RotatingDnsResolver implements Dns {

    private final Dns delegate;
    private final long ttlNanoTime; // Use milliseconds to represent TTL
    private final Map<String, ResolvedHost> knownHosts = new ConcurrentHashMap<>();

    public RotatingDnsResolver(Dns delegate, long nanoTime) {
        this.delegate = delegate;
        this.ttlNanoTime = nanoTime;
    }

    public RotatingDnsResolver() {
        this(Dns.SYSTEM, TTL_30_MIN);
    }

    public RotatingDnsResolver(Dns delegate) {
        this(delegate, TTL_30_MIN);
    }

    @NotNull
    @Override
    public List<InetAddress> lookup(@NotNull String hostname) throws UnknownHostException {
        ResolvedHost knownHost = knownHosts.get(hostname);
        if (knownHost != null && isValid(knownHost)) {
            knownHost.rotate();
            synchronized (knownHost.addresses) {
                return copy(knownHost.addresses);
            }
        } else {
            List<InetAddress> result = delegate.lookup(hostname);
            ResolvedHost newHost = new ResolvedHost(hostname, result);
            knownHosts.put(hostname, newHost);
            synchronized (result) {
                return copy(result);
            }
        }
    }

    private List<InetAddress> copy(List<InetAddress> list) {
        return new ArrayList<>(list);
    }

    private boolean isValid(ResolvedHost knownHost) {
        return knownHost.getAgeNanoTime() < ttlNanoTime && !knownHost.addresses.isEmpty();
    }

    public static final long TTL_30_MIN = 1800000000000L; // 30 minutes in milliseconds

    public static class ResolvedHost {
        private final String hostname;
        private final List<InetAddress> addresses;
        private final long resolutionTimestampNanoTime;

        public ResolvedHost(String hostname, List<InetAddress> addresses) {
            this.hostname = hostname;
            this.addresses = new LinkedList<>(addresses);
            this.resolutionTimestampNanoTime = System.nanoTime(); // Use Android's time API
        }

        public long getAgeNanoTime() {
            return System.nanoTime() - resolutionTimestampNanoTime;
        }

        public void rotate() {
            synchronized (addresses) {
                if (!addresses.isEmpty()) {
                    InetAddress first = addresses.remove(0);
                    addresses.add(first);
                }
            }
        }
    }
}