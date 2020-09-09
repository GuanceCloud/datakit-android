package com.ft.tests.base;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.BuildConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.FTExceptionHandler;
import com.ft.sdk.garble.SyncCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static com.ft.sdk.garble.utils.Constants.DEFAULT_LOG_SERVICE_NAME;
import static org.junit.Assert.assertEquals;

/**
 * BY huangDianHua
 * DATE:2020-01-10 15:20
 * Description: 用户绑定与解绑测试类
 */
@RunWith(AndroidJUnit4.class)
public class FTInitParamTest extends BaseTest {
    Context context = null;
    int codeScope = 0;

    @Before
    public void setUp() {
        context = MockApplication.getContext();
    }

    @Test
    public void emptyUuidParamTest() throws JSONException, InterruptedException {
        uuidParamTest(null, 200);
    }

    @Test
    public void normalUuidParamTest() throws JSONException, InterruptedException {
        uuidParamTest("ft-dataKit-uuid-001", 200);
    }

    @Test
    public void emptyTokenParamTest() throws JSONException, InterruptedException {
        tokenParamTest(null, 400);
    }

    @Test
    public void errorTokenParamTest() throws JSONException, InterruptedException {
        tokenParamTest("1111111", 10005);
    }

    @Test
    public void normalTokenParamTest() throws JSONException, InterruptedException {
        tokenParamTest(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN), 200);
    }

    @Test
    public void emptyServiceName() {
        serviceNameParamTest(null, DEFAULT_LOG_SERVICE_NAME);
    }

    @Test
    public void normalServiceName() {
        serviceNameParamTest("Test", "Test");
    }

    @Test
    public void emptyEnv() {
        envParamTest(null, BuildConfig.BUILD_TYPE);
    }

    @Test
    public void normalEnv() {
        envParamTest("Test", "Test");
    }

    @Test
    public void emptyUrl() throws JSONException, InterruptedException {
        urlParamTest(null, 10005);
    }

    @Test
    public void errorUrl() throws JSONException, InterruptedException {
        urlParamTest("http://www.baidu.com", 10005);
    }

    @Test
    public void normalUrl() throws JSONException, InterruptedException {
        urlParamTest(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL), 200);
    }

    @Test
    public void emptyKeyId() {
        try {
            FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                    true,
                    null,
                    AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                    .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN));
        } catch (Exception e) {
            assertEquals(e.getMessage(), "akId 未初始化");
        }
    }

    @Test
    public void errorKeyId() throws JSONException, InterruptedException {
        keyIdParamTest("00000", 200);
    }

    @Test
    public void normalKeyId() throws JSONException, InterruptedException {
        keyIdParamTest(AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID), 200);
    }

    @Test
    public void emptyKeySecret() {
        try {
            FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                    true,
                    AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                    null)
                    .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN));
        } catch (Exception e) {
            assertEquals(e.getMessage(), "akSecret 未初始化");
        }
    }

    @Test
    public void errorKeySecret() throws JSONException, InterruptedException {
        keyIdParamTest("00000", 200);
    }

    @Test
    public void normalKeySecret() throws JSONException, InterruptedException {
        keyIdParamTest(AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET), 200);
    }

    private void uuidParamTest(String uuid, int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = getDefaultConfig()
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID(uuid);
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }

    private void tokenParamTest(String token, int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = getDefaultConfig()
                .setDataWayToken(token);
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }

    private void serviceNameParamTest(String serviceName, String expected) {
        FTSDKConfig ftSDKConfig = getDefaultConfig()
                .setTraceServiceName(serviceName)
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN));
        FTSdk.install(ftSDKConfig);
        Assert.assertEquals(expected, FTExceptionHandler.get().getTrackServiceName());
    }

    private void envParamTest(String env, String expected) {
        FTSDKConfig ftSDKConfig = getDefaultConfig()
                .setEnv(env)
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN));
        FTSdk.install(ftSDKConfig);
        Assert.assertEquals(expected, FTExceptionHandler.get().getEnv());
    }

    public void urlParamTest(String url, int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(url,
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN));
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }

    public void keyIdParamTest(String keyId, int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                keyId,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN));
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }

    public void keySecretParamTest(String keySecret, int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                keySecret)
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN));
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }

    private void requestNetVerifyData(int expected) throws JSONException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject tags = new JSONObject();
        tags.put("testTag", "111");
        JSONObject fields = new JSONObject();
        fields.put("testFields", "222");
        FTTrack.getInstance().trackImmediate("TestMeasurement", tags, fields, new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                codeScope = code;
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        assertEquals(expected, codeScope);
    }

    private FTSDKConfig getDefaultConfig() {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET));
        return ftSDKConfig;
    }
}
