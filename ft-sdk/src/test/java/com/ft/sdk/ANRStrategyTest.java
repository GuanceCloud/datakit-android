package com.ft.sdk;

import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.DataType;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ANRStrategyTest {

    @Test
    public void api30AndAboveUseHistoricalExitInfoInsteadOfLiveWatchdog() {
        assertFalse(ANRStrategy.shouldUseHistoricalAnr(29));
        assertTrue(ANRStrategy.shouldUseHistoricalAnr(30));
        assertTrue(ANRStrategy.shouldUseHistoricalAnr(35));
    }

    @Test
    public void nativeAnrSourceIsDisabledWhenHistoricalExitInfoIsAvailable() {
        assertTrue(ANRStrategy.shouldEnableNativeAnr(true, 29));
        assertFalse(ANRStrategy.shouldEnableNativeAnr(true, 30));
        assertFalse(ANRStrategy.shouldEnableNativeAnr(false, 29));
    }

    @Test
    public void historicalAnrUsesTheOldSessionSamplingBucket() {
        assertEquals(DataType.RUM_APP,
                FTTrackInner.historicalRumDataType(CollectType.COLLECT_BY_SAMPLE));
        assertEquals(DataType.RUM_APP_ERROR_SAMPLED,
                FTTrackInner.historicalRumDataType(CollectType.COLLECT_BY_ERROR_SAMPLE));
    }
}
