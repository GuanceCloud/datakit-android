package com.ft.sdk.sessionreplay.internal.recorder.resources;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

public class BitmapCachesManagerTest {

    @Test
    public void resourcePayloadShouldUseItsOwnCache() {
        MapCache resourceIdCache = new MapCache();
        MapCache resourceDataCache = new MapCache();
        BitmapCachesManager manager = new BitmapCachesManager(
                resourceIdCache,
                resourceDataCache,
                null,
                null
        );
        byte[] payload = new byte[]{1, 2, 3};

        manager.putResourceData("hash-1", payload);

        assertArrayEquals(payload, manager.getResourceData("hash-1"));
    }

    private static class MapCache implements Cache<String, byte[]> {
        private final Map<String, byte[]> values = new HashMap<>();

        @Override
        public void put(byte[] value) {
        }

        @Override
        public void put(String element, byte[] value) {
            values.put(element, value);
        }

        @Override
        public byte[] get(String element) {
            return values.get(element);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public void clear() {
            values.clear();
        }
    }
}
