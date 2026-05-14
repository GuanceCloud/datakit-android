package com.ft.sdk;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.StringUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class NetProxyTest {

    @After
    public void tearDown() {
        FTHttpConfigManager.release();
        LogUtils.setDebug(false);
    }

    @Test
    public void toCommand_masksDatawayToken() {
        String token = "test_dataway_token";
        LogUtils.setDebug(false);
        FTHttpConfigManager.get().setDatawayUrl("https://dataway.example.com/v1/write/rum", token);

        HttpBuilder builder = HttpBuilder.Builder().setMethod(RequestMethod.POST);

        String command = new NetProxy.CurlBuilder(builder).toCommand();

        Assert.assertTrue(command.contains("token=" + StringUtils.maskHalfCharacter(token)));
        Assert.assertFalse(command.contains("token=" + token));
    }

    @Test
    public void toCommand_keepsDatakitUrlUntouched() {
        String url = "https://datakit.example.com/v1/write/rum?token=plain_token";
        LogUtils.setDebug(false);
        FTHttpConfigManager.get().setDatakitUrl(url);

        HttpBuilder builder = HttpBuilder.Builder().setMethod(RequestMethod.POST);

        String command = new NetProxy.CurlBuilder(builder).toCommand();

        Assert.assertTrue(command.contains(url));
    }

    @Test
    public void datakitUploadUrlCanDisableServerFilter() {
        LogUtils.setDebug(false);
        FTHttpConfigManager.get().setDatakitUrl("https://datakit.example.com");

        String url = HttpBuilder.Builder()
                .setModel(Constants.URL_MODEL_LOG)
                .addParam(Constants.URL_PARAM_DISABLE_FILTER, "true")
                .setMethod(RequestMethod.POST)
                .getUrl();

        Assert.assertEquals("https://datakit.example.com/v1/write/logging?disable_filter=true", url);
    }

    @Test
    public void datawayUploadUrlCanDisableServerFilter() {
        LogUtils.setDebug(false);
        FTHttpConfigManager.get().setDatawayUrl("https://dataway.example.com", "test-token");

        String url = HttpBuilder.Builder()
                .setModel(Constants.URL_MODEL_RUM)
                .addParam(Constants.URL_PARAM_DISABLE_FILTER, "true")
                .setMethod(RequestMethod.POST)
                .getUrl();

        Assert.assertTrue(url.startsWith("https://dataway.example.com/v1/write/rum?"));
        Assert.assertTrue(url.contains("token=test-token"));
        Assert.assertTrue(url.contains("to_headless=true"));
        Assert.assertTrue(url.contains("disable_filter=true"));
    }
}
