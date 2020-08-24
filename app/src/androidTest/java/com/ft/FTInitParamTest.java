package com.ft;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.MonitorType;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

/**
 * BY huangDianHua
 * DATE:2020-01-10 15:20
 * Description: 用户绑定与解绑测试类
 */
@RunWith(AndroidJUnit4.class)
public class FTInitParamTest {
    Context context = null;
    int codeScope = 0;
    @Before
    public void setUp(){
        context = DemoApplication.getContext();
    }

    @Test
    public void emptyUuidParamTest() throws JSONException, InterruptedException {
        uuidParamTest(null,200);
    }

    @Test
    public void normalUuidParamTest() throws JSONException, InterruptedException {
        uuidParamTest("ft-dataKit-uuid-001",200);
    }

    @Test
    public void emptyTokenParamTest() throws JSONException, InterruptedException {
        tokenParamTest(null,400);
    }

    @Test
    public void normalTokenParamTest() throws JSONException, InterruptedException {
        tokenParamTest(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN),200);
    }

    private void uuidParamTest(String uuid,int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID(uuid);
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }

    private void tokenParamTest(String token,int expected) throws JSONException, InterruptedException {
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(token);
        FTSdk.install(ftSDKConfig);
        requestNetVerifyData(expected);
    }
    private void requestNetVerifyData(int expected) throws JSONException, InterruptedException{
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject tags = new JSONObject();
        tags.put("testTag","111");
        JSONObject fields = new JSONObject();
        fields.put("testFields","222");
        FTTrack.getInstance().trackImmediate("TestMeasurement", tags, fields, new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                codeScope = code;
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        assertEquals(expected,codeScope);
    }
}
