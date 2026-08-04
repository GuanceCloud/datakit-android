package com.ft.sdk.sessionreplay.internal.resources;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ResourceUploadKeyTest {

    @Test
    public void mainAndDifferentRoutesShouldHaveDifferentKeys() {
        ResourceUploadKey main = ResourceUploadKey.from("hash-1", Collections.emptyMap());
        ResourceUploadKey routeOne = ResourceUploadKey.from("hash-1", context("widget-1"));
        ResourceUploadKey routeTwo = ResourceUploadKey.from("hash-1", context("widget-2"));

        assertFalse(main.isRouted());
        assertTrue(routeOne.isRouted());
        assertNotEquals(main, routeOne);
        assertNotEquals(routeOne, routeTwo);
        assertNotEquals(routeOne.toStorageKey(), routeTwo.toStorageKey());
    }

    @Test
    public void storageKeyShouldBeVersionedAndNotMatchLegacyHash() {
        ResourceUploadKey key = ResourceUploadKey.from("hash-1", context("widget|1"));

        assertEquals("v2|8|widget|1|hash-1", key.toStorageKey());
        assertNotEquals("hash-1", key.toStorageKey());
    }

    private Map<String, Object> context(String wgtId) {
        Map<String, Object> context = new HashMap<>();
        context.put(ResourceUploadKey.WGT_ID_KEY, wgtId);
        return context;
    }
}
