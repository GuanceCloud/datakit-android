package com.ft.tests;

import static com.ft.AllTests.hasPrepare;
import static org.junit.Assert.assertEquals;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BaseTest;
import com.ft.BuildConfig;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrackInner;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * BY huangDianHua
 * DATE:2020-01-10 15:20
 * Description: 用户绑定与解绑测试类
 */
@RunWith(AndroidJUnit4.class)
public class ServerConnectTest extends BaseTest {
    int codeScope = 0;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
    }

    /**
     * 空地址错误校验
     * @throws Exception
     */
    @Test
    public void emptyUrl() throws Exception {
        urlParamTest(null, NetCodeStatus.UNKNOWN_EXCEPTION_CODE);
    }

    /**
     * 错误 Datakit 地址校验
     * @throws Exception
     */
    @Test
    public void errorUrl() throws Exception {
        urlParamTest("http://www.baidu.com", 404);
    }

    /**
     * 正常地址校验
     * @throws Exception
     */
    @Test
    public void normalUrl() throws Exception {
        urlParamTest(BuildConfig.DATAKIT_URL, 200);
    }

    /**
     * 正常配置数据请求校验
     * @param url
     * @param expected
     * @throws Exception
     */

    public void urlParamTest(String url, int expected) throws Exception {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(url);
        FTSdk.install(ftSDKConfig);
        FTSdk.initLogWithConfig(new FTLoggerConfig());
        requestNetVerifyData(expected);
    }


    /**
     * Datakit 数据请求校验
     * @param expected
     * @throws Exception
     */
    private void requestNetVerifyData(int expected) throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject tags = new JSONObject();
        tags.put("testTag", "111");
        JSONObject fields = new JSONObject();
        fields.put("testFields", "222");
        long time = Utils.getCurrentNanoTime();
        LineProtocolBean bean = new LineProtocolBean("TestMeasurement", tags, fields, time);
        Whitebox.invokeMethod(FTTrackInner.getInstance(), "trackAsync", Collections.singletonList(bean), new AsyncCallback() {
            @Override
            public void onResponse(int code, String response,String errorCode) {
                codeScope = code;
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        assertEquals(expected, codeScope);
    }

}
