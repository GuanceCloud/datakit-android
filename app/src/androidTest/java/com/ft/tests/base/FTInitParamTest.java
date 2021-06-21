package com.ft.tests.base;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTLoggerConfigManager;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.FTTraceConfigManager;
import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.manager.AsyncCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static com.ft.AllTests.hasPrepare;
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
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
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
    public void emptyServiceName() {
        serviceNameParamTest(null, DEFAULT_LOG_SERVICE_NAME);
    }

    @Test
    public void normalServiceName() {
        serviceNameParamTest("Test", "Test");
    }

    @Test
    public void emptyEnv() {
        envParamTest(null, EnvType.PROD);
    }

    @Test
    public void normalEnv() {
        envParamTest(EnvType.GRAY, EnvType.GRAY);
    }

    @Test
    public void emptyUrl() throws JSONException, InterruptedException {
        urlParamTest(null, 10004);
    }

    @Test
    public void errorUrl() throws JSONException, InterruptedException {
        urlParamTest("http://www.baidu.com", 404);
    }

    @Test
    public void normalUrl() throws JSONException, InterruptedException {
        urlParamTest(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL), 200);
    }

    @Test
    public void rumAppId() {
        String appid = "appIdxxxxxx";
        FTRUMConfigManager.get().initWithConfig(new FTRUMConfig().setRumAppId(appid));
        Assert.assertEquals(appid, FTRUMConfigManager.get().getConfig().getRumAppId());
        Assert.assertTrue(FTRUMConfigManager.get().isRumEnable());
        Assert.assertEquals(FTRUMConfigManager.get().getConfig().getRumAppId(),appid);
        //todo
    }


    private void uuidParamTest(String uuid, int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = getDefaultConfig()
                .setXDataKitUUID(uuid);
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }

    private void serviceNameParamTest(String serviceName, String expected) {
        FTSDKConfig ftSDKConfig = getDefaultConfig();
        FTSdk.install(ftSDKConfig);
        FTLoggerConfigManager.get().initWithConfig(new FTLoggerConfig().setServiceName(serviceName));
        FTTraceConfigManager.get().initWithConfig(new FTTraceConfig().setServiceName(serviceName));

        Assert.assertEquals(expected, FTLoggerConfigManager.get().getConfig().getServiceName());
        Assert.assertEquals(expected, FTTraceConfigManager.get().getConfig().getServiceName());
    }

    private void envParamTest(EnvType env, EnvType expected) {
        FTSDKConfig ftSDKConfig = getDefaultConfig()
                .setEnv(env);
        FTSdk.install(ftSDKConfig);
        Assert.assertEquals(expected, FTSdk.get().getBaseConfig().getEnv());
    }

    public void urlParamTest(String url, int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(url);
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }


    private void requestNetVerifyData(int expected) throws JSONException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject tags = new JSONObject();
        tags.put("testTag", "111");
        JSONObject fields = new JSONObject();
        fields.put("testFields", "222");
        FTTrack.getInstance().trackImmediate("TestMeasurement", tags, fields, new AsyncCallback() {
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
        return FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL)
        );
    }
}
