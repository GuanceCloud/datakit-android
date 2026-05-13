package com.ft.sdk.garble;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

public class FTHttpConfigManagerTest {

    @After
    public void tearDown() {
        FTHttpConfigManager.release();
    }

    @Test
    public void isUrlAvailableReturnsFalseForMissingUrl() {
        assertFalse(FTHttpConfigManager.get().isUrlAvailable());
    }

    @Test
    public void isUrlAvailableReturnsFalseForUrlWithoutHttpScheme() {
        FTHttpConfigManager.get().setDatakitUrl("localhost:9529");

        assertFalse(FTHttpConfigManager.get().isUrlAvailable());
    }

    @Test
    public void isUrlAvailableReturnsTrueForValidDatakitUrl() {
        FTHttpConfigManager.get().setDatakitUrl("https://example.com");

        assertTrue(FTHttpConfigManager.get().isUrlAvailable());
    }

    @Test
    public void isUrlAvailableRequiresDatawayToken() {
        FTHttpConfigManager.get().setDatawayUrl("https://openway.example.com", "");

        assertFalse(FTHttpConfigManager.get().isUrlAvailable());

        FTHttpConfigManager.get().setDatawayUrl("https://openway.example.com", "token");

        assertTrue(FTHttpConfigManager.get().isUrlAvailable());
    }
}
