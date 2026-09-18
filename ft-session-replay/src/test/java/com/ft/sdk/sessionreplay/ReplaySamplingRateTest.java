package com.ft.sdk.sessionreplay;
import org.junit.Test;
import static org.junit.Assert.*;

@SuppressWarnings("deprecation")
public class ReplaySamplingRateTest {
    @Test public void replayPercentagesKeepDefaultsZeroFractionsAndPrecedence() {
        assertEquals(100f, new FTSessionReplayConfig().getSessionReplaySamplingRate(), 0f);
        assertEquals(0f, new FTSessionReplayConfig().getSessionReplayOnErrorSamplingRate(), 0f);
        for (float value : new float[]{0f, .1f, 1f, 20f, 99.9f, 100f}) {
            FTSessionReplayConfig config = new FTSessionReplayConfig().setSampleRate(1f)
                .setSessionReplayOnErrorSampleRate(1f).setSessionReplaySamplingRate(value)
                .setSessionReplayOnErrorSamplingRate(value).setSampleRate(.5f)
                .setSessionReplayOnErrorSampleRate(.5f);
            assertEquals(value / 100f, config.getSampleRate(), 0f);
            assertEquals(value / 100f, config.getSessionReplayOnErrorSampleRate(), 0f);
        }
        FTSessionReplayConfig legacy = new FTSessionReplayConfig().setSampleRate(.25f).setSessionReplayOnErrorSampleRate(.5f);
        assertEquals(25f, legacy.getSessionReplaySamplingRate(), 0f);
        assertEquals(50f, legacy.getSessionReplayOnErrorSamplingRate(), 0f);
    }
    @Test public void invalidValuesDoNotMutateConfiguration() {
        FTSessionReplayConfig config = new FTSessionReplayConfig().setSessionReplaySamplingRate(20f).setSessionReplayOnErrorSamplingRate(10f);
        for (float bad : new float[]{-1f, 101f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> config.setSessionReplaySamplingRate(bad));
            assertThrows(IllegalArgumentException.class, () -> config.setSessionReplayOnErrorSamplingRate(bad));
            assertEquals(20f, config.getSessionReplaySamplingRate(), .0001f);
            assertEquals(10f, config.getSessionReplayOnErrorSamplingRate(), .0001f);
        }
    }
}
