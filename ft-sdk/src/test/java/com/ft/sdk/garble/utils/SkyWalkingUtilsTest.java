package com.ft.sdk.garble.utils;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Locale;

public class SkyWalkingUtilsTest {

    @Test
    public void traceIdsUseAsciiDigitsUnderArabicLocale() throws Exception {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("ar"));

            SkyWalkingUtils skyWalkingUtils = new SkyWalkingUtils(
                    SkyWalkingUtils.SkyWalkingVersion.V2,
                    "1",
                    123456789L,
                    new URL("https://example.com/path"),
                    "service"
            );

            Assert.assertTrue(skyWalkingUtils.getNewTraceId().matches("[0-9.]+"));
            Assert.assertTrue(skyWalkingUtils.getNewParentTraceId().matches("[0-9.]+"));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}
