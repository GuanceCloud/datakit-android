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
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.manager.RequestCallback;
import com.ft.sdk.garble.utils.Utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.CountDownLatch;

/**
 * BY huangDianHua
 * DATE:2020-01-10 15:20
 * Description: User binding and unbinding test class
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
     * Empty address error validation
     * @throws Exception
     */
    @Test
    public void emptyUrl() throws Exception {
        urlParamTest(null, NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE);
    }

    /**
     * Invalid Datakit address validation
     * @throws Exception
     */
    @Test
    public void errorUrl() throws Exception {
        urlParamTest("http://www.baidu.com", 404);
    }

    /**
     * Valid address validation
     * @throws Exception
     */
    @Test
    public void normalUrl() throws Exception {
        urlParamTest(BuildConfig.DATAKIT_URL, 200);
    }

    /**
     * Valid configuration data request validation
     * @param url
     * @param expected
     * @throws Exception
     */

    public void urlParamTest(String url, int expected) throws Exception {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(url).setDebug(true);
        FTSdk.install(ftSDKConfig);
        FTSdk.initLogWithConfig(new FTLoggerConfig());
        requestNetVerifyData(expected);
    }


    /**
     * Datakit data request validation
     * @param expected
     * @throws Exception
     */
    private void requestNetVerifyData(int expected) throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long time = Utils.getCurrentNanoTime();
        LogBean bean = new LogBean("connect test", time);
        Whitebox.invokeMethod(FTTrackInner.getInstance(), "trackLogAsync", bean, new RequestCallback() {
            @Override
            public void onResponse(int code, String response, String errorCode) {
                codeScope = code;
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        assertEquals(expected, codeScope);
    }

}
