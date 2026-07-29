package com.ft.sdk;

import com.ft.sdk.garble.bean.AppState;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class FTIssueApiTest {

    @Test
    public void configAndIssueInfoExposeTheProviderContract() {
        FTIssueDataProvider provider = issue ->
                Collections.<String, Object>singletonMap("historical_issue", issue.isHistorical());

        FTRUMConfig config = new FTRUMConfig().setIssueDataProvider(provider);
        FTIssueInfo info = new FTIssueInfo(
                FTIssueCategory.CRASH,
                "java_crash",
                "message",
                "stack",
                123L,
                "run",
                "crash-thread",
                false);

        Assert.assertSame(config, config.setIssueDataProvider(provider));
        Assert.assertSame(provider, config.getIssueDataProvider());
        Assert.assertEquals(FTIssueCategory.CRASH, info.getCategory());
        Assert.assertEquals("java_crash", info.getErrorType());
        Assert.assertEquals("message", info.getMessage());
        Assert.assertEquals("stack", info.getStack());
        Assert.assertEquals(123L, info.getOccurredAtNanoseconds());
        Assert.assertEquals("run", info.getAppState());
        Assert.assertEquals("crash-thread", info.getThreadName());
        Assert.assertFalse(info.isHistorical());
        Assert.assertFalse(config.toString().contains("issueDataProvider"));
    }

    @Test
    public void applicationExitInfoAnrUsesHistoricalIssueFacts() {
        FTIssueInfo info = FTRUMInnerManager.createHistoricalAnrIssueInfo(
                "blocked main thread",
                456L,
                AppState.BACKGROUND);

        Assert.assertEquals(FTIssueCategory.ANR, info.getCategory());
        Assert.assertEquals("anr_crash", info.getErrorType());
        Assert.assertEquals("android_anr", info.getMessage());
        Assert.assertEquals("blocked main thread", info.getStack());
        Assert.assertEquals(456L, info.getOccurredAtNanoseconds());
        Assert.assertEquals("background", info.getAppState());
        Assert.assertNull(info.getThreadName());
        Assert.assertTrue(info.isHistorical());
    }
}
